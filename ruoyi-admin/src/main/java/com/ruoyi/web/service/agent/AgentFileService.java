package com.ruoyi.web.service.agent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.agent.api.AgentFileView;
import com.ruoyi.agent.domain.AgentFile;
import com.ruoyi.agent.domain.enums.AgentFileStatus;
import com.ruoyi.agent.infrastructure.dify.DifyClientSettings;
import com.ruoyi.agent.infrastructure.dify.model.DifyStreamEvent;
import com.ruoyi.agent.mapper.AgentFileMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Agent 生成文件的持久化与安全访问服务。
 *
 * <p>
 * 文件内容由 AgentFileStorage 保存，文件元数据保存到数据库。 浏览器只能拿到不可猜测的 resourceId，不能直接访问 Dify
 * 的签名地址或 API Key。
 * </p>
 */
@Service
public class AgentFileService {
    private static final Logger log = LoggerFactory.getLogger(AgentFileService.class);
    private static final long MAX_FILE_BYTES = 50L * 1024 * 1024;
    private static final Duration DOWNLOAD_TIMEOUT = Duration.ofMinutes(3);

    private final AgentFileMapper fileMapper;
    private final AgentFileStorage fileStorage;
    private final HttpClient httpClient;

    public AgentFileService(AgentFileMapper fileMapper, AgentFileStorage fileStorage) {
        this.fileMapper = fileMapper;
        this.fileStorage = fileStorage;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();
    }

    /** 创建一次 Dify 流对应的文件上下文，避免 message_file 与 message_end 重复发送。 */
    public StreamContext newStreamContext() {
        return new StreamContext();
    }

    /** 创建流文件上下文，并记录本轮用户输入附件，防止被误判为 Agent 生成文件。 */
    public StreamContext newStreamContext(List<String> inputFileIds) {
        StreamContext context = new StreamContext();
        if (inputFileIds != null) {
            context.inputFileIds.addAll(inputFileIds.stream()
                    .filter(id -> id != null && !id.isBlank())
                    .toList());
        }
        return context;
    }

    /** 收集 Dify 的文件事件和插件 output_filename，供最终文件卡片使用。 */
    public void capture(DifyStreamEvent event, StreamContext context) {
        if (event == null || context == null) {
            return;
        }
        if ("message_file".equals(event.getEvent())) {
            Map<String, Object> descriptor = copyMap(event.getRaw());
            String fileId = text(descriptor.get("id"));
            if (!fileId.isBlank()) {
                context.pendingFiles.put(fileId, descriptor);
                if (event.getTool() != null && !event.getTool().isBlank()) {
                    context.toolByFileId.put(fileId, event.getTool());
                }
            }
        }
        if ("agent_thought".equals(event.getEvent())) {
            captureHints(event, context);
        }
    }

    /** 将 message_end.files 转换为可供前端消费的统一文件事件数据。 */
    public List<Map<String, Object>> materialize(DifyClientSettings settings, DifyStreamEvent event,
            Long userId, StreamContext context) {
        if (context == null || context.materialized) {
            return Collections.emptyList();
        }
        context.materialized = true;

        LinkedHashMap<String, Map<String, Object>> descriptors = new LinkedHashMap<>(context.pendingFiles);
        for (Map<String, Object> descriptor : extractFileList(event == null ? null : event.getRaw().get("files"))) {
            String id = text(first(descriptor, "related_id", "id", "upload_file_id"));
            descriptors.put(id.isBlank() ? "file-" + descriptors.size() : id, descriptor);
        }

        List<Map<String, Object>> results = new ArrayList<>();
        List<String> orderedTools = new ArrayList<>(context.nameByTool.keySet());
        int descriptorIndex = 0;
        for (Map<String, Object> descriptor : descriptors.values()) {
            if (isInputFile(descriptor, context)) {
                continue;
            }
            String fileId = text(first(descriptor, "related_id", "id", "upload_file_id"));
            String toolName = context.toolByFileId.getOrDefault(fileId, "");
            if (toolName.isBlank() && descriptorIndex < orderedTools.size()) {
                toolName = orderedTools.get(descriptorIndex);
            }
            String hint = context.nameByFileId.get(fileId);
            if (hint == null || hint.isBlank()) {
                hint = context.nameByTool.get(toolName);
            }
            try {
                results.add(store(settings, descriptor, hint, toolName, userId,
                        event == null ? null : event.getConversationId()));
            } catch (Exception exception) {
                if (exception instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                log.warn("保存 Dify 生成文件失败，fileId={}", fileId, exception);
                Map<String, Object> failed = new LinkedHashMap<>();
                failed.put("phase", "failed");
                failed.put("resourceId", UUID.randomUUID().toString());
                failed.put("sourceFileId", fileId);
                failed.put("name", hint == null || hint.isBlank() ? "生成文件" : hint);
                failed.put("error", "文件已生成，但保存到系统失败，请稍后重试");
                results.add(failed);
            }
            descriptorIndex++;
        }
        context.materializedFiles = List.copyOf(results);
        return results;
    }

    private boolean isInputFile(Map<String, Object> descriptor, StreamContext context) {
        String uploadFileId = text(descriptor.get("upload_file_id"));
        String relatedId = text(descriptor.get("related_id"));
        String id = text(descriptor.get("id"));
        return context.inputFileIds.contains(uploadFileId)
                || context.inputFileIds.contains(relatedId)
                || context.inputFileIds.contains(id);
    }

    /** 返回当前流已经物化的文件，供 done 事件做最后兜底传递。 */
    public List<Map<String, Object>> materializedFiles(StreamContext context) {
        return context == null ? Collections.emptyList() : context.materializedFiles;
    }

    /** 按用户归属读取文件资源，未找到或不属于当前用户时统一返回 null。 */
    public StoredFile findOwned(String resourceId, Long userId) {
        if (resourceId == null || userId == null) {
            return null;
        }
        try {
            UUID.fromString(resourceId);
        } catch (IllegalArgumentException exception) {
            return null;
        }

        AgentFile metadata = fileMapper.selectOwned(resourceId, userId);
        if (metadata == null) {
            return null;
        }
        Path file = fileStorage.resolve(metadata.getRelativePath());
        if (file == null) {
            return null;
        }
        return new StoredFile(file, metadata.getFileName(), metadata.getMediaType(),
                "BROWSER".equals(metadata.getPreviewMode()), metadata.getFileSize());
    }

    /** 查询当前用户持久化的全部可用文件。 */
    public List<AgentFileView> listOwned(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return fileMapper.selectByUserId(userId).stream().map(this::toView).toList();
    }

    /** 删除当前用户拥有的文件内容并标记数据库记录。 */
    public boolean deleteOwned(String resourceId, Long userId, String operator) throws IOException {
        AgentFile file = fileMapper.selectOwned(resourceId, userId);
        if (file == null) {
            return false;
        }
        if (fileMapper.markDeleted(resourceId, userId, operator) == 0) {
            return false;
        }
        fileStorage.delete(file.getRelativePath());
        return true;
    }

    private AgentFileView toView(AgentFile file) {
        return new AgentFileView(file.getResourceId(), file.getFileName(), file.getExtension(), file.getMediaType(),
                file.getFileKind(), file.getFileSize(), "/agent/files/" + file.getResourceId(),
                "BROWSER".equals(file.getPreviewMode()) ? "browser" : "download", file.getCreateTime());
    }

    private Map<String, Object> store(DifyClientSettings settings, Map<String, Object> descriptor,
            String filenameHint, String toolName, Long userId, String conversationId)
            throws IOException, InterruptedException {
        String url = text(first(descriptor, "url", "download_url"));
        if (url.isBlank()) {
            throw new IOException("Dify 文件缺少下载地址");
        }
        URI targetUri = resolveDifyFileUri(settings.getBaseUrl(), url);
        String extension = normalizeExtension(text(first(descriptor, "extension")));
        String sourceName = text(first(descriptor, "filename", "name"));
        if (extension.isBlank()) {
            extension = extensionOf(sourceName);
        }
        String name = normalizeFilename(filenameHint, sourceName, extension);
        String mediaType = resolveMediaType(text(first(descriptor, "mime_type", "mimeType")), extension);
        String resourceId = UUID.randomUUID().toString();
        Path temporary = fileStorage.createTemporary(resourceId);
        String relativePath = null;
        try {
            download(settings, targetUri, temporary);
            long size = Files.size(temporary);
            if (size > MAX_FILE_BYTES) {
                throw new IOException("生成文件超过 50MB 限制");
            }
            relativePath = fileStorage.persist(temporary, resourceId, extension);
            AgentFile file = buildMetadata(resourceId, userId, conversationId, descriptor, toolName, name,
                    extension, mediaType, relativePath, size, sha256(fileStorage.resolve(relativePath)));
            fileMapper.insert(file);
        } catch (IOException | RuntimeException exception) {
            if (relativePath != null) {
                fileStorage.delete(relativePath);
            }
            throw exception;
        } finally {
            Files.deleteIfExists(temporary);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("phase", "available");
        result.put("resourceId", resourceId);
        result.put("name", name);
        result.put("filename", name);
        result.put("extension", extension.startsWith(".") ? extension.substring(1) : extension);
        result.put("mediaType", mediaType);
        result.put("kind", fileKind(extension, mediaType));
        result.put("size", Files.size(fileStorage.resolve(relativePath)));
        result.put("downloadUrl", "/agent/files/" + resourceId);
        result.put("preview", isBrowserPreview(extension, mediaType) ? "browser" : "download");
        // Dify 文本回复可能包含 /files/tools/{filename} 的 Markdown 链接，前端用此字段识别并屏蔽外链跳转。
        if (!sourceName.isBlank()) {
            result.put("sourceFilename", sourceName);
        }
        if (!toolName.isBlank()) {
            result.put("toolName", toolName);
        }
        String sourceFileId = text(first(descriptor, "related_id", "id", "upload_file_id"));
        if (!sourceFileId.isBlank()) {
            result.put("sourceFileId", sourceFileId);
        }
        return result;
    }

    private void download(DifyClientSettings settings, URI uri, Path target) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(uri).timeout(DOWNLOAD_TIMEOUT)
                .header("Authorization", "Bearer " + settings.getApiKey()).GET().build();
        HttpResponse<Path> response = httpClient.send(request, HttpResponse.BodyHandlers.ofFile(target));
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            Files.deleteIfExists(target);
            throw new IOException("Dify 文件下载失败，HTTP " + response.statusCode());
        }
    }

    private URI resolveDifyFileUri(String baseUrl, String fileUrl) {
        URI base = URI.create(baseUrl.replaceAll("/+$", "") + "/");
        URI target = base.resolve(fileUrl);
        boolean sameOrigin = base.getScheme().equalsIgnoreCase(target.getScheme())
                && String.valueOf(base.getHost()).equalsIgnoreCase(String.valueOf(target.getHost()))
                && effectivePort(base) == effectivePort(target);
        if (!sameOrigin || target.getPath() == null || !target.getPath().startsWith("/files/")) {
            throw new IllegalArgumentException("Dify 文件地址不在受信任的文件路径内");
        }
        return target;
    }

    private int effectivePort(URI uri) {
        if (uri.getPort() >= 0)
            return uri.getPort();
        return "https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
    }

    private void captureHints(DifyStreamEvent event, StreamContext context) {
        List<String> fileIds = strings(event.getRaw().get("message_files"));
        List<String> toolNames = event.getTool() == null ? List.of() : splitTools(event.getTool());
        List<String> filenames = outputFilenames(event.getToolInput(), toolNames);
        for (int index = 0; index < toolNames.size(); index++) {
            if (index < filenames.size() && !filenames.get(index).isBlank()) {
                context.nameByTool.put(toolNames.get(index), filenames.get(index));
            }
        }
        for (int index = 0; index < fileIds.size(); index++) {
            String name = index < filenames.size() ? filenames.get(index) : "";
            if (!name.isBlank())
                context.nameByFileId.put(fileIds.get(index), name);
            if (!toolNames.isEmpty())
                context.toolByFileId.put(fileIds.get(index), toolNames.get(Math.min(index, toolNames.size() - 1)));
        }
    }

    private List<String> outputFilenames(String value, List<String> toolNames) {
        Object parsed = parseStructuredValue(value);
        if (!(parsed instanceof Map<?, ?> values))
            return List.of();
        String directFilename = text(values.get("output_filename"));
        if (toolNames.size() == 1 && !directFilename.isBlank())
            return List.of(directFilename);
        List<String> filenames = new ArrayList<>();
        for (String toolName : toolNames) {
            Object nested = values.get(toolName);
            if (nested instanceof Map<?, ?> nestedValues) {
                filenames.add(text(nestedValues.get("output_filename")));
            } else {
                filenames.add("");
            }
        }
        if (filenames.isEmpty())
            filenames.add(text(values.get("output_filename")));
        return filenames;
    }

    private Object parseStructuredValue(String value) {
        if (value == null || value.isBlank())
            return null;
        try {
            return JSON.parse(value);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private List<Map<String, Object>> extractFileList(Object value) {
        Object parsed = value;
        if (value instanceof String text)
            parsed = parseStructuredValue(text);
        if (!(parsed instanceof List<?> list))
            return List.of();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map<?, ?> map)
                result.add(copyMap(map));
        }
        return result;
    }

    private List<String> strings(Object value) {
        if (!(value instanceof List<?> list))
            return List.of();
        List<String> result = new ArrayList<>();
        for (Object item : list) {
            String text = text(item);
            if (!text.isBlank())
                result.add(text);
        }
        return result;
    }

    private List<String> splitTools(String value) {
        List<String> result = new ArrayList<>();
        for (String item : value.split(";"))
            if (!item.isBlank())
                result.add(item.trim());
        return result;
    }

    private Map<String, Object> copyMap(Map<?, ?> source) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (source != null)
            source.forEach((key, value) -> result.put(String.valueOf(key), value));
        return result;
    }

    private Object first(Map<String, Object> map, String... keys) {
        if (map == null)
            return null;
        for (String key : keys) {
            Object value = map.get(key);
            if (value != null && !text(value).isBlank())
                return value;
        }
        return null;
    }

    private String normalizeFilename(String hint, String source, String extension) {
        String value = hint == null || hint.isBlank() ? source : hint;
        value = value == null ? "生成文件" : value;
        value = value.replace('\\', '/');
        value = value.substring(value.lastIndexOf('/') + 1).replaceAll("[\\r\\n\\\\/:*?\"<>|]", "_").trim();
        if (value.isBlank())
            value = "生成文件";
        if (!extension.isBlank() && !value.toLowerCase(Locale.ROOT).endsWith(extension.toLowerCase(Locale.ROOT)))
            value += extension;
        return value;
    }

    private String normalizeExtension(String extension) {
        if (extension == null)
            return "";
        String value = extension.trim().toLowerCase(Locale.ROOT);
        if (!value.isBlank() && !value.startsWith("."))
            value = "." + value;
        return value.matches("\\.[a-z0-9]{1,10}") ? value : "";
    }

    private String extensionOf(String filename) {
        if (filename == null)
            return "";
        int index = filename.lastIndexOf('.');
        return index >= 0 ? normalizeExtension(filename.substring(index)) : "";
    }

    private String resolveMediaType(String source, String extension) {
        if (source != null && !source.isBlank() && !"application/octet-stream".equalsIgnoreCase(source))
            return source;
        return switch (extension) {
            case ".pdf" -> "application/pdf";
            case ".doc", ".docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case ".xls", ".xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case ".ppt", ".pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case ".csv" -> "text/csv";
            case ".txt", ".md" -> "text/plain";
            case ".png" -> "image/png";
            case ".jpg", ".jpeg" -> "image/jpeg";
            case ".svg" -> "image/svg+xml";
            default -> "application/octet-stream";
        };
    }

    private String fileKind(String extension, String mediaType) {
        if (mediaType.startsWith("image/"))
            return "image";
        if ("application/pdf".equals(mediaType))
            return "pdf";
        return switch (extension) {
            case ".xlsx", ".xls", ".csv" -> "spreadsheet";
            case ".docx", ".doc", ".txt", ".md" -> "document";
            case ".pptx", ".ppt" -> "presentation";
            default -> "file";
        };
    }

    private boolean isBrowserPreview(String extension, String mediaType) {
        return "application/pdf".equals(mediaType) || mediaType.startsWith("image/") || extension.equals(".txt")
                || extension.equals(".md");
    }

    private AgentFile buildMetadata(String resourceId, Long userId, String conversationId,
            Map<String, Object> descriptor, String toolName, String name, String extension, String mediaType,
            String relativePath, long size, String hash) {
        AgentFile file = new AgentFile();
        file.setResourceId(resourceId);
        file.setUserId(userId);
        file.setDifyConversationId(conversationId);
        file.setDifyFileId(text(first(descriptor, "related_id", "id", "upload_file_id")));
        file.setToolName(toolName);
        file.setFileName(name);
        file.setStorageName(resourceId + extension);
        file.setRelativePath(relativePath);
        file.setExtension(extension.startsWith(".") ? extension.substring(1) : extension);
        file.setMediaType(mediaType);
        file.setFileKind(fileKind(extension, mediaType));
        file.setFileSize(size);
        file.setFileHash(hash);
        file.setPreviewMode(isBrowserPreview(extension, mediaType) ? "BROWSER" : "DOWNLOAD");
        file.setStatus(AgentFileStatus.AVAILABLE.name());
        file.setCreateBy(String.valueOf(userId));
        file.setUpdateBy(String.valueOf(userId));
        return file;
    }

    private String sha256(Path file) throws IOException {
        if (file == null) {
            throw new IOException("持久化文件不存在");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (var input = Files.newInputStream(file)) {
                byte[] buffer = new byte[8192];
                int length;
                while ((length = input.read(buffer)) >= 0) {
                    digest.update(buffer, 0, length);
                }
            }
            return java.util.HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("当前JDK不支持SHA-256", exception);
        }
    }

    private String text(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    /** 流式文件上下文。 */
    public static final class StreamContext {
        private Map<String, String> nameByFileId = new LinkedHashMap<>();
        private Map<String, String> nameByTool = new LinkedHashMap<>();
        private Map<String, String> toolByFileId = new LinkedHashMap<>();
        private Map<String, Map<String, Object>> pendingFiles = new LinkedHashMap<>();
        private java.util.Set<String> inputFileIds = new java.util.LinkedHashSet<>();
        private List<Map<String, Object>> materializedFiles = Collections.emptyList();
        private boolean materialized;
    }

    /** 文件下载所需的本地资源信息。 */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StoredFile {
        private Path path;
        private String name;
        private String mediaType;
        private boolean browserPreview;
        private long size;

    }
}

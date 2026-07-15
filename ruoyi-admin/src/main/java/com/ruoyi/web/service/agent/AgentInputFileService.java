package com.ruoyi.web.service.agent;

import com.ruoyi.agent.api.AgentInputFileUploadResult;
import com.ruoyi.agent.application.DifyAppConfigService;
import com.ruoyi.agent.domain.enums.DifyAppCode;
import com.ruoyi.agent.infrastructure.dify.DifyClientSettings;
import com.ruoyi.agent.infrastructure.dify.DifyWorkflowClient;
import com.ruoyi.agent.infrastructure.dify.model.DifyFileUploadRequest;
import com.ruoyi.agent.infrastructure.dify.model.DifyFileUploadResult;
import com.ruoyi.common.exception.ServiceException;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/** 校验用户输入附件并上传至当前 Agent 对应的 Dify 应用。 */
@Service
public class AgentInputFileService {
    private static final long MAX_FILE_SIZE = 15L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "png", "jpg", "jpeg", "webp", "pdf", "docx", "xlsx", "pptx", "txt", "csv");
    private static final Map<String, String> FILE_TYPES = Map.ofEntries(
            Map.entry("png", "image"), Map.entry("jpg", "image"), Map.entry("jpeg", "image"),
            Map.entry("webp", "image"), Map.entry("pdf", "document"), Map.entry("docx", "document"),
            Map.entry("xlsx", "document"), Map.entry("pptx", "document"), Map.entry("txt", "document"),
            Map.entry("csv", "document"));

    private final DifyAppConfigService configService;
    private final DifyWorkflowClient difyFileClient;

    public AgentInputFileService(DifyAppConfigService configService, DifyWorkflowClient difyFileClient) {
        this.configService = configService;
        this.difyFileClient = difyFileClient;
    }

    /** 上传一个属于当前用户的聊天输入附件。 */
    public AgentInputFileUploadResult upload(MultipartFile file, Long userId) {
        validate(file);
        String filename = file.getOriginalFilename();
        String extension = extension(filename);
        String mediaType = file.getContentType() == null ? "application/octet-stream" : file.getContentType();
        String user = user(userId);
        try {
            DifyClientSettings settings = configService.requireSettings(DifyAppCode.AGENT_SUPERVISOR.getCode());
            DifyFileUploadResult result = difyFileClient.uploadFile(settings,
                    new DifyFileUploadRequest(filename, mediaType, file.getBytes(), user));
            return new AgentInputFileUploadResult(result.getId(), result.getName(), FILE_TYPES.get(extension),
                    result.getMimeType(), result.getSize());
        } catch (IOException e) {
            throw new ServiceException("附件上传失败，请稍后重试");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServiceException("附件上传已中断");
        }
    }

    /** 生成 Dify 端一致的终端用户标识。 */
    public String user(Long userId) {
        return "ruoyi-user-" + userId;
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ServiceException("请选择需要上传的附件");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ServiceException("单个附件不能超过15MB");
        }
        if (!ALLOWED_EXTENSIONS.contains(extension(file.getOriginalFilename()))) {
            throw new ServiceException("仅支持 PNG、JPG、WEBP、PDF、DOCX、XLSX、PPTX、TXT、CSV 文件");
        }
    }

    private String extension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }
}

package com.ruoyi.web.service.mes;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.web.domain.BomAiImportTrace;
import com.ruoyi.web.domain.dto.BomAiImportSourceFile;
import com.ruoyi.web.domain.dto.BomAiImportedBom;
import com.ruoyi.web.domain.dto.BomAiPreviewResult;
import com.ruoyi.web.domain.enums.BomAiImportTraceStatus;
import com.ruoyi.web.mapper.mes.BomAiImportTraceMapper;
import com.ruoyi.mes.base.domain.BomMaster;
import com.ruoyi.mes.base.domain.BomVersion;
import com.ruoyi.mes.base.mapper.BomMasterMapper;
import com.ruoyi.mes.base.mapper.BomVersionMapper;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/** 管理 BOM AI 导入的原始图纸、识别输出及最终 BOM 关联。 */
@Service
public class BomAiImportTraceService {
    private static final DateTimeFormatter IMPORT_NO_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final BomAiImportTraceMapper traceMapper;
    private final BomAiImportFileStorage fileStorage;
    private final BomMasterMapper bomMasterMapper;
    private final BomVersionMapper bomVersionMapper;

    public BomAiImportTraceService(BomAiImportTraceMapper traceMapper, BomAiImportFileStorage fileStorage,
            BomMasterMapper bomMasterMapper, BomVersionMapper bomVersionMapper) {
        this.traceMapper = traceMapper;
        this.fileStorage = fileStorage;
        this.bomMasterMapper = bomMasterMapper;
        this.bomVersionMapper = bomVersionMapper;
    }

    /** 在调用 Dify 前保存本次上传的原始图纸及初始追溯记录。 */
    public BomAiImportTrace start(MultipartFile[] files, String operator) {
        List<BomAiImportSourceFile> sourceFiles = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            byte[] content;
            try {
                content = file.getBytes();
            } catch (Exception exception) {
                throw new ServiceException("读取原始图纸失败：" + file.getOriginalFilename())
                        .setDetailMessage(exception.getMessage());
            }
            String resourceId = UUID.randomUUID().toString();
            BomAiImportSourceFile sourceFile = new BomAiImportSourceFile();
            sourceFile.setResourceId(resourceId);
            sourceFile.setOriginalFilename(safeFilename(file.getOriginalFilename()));
            sourceFile.setMediaType(file.getContentType());
            sourceFile.setFileSize((long) content.length);
            sourceFile.setFileHash(sha256(content));
            sourceFile.setStoragePath(fileStorage.persist(resourceId, extension(sourceFile.getOriginalFilename()), content));
            sourceFiles.add(sourceFile);
        }
        BomAiImportTrace trace = new BomAiImportTrace();
        trace.setImportNo(nextImportNo());
        trace.setStatus(BomAiImportTraceStatus.RECOGNIZING.name());
        trace.setFileCount(sourceFiles.size());
        trace.setSourceFingerprint(batchFingerprint(sourceFiles));
        trace.setSourceFiles(JSON.toJSONString(sourceFiles));
        trace.setCreateBy(operator);
        trace.setUpdateBy(operator);
        traceMapper.insert(trace);
        return trace;
    }

    /** 记录 Dify 文件 ID、原始输出和解析后的预览结果。 */
    public void markRecognized(BomAiImportTrace trace, List<String> difyFileIds, Map<String, Object> outputs,
            BomAiPreviewResult preview, String operator) {
        List<BomAiImportSourceFile> sourceFiles = parseSourceFiles(trace.getSourceFiles());
        for (int i = 0; i < sourceFiles.size() && i < difyFileIds.size(); i++) {
            sourceFiles.get(i).setDifyFileId(difyFileIds.get(i));
        }
        trace.setStatus(BomAiImportTraceStatus.RECOGNIZED.name());
        trace.setRecognizedBomCount(preview.getDocuments() == null ? 0 : preview.getDocuments().size());
        trace.setSourceFiles(JSON.toJSONString(sourceFiles));
        trace.setRawDifyOutputs(JSON.toJSONString(outputs));
        trace.setPreviewPayload(JSON.toJSONString(preview));
        trace.setRecognizedTime(new Date());
        trace.setUpdateBy(operator);
        traceMapper.update(trace);
    }

    /** 标记识别失败，同时保留已写入的原始图纸。 */
    public void markFailed(BomAiImportTrace trace, String errorMessage, String operator) {
        trace.setStatus(BomAiImportTraceStatus.FAILED.name());
        trace.setErrorMessage(errorMessage);
        trace.setUpdateBy(operator);
        traceMapper.update(trace);
    }

    /** 将确认导入生成的 BOM 关联回识别批次。 */
    public void markImported(Long traceId, List<BomAiImportedBom> importedBoms, String operator, String reimportReason) {
        if (traceId == null) {
            return;
        }
        BomAiImportTrace trace = traceMapper.selectById(traceId);
        if (trace == null) {
            throw new ServiceException("AI 导入追溯记录不存在：" + traceId);
        }
        if (!BomAiImportTraceStatus.IMPORTING.name().equals(trace.getStatus())) {
            throw new ServiceException("当前 AI 导入批次不能确认导入，状态为："
                    + BomAiImportTraceStatus.labelOf(trace.getStatus()));
        }
        List<Long> masterIds = new ArrayList<>();
        List<Long> versionIds = new ArrayList<>();
        for (BomAiImportedBom importedBom : importedBoms) {
            masterIds.add(importedBom.getBomMasterId());
            versionIds.add(importedBom.getBomVersionId());
        }
        trace.setStatus(BomAiImportTraceStatus.IMPORTED.name());
        trace.setImportedBomMasterIds(JSON.toJSONString(masterIds));
        trace.setImportedBomVersionIds(JSON.toJSONString(versionIds));
        trace.setConfirmedTime(new Date());
        if (reimportReason != null && !reimportReason.isBlank()) {
            trace.setRemark("重复导入原因：" + reimportReason.trim());
        }
        trace.setUpdateBy(operator);
        traceMapper.update(trace);
    }

    /** 原子取得确认导入资格，避免双击、重试或多页签重复写入。 */
    public BomAiImportTrace acquireForImport(Long traceId, String operator) {
        if (traceId == null) {
            throw new ServiceException("缺少 AI 导入追溯记录");
        }
        expireStale();
        int updated = traceMapper.transitionStatus(traceId, BomAiImportTraceStatus.RECOGNIZED.name(),
                BomAiImportTraceStatus.IMPORTING.name(), operator);
        BomAiImportTrace trace = selectById(traceId);
        if (updated == 1) {
            return trace;
        }
        if (BomAiImportTraceStatus.IMPORTED.name().equals(trace.getStatus())) {
            return trace;
        }
        throw new ServiceException("当前 AI 导入批次不能确认导入，状态为："
                + BomAiImportTraceStatus.labelOf(trace.getStatus()));
    }

    /** 主动放弃一条待确认记录。 */
    public void cancel(Long traceId, String operator) {
        int updated = traceMapper.transitionStatus(traceId, BomAiImportTraceStatus.RECOGNIZED.name(),
                BomAiImportTraceStatus.CANCELLED.name(), operator);
        if (updated != 1) {
            BomAiImportTrace trace = selectById(traceId);
            if (BomAiImportTraceStatus.CANCELLED.name().equals(trace.getStatus())) {
                return;
            }
            throw new ServiceException("只有待确认的 AI 导入批次可以放弃，当前状态："
                    + BomAiImportTraceStatus.labelOf(trace.getStatus()));
        }
        BomAiImportTrace trace = selectById(traceId);
        trace.setCancelledTime(new Date());
        trace.setUpdateBy(operator);
        traceMapper.update(trace);
    }

    /** 恢复待确认记录中保存的完整预览快照。 */
    public BomAiPreviewResult resume(Long traceId) {
        BomAiImportTrace trace = selectById(traceId);
        if (!BomAiImportTraceStatus.RECOGNIZED.name().equals(trace.getStatus())) {
            throw new ServiceException("只有待确认的 AI 导入批次可以继续确认，当前状态："
                    + BomAiImportTraceStatus.labelOf(trace.getStatus()));
        }
        if (trace.getPreviewPayload() == null || trace.getPreviewPayload().isBlank()) {
            throw new ServiceException("该批次没有可恢复的识别预览");
        }
        return JSON.parseObject(trace.getPreviewPayload(), BomAiPreviewResult.class);
    }

    public BomAiImportTrace findImportedDuplicate(BomAiImportTrace trace) {
        if (trace == null || trace.getSourceFingerprint() == null) {
            return null;
        }
        return traceMapper.selectLatestImportedByFingerprint(trace.getSourceFingerprint(), trace.getId());
    }

    /** 查询一条 AI 导入追溯记录。 */
    public BomAiImportTrace selectById(Long traceId) {
        expireStale();
        BomAiImportTrace trace = traceMapper.selectById(traceId);
        if (trace == null) {
            throw new ServiceException("AI 导入追溯记录不存在：" + traceId);
        }
        hydrateImportedBoms(trace);
        return trace;
    }

    /** 查询 AI 图纸导入追溯记录列表。 */
    public List<BomAiImportTrace> selectList(BomAiImportTrace trace) {
        expireStale();
        return traceMapper.selectList(trace);
    }

    private void expireStale() {
        traceMapper.failStaleRecognizing();
        traceMapper.expireRecognized();
    }

    private void hydrateImportedBoms(BomAiImportTrace trace) {
        if (trace.getImportedBomVersionIds() == null || trace.getImportedBomVersionIds().isBlank()) {
            trace.setImportedBoms(new ArrayList<>());
            return;
        }
        List<Long> versionIds = JSON.parseArray(trace.getImportedBomVersionIds(), Long.class);
        List<BomAiImportedBom> summaries = new ArrayList<>();
        for (Long versionId : versionIds) {
            BomVersion version = bomVersionMapper.selectBomVersionById(versionId);
            if (version == null) {
                continue;
            }
            BomMaster master = bomMasterMapper.selectBomMasterById(version.getBomMasterId());
            if (master == null) {
                continue;
            }
            BomAiImportedBom summary = new BomAiImportedBom();
            summary.setBomMasterId(master.getId());
            summary.setBomVersionId(version.getId());
            summary.setBomCode(master.getBomCode());
            summary.setVersionCode(version.getVersionCode());
            summary.setParentItemCode(master.getParentItemCode());
            summary.setParentItemName(master.getParentItemName());
            summaries.add(summary);
        }
        trace.setImportedBoms(summaries);
    }

    /** 获取某个原始图纸的安全本地路径及元数据。 */
    public StoredSourceFile resolveSourceFile(Long traceId, String resourceId) {
        BomAiImportTrace trace = selectById(traceId);
        BomAiImportSourceFile sourceFile = parseSourceFiles(trace.getSourceFiles()).stream()
                .filter(file -> resourceId.equals(file.getResourceId()))
                .findFirst()
                .orElseThrow(() -> new ServiceException("原始图纸不存在：" + resourceId));
        Path path = fileStorage.resolve(sourceFile.getStoragePath());
        if (path == null) {
            throw new ServiceException("原始图纸文件已丢失：" + sourceFile.getOriginalFilename());
        }
        return new StoredSourceFile(sourceFile, path);
    }

    private List<BomAiImportSourceFile> parseSourceFiles(String sourceFiles) {
        if (sourceFiles == null || sourceFiles.isBlank()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(JSON.parseArray(sourceFiles, BomAiImportSourceFile.class));
    }

    private String nextImportNo() {
        return "BOMAI-" + IMPORT_NO_TIME.format(LocalDateTime.now()) + "-"
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private String safeFilename(String filename) {
        String value = filename == null ? "图纸" : filename.replace('\\', '/');
        value = value.substring(value.lastIndexOf('/') + 1).replaceAll("[\\r\\n\\/:*?\"<>|]", "_").trim();
        return value.isBlank() ? "图纸" : value;
    }

    private String extension(String filename) {
        int index = filename.lastIndexOf('.');
        return index >= 0 ? filename.substring(index).toLowerCase() : ".bin";
    }

    private String sha256(byte[] content) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("当前运行环境不支持 SHA-256", exception);
        }
    }

    private String batchFingerprint(List<BomAiImportSourceFile> sourceFiles) {
        String canonical = sourceFiles.stream()
                .map(BomAiImportSourceFile::getFileHash)
                .filter(hash -> hash != null && !hash.isBlank())
                .sorted(Comparator.naturalOrder())
                .reduce("", (left, right) -> left + right + "\n");
        return sha256(canonical.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    /** 原始图纸下载所需的安全元数据。 */
    public static class StoredSourceFile {
        private final BomAiImportSourceFile sourceFile;
        private final Path path;

        private StoredSourceFile(BomAiImportSourceFile sourceFile, Path path) {
            this.sourceFile = sourceFile;
            this.path = path;
        }

        public BomAiImportSourceFile getSourceFile() {
            return sourceFile;
        }

        public Path getPath() {
            return path;
        }
    }
}

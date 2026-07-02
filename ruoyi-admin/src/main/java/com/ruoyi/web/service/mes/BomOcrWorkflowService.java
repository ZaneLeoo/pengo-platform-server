package com.ruoyi.web.service.mes;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.ruoyi.agent.application.DifyAppConfigService;
import com.ruoyi.agent.domain.enums.DifyAppCode;
import com.ruoyi.agent.infrastructure.dify.DifyClientSettings;
import com.ruoyi.agent.infrastructure.dify.DifyWorkflowClient;
import com.ruoyi.agent.infrastructure.dify.model.DifyFileUploadRequest;
import com.ruoyi.agent.infrastructure.dify.model.DifyFileUploadResult;
import com.ruoyi.agent.infrastructure.dify.model.DifyWorkflowRunRequest;
import com.ruoyi.agent.infrastructure.dify.model.DifyWorkflowRunResult;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.framework.config.ServerConfig;
import com.ruoyi.mes.base.domain.dto.BomImportCreateRequest;
import com.ruoyi.mes.base.domain.dto.BomImportDraft;
import com.ruoyi.mes.base.domain.ocr.BomOcrResult;
import com.ruoyi.mes.base.service.IBomImportService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/** BOM OCR Dify 工作流编排服务。 */
@Service
public class BomOcrWorkflowService
{
    private static final Logger log = LoggerFactory.getLogger(BomOcrWorkflowService.class);
    private static final String DEFAULT_FILE_VARIABLE = "image";
    private static final String DEFAULT_QUERY = "识别这份 BOM 图纸并返回系统约定的 JSON";

    private final DifyAppConfigService difyAppConfigService;
    private final DifyWorkflowClient workflowClient;
    private final IBomImportService bomImportService;
    private final BomOcrWorkflowResultParser resultParser;
    private final ServerConfig serverConfig;
    private final Executor taskExecutor;

    public BomOcrWorkflowService(DifyAppConfigService difyAppConfigService, DifyWorkflowClient workflowClient,
        IBomImportService bomImportService, BomOcrWorkflowResultParser resultParser, ServerConfig serverConfig,
        @Qualifier("threadPoolTaskExecutor") Executor taskExecutor)
    {
        this.difyAppConfigService = difyAppConfigService;
        this.workflowClient = workflowClient;
        this.bomImportService = bomImportService;
        this.resultParser = resultParser;
        this.serverConfig = serverConfig;
        this.taskExecutor = taskExecutor;
    }

    /** 创建识别任务并在后台调用 BOM_OCR 工作流。 */
    public BomImportDraft recognize(MultipartFile file, String fileVariable, String query, String inputsJson,
        Long userId, String username)
    {
        if (file == null || file.isEmpty())
        {
            throw new ServiceException("请上传 BOM 图纸文件");
        }
        try
        {
            OcrFile ocrFile = new OcrFile(file.getOriginalFilename(), contentType(file), difyFileType(file),
                file.getBytes());
            String localFileName = FileUploadUtils.upload(RuoYiConfig.getUploadPath(), file);
            String fileUrl = serverConfig.getUrl() + localFileName;
            BomImportCreateRequest request = createRequest(ocrFile, localFileName, fileUrl, null);
            BomImportDraft draft = bomImportService.createProcessingDraft(request, username);
            taskExecutor.execute(() -> runRecognition(draft.getId(), ocrFile, localFileName, fileUrl, fileVariable,
                query, inputsJson, userId, username));
            return draft;
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ServiceException("BOM OCR 识别任务创建失败：" + e.getMessage());
        }
    }

    private void runRecognition(Long draftId, OcrFile file, String localFileName, String fileUrl, String fileVariable,
        String query, String inputsJson, Long userId, String username)
    {
        try
        {
            DifyClientSettings settings = difyAppConfigService.requireSettings(DifyAppCode.BOM_OCR.getCode());
            String difyUser = "ruoyi-user-" + userId;
            DifyFileUploadResult uploadResult = workflowClient.uploadFile(settings, new DifyFileUploadRequest(
                file.originalFilename(), file.contentType(), file.content(), difyUser));
            Map<String, Object> inputs = buildInputs(file, uploadResult, fileVariable, query, inputsJson);
            log.debug("BOM OCR Dify workflow inputs: {}", JSON.toJSONString(inputs));
            DifyWorkflowRunResult runResult = workflowClient.runStreaming(settings,
                new DifyWorkflowRunRequest(inputs, difyUser));
            requireWorkflowSucceeded(runResult);
            BomOcrResult ocrResult = resultParser.parse(runResult.outputs());
            bomImportService.completeRecognition(draftId, createRequest(file, localFileName, fileUrl, ocrResult),
                username);
        }
        catch (Exception e)
        {
            log.warn("BOM OCR recognition task failed, draftId={}", draftId, e);
            bomImportService.markRecognitionFailed(draftId, "BOM OCR 识别失败：" + e.getMessage(), username);
        }
    }

    private Map<String, Object> buildInputs(OcrFile file, DifyFileUploadResult uploadResult, String fileVariable,
        String query, String inputsJson)
    {
        Map<String, Object> inputs = parseInputs(inputsJson);
        inputs.put(StringUtils.isBlank(fileVariable) ? DEFAULT_FILE_VARIABLE : fileVariable, fileObject(file,
            uploadResult));
        if (StringUtils.isNotBlank(query))
        {
            inputs.put("query", query);
        }
        else if (!inputs.containsKey("query"))
        {
            inputs.put("query", DEFAULT_QUERY);
        }
        return inputs;
    }

    private Map<String, Object> parseInputs(String inputsJson)
    {
        if (StringUtils.isBlank(inputsJson))
        {
            return new LinkedHashMap<>();
        }
        try
        {
            return JSON.parseObject(inputsJson, new TypeReference<LinkedHashMap<String, Object>>() {});
        }
        catch (Exception e)
        {
            throw new ServiceException("inputs 必须是合法 JSON 对象");
        }
    }

    private Map<String, Object> fileObject(OcrFile file, DifyFileUploadResult uploadResult)
    {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("type", file.difyFileType());
        value.put("transfer_method", "local_file");
        value.put("upload_file_id", uploadResult.id());
        return value;
    }

    private void requireWorkflowSucceeded(DifyWorkflowRunResult runResult)
    {
        if (runResult == null)
        {
            throw new ServiceException("Dify 工作流无返回");
        }
        if (runResult.status() != null && !"succeeded".equals(runResult.status()))
        {
            throw new ServiceException("Dify 工作流执行失败：" + (runResult.error() == null ? runResult.status()
                : runResult.error()));
        }
    }

    private BomImportCreateRequest createRequest(OcrFile file, String localFileName, String fileUrl,
        BomOcrResult result)
    {
        BomImportCreateRequest request = new BomImportCreateRequest();
        request.setFileName(StringUtils.isBlank(file.originalFilename()) ? localFileName : file.originalFilename());
        request.setFileUrl(fileUrl);
        request.setFileType(file.difyFileType());
        request.setResult(result);
        return request;
    }

    private String difyFileType(MultipartFile file)
    {
        String extension = FileUploadUtils.getExtension(file);
        if (List.of("jpg", "jpeg", "png", "gif", "webp", "bmp").contains(extension.toLowerCase()))
        {
            return "image";
        }
        if (List.of("mp4", "mov", "mpeg", "webm").contains(extension.toLowerCase()))
        {
            return "video";
        }
        return "document";
    }

    private String contentType(MultipartFile file)
    {
        return StringUtils.isBlank(file.getContentType()) ? "application/octet-stream" : file.getContentType();
    }

    private record OcrFile(String originalFilename, String contentType, String difyFileType, byte[] content) { }
}

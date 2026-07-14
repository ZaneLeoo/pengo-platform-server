package com.ruoyi.web.controller.agent;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.web.service.agent.AgentFileService;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 当前登录用户的 Agent 生成文件下载和预览入口。 */
@RestController
@RequestMapping("/agent/files")
public class AgentFileController extends BaseController {
    private final AgentFileService fileService;

    public AgentFileController(AgentFileService fileService) {
        this.fileService = fileService;
    }

    /** 下载或内联预览一个当前用户拥有的生成文件。 */
    @GetMapping("/{resourceId}")
    public void download(@PathVariable String resourceId, HttpServletResponse response) throws IOException {
        AgentFileService.StoredFile file = fileService.findOwned(resourceId, getUserId());
        if (file == null) {
            response.sendError(HttpStatus.NOT_FOUND.value(), "文件不存在或已过期");
            return;
        }
        response.setContentType(file.getMediaType());
        response.setContentLengthLong(file.getSize());
        ContentDisposition disposition = file.isBrowserPreview()
                ? ContentDisposition.inline().filename(file.getName(), StandardCharsets.UTF_8).build()
                : ContentDisposition.attachment().filename(file.getName(), StandardCharsets.UTF_8).build();
        response.setHeader("Content-Disposition", disposition.toString());
        Files.copy(file.getPath(), response.getOutputStream());
    }

    /** 查询当前用户持久化的生成文件。 */
    @GetMapping
    public AjaxResult list() {
        return success(fileService.listOwned(getUserId()));
    }

    /** 删除当前用户拥有的生成文件。 */
    @DeleteMapping("/{resourceId}")
    public AjaxResult delete(@PathVariable String resourceId) throws IOException {
        return toAjax(fileService.deleteOwned(resourceId, getUserId(), getUsername()));
    }
}

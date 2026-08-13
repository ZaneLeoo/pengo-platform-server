package com.ruoyi.web.controller.flow;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.flow.message.service.FlowMessageService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 流程站内消息接口。
 */
@RestController
@RequestMapping("/flow/message")
public class FlowMessageController extends BaseController {

    private final FlowMessageService service;

    public FlowMessageController(FlowMessageService service) {
        this.service = service;
    }

    /** 查询我的消息列表。 */
    @PreAuthorize("@ss.hasPermi('flow:message:list')")
    @GetMapping("/list")
    public AjaxResult list(@RequestParam(required = false) String readFlag) {
        return success(service.list(getUsername(), readFlag));
    }

    /** 我的未读消息数。 */
    @PreAuthorize("@ss.hasPermi('flow:message:list')")
    @GetMapping("/unread-count")
    public AjaxResult unreadCount() {
        return success(service.unreadCount(getUsername()));
    }

    /** 标记单条已读。 */
    @PreAuthorize("@ss.hasPermi('flow:message:list')")
    @PutMapping("/{messageId}/read")
    public AjaxResult read(@PathVariable Long messageId) {
        return toAjax(service.read(messageId, getUsername()));
    }

    /** 全部标记已读。 */
    @PreAuthorize("@ss.hasPermi('flow:message:list')")
    @PutMapping("/read-all")
    public AjaxResult readAll() {
        return toAjax(service.readAll(getUsername()));
    }
}

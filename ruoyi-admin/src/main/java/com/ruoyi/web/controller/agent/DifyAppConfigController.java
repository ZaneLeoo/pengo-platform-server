package com.ruoyi.web.controller.agent;

import com.ruoyi.agent.application.DifyAppConfigService;
import com.ruoyi.agent.domain.DifyAppConfig;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Dify 应用配置控制器。 */
@RestController
@RequestMapping("/agent/difyApps")
public class DifyAppConfigController extends BaseController {
    private final DifyAppConfigService configService;

    public DifyAppConfigController(DifyAppConfigService configService) {
        this.configService = configService;
    }

    /** 查询 Dify 应用配置列表。 */
    @PreAuthorize("@ss.hasPermi('agent:difyApp:list')")
    @GetMapping("/list")
    public TableDataInfo list(DifyAppConfig config) {
        startPage();
        List<DifyAppConfig> list = configService.selectDifyAppConfigList(config);
        list.forEach(this::maskApiKey);
        return getDataTable(list);
    }

    /** 获取 Dify 应用配置详情。 */
    @PreAuthorize("@ss.hasPermi('agent:difyApp:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        DifyAppConfig config = configService.selectDifyAppConfigById(id);
        maskApiKey(config);
        return success(config);
    }

    /** 新增 Dify 应用配置。 */
    @PreAuthorize("@ss.hasPermi('agent:difyApp:add')")
    @Log(title = "Dify应用配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody DifyAppConfig config) {
        config.setCreateBy(getUsername());
        return toAjax(configService.insertDifyAppConfig(config));
    }

    /** 修改 Dify 应用配置。 */
    @PreAuthorize("@ss.hasPermi('agent:difyApp:edit')")
    @Log(title = "Dify应用配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody DifyAppConfig config) {
        config.setUpdateBy(getUsername());
        return toAjax(configService.updateDifyAppConfig(config));
    }

    /** 删除 Dify 应用配置。 */
    @PreAuthorize("@ss.hasPermi('agent:difyApp:remove')")
    @Log(title = "Dify应用配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(configService.deleteDifyAppConfigByIds(ids));
    }

    private void maskApiKey(DifyAppConfig config) {
        if (config != null && config.getApiKey() != null && !config.getApiKey().isBlank()) {
            config.setApiKey(DifyAppConfigService.MASKED_API_KEY);
        }
    }
}

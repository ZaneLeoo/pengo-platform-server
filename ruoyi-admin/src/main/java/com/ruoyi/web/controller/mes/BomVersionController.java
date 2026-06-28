package com.ruoyi.web.controller.mes;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.mes.base.domain.BomVersion;
import com.ruoyi.mes.base.service.IBomVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * BOM版本控制器。
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mes/base/bomVersion")
public class BomVersionController extends BaseController {
    @Autowired
    private IBomVersionService bomVersionService;

    /**
     * 查询BOM版本列表。
     */
    @PreAuthorize("@ss.hasPermi('mes:bomVersion:list')")
    @GetMapping("/list")
    public TableDataInfo list(BomVersion bomVersion) {
        startPage();
        return getDataTable(bomVersionService.selectBomVersionList(bomVersion));
    }

    /**
     * 获取BOM版本详情。
     */
    @PreAuthorize("@ss.hasPermi('mes:bomVersion:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return success(bomVersionService.selectBomVersionById(id));
    }

    /**
     * BOM版本完整性检查。
     */
    @PreAuthorize("@ss.hasPermi('mes:bomVersion:query')")
    @GetMapping("/{id}/check")
    public AjaxResult check(@PathVariable Long id) {
        return success(bomVersionService.checkBomVersion(id));
    }

    /**
     * 新增BOM版本。
     */
    @PreAuthorize("@ss.hasPermi('mes:bomVersion:add')")
    @Log(title = "BOM版本", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody BomVersion bomVersion) {
        if (!bomVersionService.checkVersionCodeUnique(bomVersion)) {
            return error("新增BOM版本'" + bomVersion.getVersionCode() + "'失败，版本号已存在");
        }
        bomVersion.setCreateBy(getUsername());
        return toAjax(bomVersionService.insertBomVersion(bomVersion));
    }

    /**
     * 修改BOM版本。
     */
    @PreAuthorize("@ss.hasPermi('mes:bomVersion:edit')")
    @Log(title = "BOM版本", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody BomVersion bomVersion) {
        if (!bomVersionService.checkVersionCodeUnique(bomVersion)) {
            return error("修改BOM版本'" + bomVersion.getVersionCode() + "'失败，版本号已存在");
        }
        bomVersion.setUpdateBy(getUsername());
        return toAjax(bomVersionService.updateBomVersion(bomVersion));
    }

    /**
     * 删除BOM版本。
     */
    @PreAuthorize("@ss.hasPermi('mes:bomVersion:remove')")
    @Log(title = "BOM版本", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(bomVersionService.deleteBomVersionByIds(ids));
    }

    /**
     * 复制BOM版本及关联子件明细。
     */
    @PreAuthorize("@ss.hasPermi('mes:bomVersion:add')")
    @Log(title = "BOM版本", businessType = BusinessType.INSERT)
    @PostMapping("/copy")
    public AjaxResult copy(@RequestBody java.util.Map<String, Object> params) {
        Object sourceIdObj = params.get("sourceVersionId");
        Object codeObj = params.get("targetVersionCode");
        Object nameObj = params.get("targetVersionName");

        if (sourceIdObj == null || codeObj == null) {
            return error("源版本ID和目标版本号不能为空");
        }

        Long sourceVersionId = Long.valueOf(sourceIdObj.toString());
        String targetVersionCode = codeObj.toString();
        String targetVersionName = nameObj != null ? nameObj.toString() : "";

        bomVersionService.copyBomVersion(sourceVersionId, targetVersionCode, targetVersionName, getUsername());
        return success();
    }
}

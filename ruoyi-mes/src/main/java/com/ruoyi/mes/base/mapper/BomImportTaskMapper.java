package com.ruoyi.mes.base.mapper;

import com.ruoyi.mes.base.domain.BomImportTask;
import java.util.List;

/** BOM OCR 导入任务数据访问接口。 */
public interface BomImportTaskMapper {
    List<BomImportTask> selectBomImportTaskList(BomImportTask task);
    BomImportTask selectBomImportTaskById(Long id);
    int insertBomImportTask(BomImportTask task);
    int updateBomImportTask(BomImportTask task);
    int deleteBomImportTaskByIds(Long[] ids);
}

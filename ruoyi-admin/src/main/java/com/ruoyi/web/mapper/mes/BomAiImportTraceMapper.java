package com.ruoyi.web.mapper.mes;

import com.ruoyi.web.domain.BomAiImportTrace;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/** BOM AI 图纸导入追溯记录数据访问。 */
@Mapper
public interface BomAiImportTraceMapper {
    int insert(BomAiImportTrace trace);

    int update(BomAiImportTrace trace);

    BomAiImportTrace selectById(Long id);

    List<BomAiImportTrace> selectList(BomAiImportTrace trace);
}

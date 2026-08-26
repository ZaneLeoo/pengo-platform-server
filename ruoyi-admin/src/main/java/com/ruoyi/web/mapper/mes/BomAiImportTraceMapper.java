package com.ruoyi.web.mapper.mes;

import com.ruoyi.web.domain.BomAiImportTrace;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** BOM AI 图纸导入追溯记录数据访问。 */
@Mapper
public interface BomAiImportTraceMapper {
    int insert(BomAiImportTrace trace);

    int update(BomAiImportTrace trace);

    int transitionStatus(
            @Param("id") Long id,
            @Param("expectedStatus") String expectedStatus,
            @Param("targetStatus") String targetStatus,
            @Param("operator") String operator);

    int expireRecognized();

    int failStaleRecognizing();

    BomAiImportTrace selectById(Long id);

    List<BomAiImportTrace> selectImportedByFingerprint(
            @Param("sourceFingerprint") String sourceFingerprint,
            @Param("excludeId") Long excludeId);

    List<BomAiImportTrace> selectList(BomAiImportTrace trace);
}

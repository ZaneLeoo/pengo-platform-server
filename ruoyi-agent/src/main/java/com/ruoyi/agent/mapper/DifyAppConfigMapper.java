package com.ruoyi.agent.mapper;

import com.ruoyi.agent.domain.DifyAppConfig;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/** Dify 应用配置数据访问。 */
public interface DifyAppConfigMapper
{
    /** 查询配置列表。 */
    List<DifyAppConfig> selectDifyAppConfigList(DifyAppConfig config);

    /** 按 ID 查询配置。 */
    DifyAppConfig selectDifyAppConfigById(@Param("id") Long id);

    /** 按应用编码查询配置。 */
    DifyAppConfig selectDifyAppConfigByCode(@Param("appCode") String appCode);

    /** 新增配置。 */
    int insertDifyAppConfig(DifyAppConfig config);

    /** 更新配置。 */
    int updateDifyAppConfig(DifyAppConfig config);

    /** 批量删除配置。 */
    int deleteDifyAppConfigByIds(Long[] ids);
}

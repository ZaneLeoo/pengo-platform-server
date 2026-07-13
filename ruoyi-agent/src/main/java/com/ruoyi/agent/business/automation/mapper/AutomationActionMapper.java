package com.ruoyi.agent.business.automation.mapper;

import com.ruoyi.agent.business.automation.domain.AutomationAction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** AI 自动化动作幂等记录访问接口。 */
@Mapper
public interface AutomationActionMapper
{
    AutomationAction selectByActionKey(@Param("actionKey") String actionKey);

    int insert(AutomationAction action);

    int complete(@Param("actionKey") String actionKey, @Param("targetId") Long targetId,
                 @Param("targetCode") String targetCode);
}

package com.ruoyi.web.service.agent.tool;

import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.service.ISysMenuService;
import java.util.Set;
import org.springframework.stereotype.Service;

/** 在无浏览器 SecurityContext 的 Dify 工具回调中重新校验发起用户权限。 */
@Service
public class AgentToolPermissionService
{
    private final ISysMenuService menuService;

    public AgentToolPermissionService(ISysMenuService menuService)
    {
        this.menuService = menuService;
    }

    /** 要求运行所属用户仍然具备指定权限。 */
    public void require(Long userId, String permission)
    {
        if (SecurityUtils.isAdmin(userId))
        {
            return;
        }
        Set<String> permissions = menuService.selectMenuPermsByUserId(userId);
        if (permissions == null || (!permissions.contains(Constants.ALL_PERMISSION) && !permissions.contains(permission)))
        {
            throw new ServiceException("当前用户无权调用该业务能力");
        }
    }
}

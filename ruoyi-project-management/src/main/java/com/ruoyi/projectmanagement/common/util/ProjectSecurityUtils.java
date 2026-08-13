package com.ruoyi.projectmanagement.common.util;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

/**
 * 项目管理通用操作者校验。
 */
public final class ProjectSecurityUtils {

    private ProjectSecurityUtils() {
    }

    /**
     * 校验操作者是否为admin或指定负责人（编码不区分大小写）。
     *
     * @param ownerCode 负责人编码，可为空
     * @param operator  当前操作者
     * @param message   校验失败时的异常信息
     */
    public static void assertAdminOrOwner(String ownerCode, String operator, String message) {
        if (!"admin".equalsIgnoreCase(operator)
                && (StringUtils.isBlank(ownerCode) || !ownerCode.equalsIgnoreCase(operator))) {
            throw new ServiceException(message);
        }
    }
}

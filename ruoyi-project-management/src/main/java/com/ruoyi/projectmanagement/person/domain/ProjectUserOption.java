package com.ruoyi.projectmanagement.person.domain;

import lombok.Data;

/** 可绑定的系统登录账号选项。 */
@Data
public class ProjectUserOption {
    private Long userId;
    private String userName;
    private String nickName;
    private String status;
}

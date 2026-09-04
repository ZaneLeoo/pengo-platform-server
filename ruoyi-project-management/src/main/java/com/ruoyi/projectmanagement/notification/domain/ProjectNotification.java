package com.ruoyi.projectmanagement.notification.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.Data;

/** 发送给指定系统用户的项目站内通知。 */
@Data
public class ProjectNotification {
    private Long notificationId;
    private Long userId;
    private String notificationType;
    private String businessType;
    private Long businessId;
    private String title;
    private String content;
    private String targetPath;
    private String readFlag;
    private String dedupeKey;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime readTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createTime;
}

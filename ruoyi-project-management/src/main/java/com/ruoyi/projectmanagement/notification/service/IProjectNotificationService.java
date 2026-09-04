package com.ruoyi.projectmanagement.notification.service;

import com.ruoyi.projectmanagement.notification.domain.ProjectNotification;
import com.ruoyi.projectmanagement.notification.domain.ProjectTodoItem;
import java.util.List;
import java.util.Map;

public interface IProjectNotificationService {
    List<ProjectNotification> list(Long userId, String readFlag);

    List<ProjectTodoItem> todos(Long userId);

    Map<String, Integer> summary(Long userId);

    int markRead(Long notificationId, Long userId);

    int markAllRead(Long userId);

    void notifyProjectStarted(Long projectId, String projectName);
}

package com.ruoyi.projectmanagement.notification.service.impl;

import com.ruoyi.projectmanagement.notification.domain.ProjectNotification;
import com.ruoyi.projectmanagement.notification.domain.ProjectTodoItem;
import com.ruoyi.projectmanagement.notification.domain.TaskAssignmentSummary;
import com.ruoyi.projectmanagement.notification.mapper.ProjectNotificationMapper;
import com.ruoyi.projectmanagement.notification.service.IProjectNotificationService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 个人待办与站内通知服务。 */
@Service
public class ProjectNotificationServiceImpl implements IProjectNotificationService {
    private final ProjectNotificationMapper mapper;

    public ProjectNotificationServiceImpl(ProjectNotificationMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<ProjectNotification> list(Long userId, String readFlag) {
        return mapper.selectList(userId, readFlag);
    }

    @Override
    public List<ProjectTodoItem> todos(Long userId) {
        return mapper.selectTodos(userId);
    }

    @Override
    public Map<String, Integer> summary(Long userId) {
        Map<String, Integer> result = new LinkedHashMap<>();
        int todoCount = mapper.countTodos(userId);
        int unreadCount = mapper.countUnread(userId);
        result.put("todoCount", todoCount);
        result.put("unreadCount", unreadCount);
        result.put("totalCount", todoCount + unreadCount);
        return result;
    }

    @Override
    @Transactional
    public int markRead(Long notificationId, Long userId) {
        return mapper.markRead(notificationId, userId);
    }

    @Override
    @Transactional
    public int markAllRead(Long userId) {
        return mapper.markAllRead(userId);
    }

    /** 项目首次启动后，每位任务执行人只收到一条汇总通知。唯一键保证重复调用不重复发送。 */
    @Override
    @Transactional
    public void notifyProjectStarted(Long projectId, String projectName) {
        for (TaskAssignmentSummary assignment : mapper.selectTaskAssignmentSummaries(projectId)) {
            ProjectNotification notification = new ProjectNotification();
            notification.setUserId(assignment.getUserId());
            notification.setNotificationType("TASK_ASSIGNMENT");
            notification.setBusinessType("PROJECT_START");
            notification.setBusinessId(projectId);
            notification.setTitle("项目已启动，任务已分配");
            notification.setContent(buildTaskAssignmentContent(projectName, assignment));
            notification.setTargetPath("/personal/my-task?projectId=" + projectId);
            notification.setReadFlag("0");
            notification.setDedupeKey("PROJECT_START:" + projectId + ":" + assignment.getUserId());
            mapper.insert(notification);
        }
    }

    private String buildTaskAssignmentContent(
            String projectName, TaskAssignmentSummary assignment) {
        StringBuilder content =
                new StringBuilder("项目“")
                        .append(projectName)
                        .append("”已启动，共有 ")
                        .append(assignment.getTaskCount())
                        .append(" 项执行任务分配给你");
        if (assignment.getFirstStartDate() != null && assignment.getLastEndDate() != null) {
            content.append("，计划周期 ")
                    .append(assignment.getFirstStartDate())
                    .append(" 至 ")
                    .append(assignment.getLastEndDate());
        }
        return content.append("。请及时查看并推进。").toString();
    }
}

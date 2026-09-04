package com.ruoyi.projectmanagement.notification.mapper;

import com.ruoyi.projectmanagement.notification.domain.ProjectNotification;
import com.ruoyi.projectmanagement.notification.domain.ProjectTodoItem;
import com.ruoyi.projectmanagement.notification.domain.TaskAssignmentSummary;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProjectNotificationMapper {
    List<ProjectNotification> selectList(
            @Param("userId") Long userId, @Param("readFlag") String readFlag);

    int countUnread(Long userId);

    int insert(ProjectNotification notification);

    int markRead(@Param("notificationId") Long notificationId, @Param("userId") Long userId);

    int markAllRead(Long userId);

    List<ProjectTodoItem> selectTodos(Long userId);

    int countTodos(Long userId);

    List<TaskAssignmentSummary> selectTaskAssignmentSummaries(Long projectId);
}

package com.ruoyi.projectmanagement.workhours.service;

import com.ruoyi.projectmanagement.task.domain.ProjectTask;
import com.ruoyi.projectmanagement.workhours.domain.ProjectLaborRate;
import com.ruoyi.projectmanagement.workhours.domain.ProjectWorkHoursEntry;
import com.ruoyi.projectmanagement.workhours.domain.ProjectWorkHoursSheet;
import java.time.LocalDate;
import java.util.List;

public interface IProjectWorkHoursService {
    List<ProjectWorkHoursSheet> mySheets(Long userId);

    ProjectWorkHoursSheet mySheet(Long userId, LocalDate weekStartDate);

    ProjectWorkHoursSheet save(Long userId, String userName, ProjectWorkHoursSheet sheet);

    void submit(Long userId, String userName, Long sheetId);

    void withdraw(Long userId, String userName, Long sheetId);

    ProjectWorkHoursSheet correction(Long userId, String userName, Long entryId);

    List<ProjectTask> eligibleTasks(Long userId);

    List<ProjectWorkHoursEntry> manage(ProjectWorkHoursEntry filter, Long userId);

    List<ProjectLaborRate> rates(ProjectLaborRate filter);

    ProjectLaborRate saveRate(ProjectLaborRate rate, String operator);
}

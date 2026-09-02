package com.ruoyi.projectmanagement.workhours.mapper;

import com.ruoyi.projectmanagement.workhours.domain.ProjectLaborRate;
import com.ruoyi.projectmanagement.workhours.domain.ProjectWorkHoursEntry;
import com.ruoyi.projectmanagement.workhours.domain.ProjectWorkHoursSheet;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProjectWorkHoursMapper {
    ProjectWorkHoursSheet selectSheet(@Param("sheetId") Long sheetId);
    ProjectWorkHoursSheet selectSheetByUserWeek(@Param("userId") Long userId, @Param("weekStartDate") LocalDate weekStartDate);
    ProjectWorkHoursSheet selectCorrectionSheetByUserWeek(@Param("userId") Long userId, @Param("weekStartDate") LocalDate weekStartDate);
    List<ProjectWorkHoursSheet> selectMySheets(@Param("userId") Long userId);
    List<ProjectWorkHoursEntry> selectEntries(@Param("sheetId") Long sheetId);
    ProjectWorkHoursEntry selectEntry(@Param("entryId") Long entryId);
    List<ProjectWorkHoursEntry> selectManage(ProjectWorkHoursEntry filter);
    int insertSheet(ProjectWorkHoursSheet sheet);
    int updateSheet(ProjectWorkHoursSheet sheet);
    int updateSheetStatus(ProjectWorkHoursSheet sheet);
    int deleteEntries(Long sheetId);
    int insertEntry(ProjectWorkHoursEntry entry);
    int updateEntryArchive(ProjectWorkHoursEntry entry);
    int reverseEntry(@Param("entryId") Long entryId, @Param("operator") String operator);
    int updateTaskActualHours(@Param("taskId") Long taskId);
    List<ProjectLaborRate> selectRates(ProjectLaborRate filter);
    ProjectLaborRate selectRate(@Param("userId") Long userId, @Param("workDate") LocalDate workDate);
    ProjectLaborRate selectRateById(Long rateId);
    int insertRate(ProjectLaborRate rate);
    int updateRate(ProjectLaborRate rate);
    int countRateOverlap(ProjectLaborRate rate);
}

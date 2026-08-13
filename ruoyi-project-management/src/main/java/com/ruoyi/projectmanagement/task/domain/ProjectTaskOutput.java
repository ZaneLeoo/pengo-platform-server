package com.ruoyi.projectmanagement.task.domain;
import com.ruoyi.common.core.domain.BaseEntity;import jakarta.validation.constraints.NotBlank;import jakarta.validation.constraints.NotNull;import lombok.Data;import lombok.EqualsAndHashCode;
@Data @EqualsAndHashCode(callSuper=true) public class ProjectTaskOutput extends BaseEntity {private Long outputId;@NotNull(message="所属任务不能为空")private Long taskId;@NotBlank(message="成果名称不能为空")private String outputName;@NotBlank(message="请上传成果文件")private String fileUrl;private String description;}

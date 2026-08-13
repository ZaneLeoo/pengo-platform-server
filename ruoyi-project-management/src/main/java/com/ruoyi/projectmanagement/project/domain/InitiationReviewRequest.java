package com.ruoyi.projectmanagement.project.domain;
import jakarta.validation.constraints.NotBlank;import lombok.Data;
@Data public class InitiationReviewRequest {@NotBlank private String result;private String comment;}

package com.admitgenius.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class EducationDTO {
    private Long id;

    @NotBlank(message = "学校名称不能为空")
    @Size(max = 200, message = "学校名称长度不能超过200个字符")
    private String school;

    @NotBlank(message = "学位不能为空")
    @Size(max = 50, message = "学位长度不能超过50个字符")
    private String degree;

    @NotBlank(message = "专业不能为空")
    @Size(max = 100, message = "专业长度不能超过100个字符")
    private String major;

    @NotBlank(message = "开始时间不能为空")
    @Size(min = 7, max = 7, message = "时间格式应为YYYY-MM")
    private String startDate;

    @NotBlank(message = "结束时间不能为空")
    @Size(min = 7, max = 7, message = "时间格式应为YYYY-MM")
    private String endDate;

    @Size(max = 1000, message = "主要成就长度不能超过1000个字符")
    private String achievement;
}
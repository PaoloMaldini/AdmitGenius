package com.admitgenius.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

@Data
public class ProjectDTO {
    private Long id;

    @NotBlank(message = "项目名称不能为空")
    @Size(max = 200, message = "项目名称长度不能超过200个字符")
    private String name;

    @NotBlank(message = "开始时间不能为空")
    @Size(min = 7, max = 7, message = "时间格式应为YYYY-MM")
    private String startDate;

    @NotBlank(message = "结束时间不能为空")
    @Size(min = 7, max = 7, message = "时间格式应为YYYY-MM")
    private String endDate;

    @Size(max = 1000, message = "项目内容长度不能超过1000个字符")
    private String content;

    @NotNull(message = "是否为负责人不能为空")
    private Boolean isLeader = false;
}
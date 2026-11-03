package com.exam.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PaperManualDTO {

    @NotBlank(message = "试卷名称不能为空")
    private String paperName;

    @NotNull(message = "请选择课程")
    private Integer courseId;

    @NotNull(message = "请填写考试时长")
    private Integer duration;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @NotEmpty(message = "请至少选择一道题目")
    private List<Integer> questionIds;
}

package com.exam.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PaperAutoDTO {

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

    private Integer singleCount = 0;

    private Integer multiCount = 0;

    private Integer judgeCount = 0;

    private Integer difficulty;
}

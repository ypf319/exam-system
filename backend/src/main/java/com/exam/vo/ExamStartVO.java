package com.exam.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ExamStartVO {

    private Integer recordId;

    private Integer paperId;

    private String paperName;

    private Integer totalScore;

    private Integer duration;

    private Long remainSeconds;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    private List<PaperQuestionVO> questions;

    private String savedAnswer;
}

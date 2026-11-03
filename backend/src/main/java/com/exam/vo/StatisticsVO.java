package com.exam.vo;

import lombok.Data;

@Data
public class StatisticsVO {

    private Integer paperId;

    private String paperName;

    private Integer totalScore;

    private Integer studentCount;

    private Integer submittedCount;

    private Integer maxScore;

    private Integer minScore;

    private Double avgScore;

    private Double passRate;
}

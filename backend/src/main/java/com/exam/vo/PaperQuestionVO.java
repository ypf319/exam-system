package com.exam.vo;

import lombok.Data;

@Data
public class PaperQuestionVO {

    private Integer questionId;

    private String title;

    private Integer questionType;

    private String optionA;

    private String optionB;

    private String optionC;

    private String optionD;

    private String answer;

    private Integer score;

    private Integer difficulty;

    private Integer courseId;
}

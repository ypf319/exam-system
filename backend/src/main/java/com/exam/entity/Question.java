package com.exam.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Question {

    private Integer id;

    private Integer courseId;

    private String title;

    private Integer questionType;

    private String optionA;

    private String optionB;

    private String optionC;

    private String optionD;

    private String answer;

    private Integer score;

    private Integer difficulty;

    private Integer createBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    private String courseName;
}

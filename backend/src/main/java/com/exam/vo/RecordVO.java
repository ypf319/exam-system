package com.exam.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RecordVO {

    private Integer id;

    private Integer paperId;

    private String paperName;

    private String courseName;

    private Integer studentId;

    private String studentName;

    private String username;

    private String studentAnswer;

    private Integer score;

    private Integer totalScore;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submitTime;

    private Integer status;
}

package com.exam.entity;

import lombok.Data;

@Data
public class PaperQuestion {

    private Integer id;

    private Integer paperId;

    private Integer questionId;

    private Integer score;
}

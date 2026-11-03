package com.exam.vo;

import com.exam.entity.Paper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class PaperDetailVO extends Paper {

    private List<PaperQuestionVO> questions;
}

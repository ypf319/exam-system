package com.exam.vo;

import com.exam.entity.Paper;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AvailablePaperVO extends Paper {

    private Integer recordId;

    private Integer examStatus;

    private Integer score;
}

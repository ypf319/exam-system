package com.exam.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class SubmitDTO {

    @NotNull(message = "答卷记录不能为空")
    private Integer recordId;

    private Map<Integer, String> answers;
}

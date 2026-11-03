package com.exam.service;

import com.exam.dto.SubmitDTO;
import com.exam.vo.AvailablePaperVO;
import com.exam.vo.ExamStartVO;

import java.util.List;

public interface ExamService {

    List<AvailablePaperVO> available(Integer studentId);

    ExamStartVO start(Integer paperId, Integer studentId);

    Integer submit(Integer paperId, SubmitDTO dto, Integer studentId);
}

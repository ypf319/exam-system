package com.exam.service;

import com.exam.vo.PaperQuestionVO;
import com.exam.vo.RecordVO;
import com.exam.vo.StatisticsVO;
import com.github.pagehelper.PageInfo;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

public interface RecordService {

    PageInfo<RecordVO> myRecords(Integer studentId, Integer pageNum, Integer pageSize);

    PageInfo<RecordVO> paperRecords(Integer paperId, Integer pageNum, Integer pageSize);

    RecordVO detail(Integer recordId, Integer userId, Integer role);

    List<PaperQuestionVO> questions(Integer recordId, Integer userId, Integer role);

    StatisticsVO statistics(Integer paperId, Integer teacherId);

    void export(Integer paperId, Integer teacherId, HttpServletResponse response);
}

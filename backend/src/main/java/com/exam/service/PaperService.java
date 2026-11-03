package com.exam.service;

import com.exam.dto.PaperAutoDTO;
import com.exam.dto.PaperManualDTO;
import com.exam.entity.Paper;
import com.exam.vo.PaperDetailVO;
import com.github.pagehelper.PageInfo;

public interface PaperService {

    PageInfo<Paper> pageByTeacher(Integer teacherId, Integer courseId, Integer status,
                                  String keyword, Integer pageNum, Integer pageSize);

    PaperDetailVO detail(Integer paperId, boolean withAnswer);

    PaperDetailVO createManual(PaperManualDTO dto, Integer teacherId);

    PaperDetailVO createAuto(PaperAutoDTO dto, Integer teacherId);

    void updateStatus(Integer paperId, Integer status, Integer teacherId);

    void delete(Integer paperId, Integer teacherId);
}

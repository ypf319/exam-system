package com.exam.mapper;

import com.exam.entity.ExamRecord;
import com.exam.vo.RecordVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExamRecordMapper {

    ExamRecord selectById(Integer id);

    ExamRecord selectByPaperAndStudent(@Param("paperId") Integer paperId, @Param("studentId") Integer studentId);

    List<RecordVO> selectByStudent(Integer studentId);

    List<RecordVO> selectByPaper(Integer paperId);

    RecordVO selectDetailById(Integer id);

    int insert(ExamRecord record);

    int updateSubmit(ExamRecord record);

    int deleteByPaperId(Integer paperId);

    int countSubmittedByPaper(Integer paperId);
}

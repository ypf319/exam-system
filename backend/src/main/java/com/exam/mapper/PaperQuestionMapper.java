package com.exam.mapper;

import com.exam.entity.PaperQuestion;
import com.exam.vo.PaperQuestionVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PaperQuestionMapper {

    List<PaperQuestion> selectByPaperId(Integer paperId);

    List<PaperQuestionVO> selectQuestionVOByPaperId(Integer paperId);

    int insertBatch(@Param("list") List<PaperQuestion> list);

    int deleteByPaperId(Integer paperId);

    int countByQuestionId(Integer questionId);
}

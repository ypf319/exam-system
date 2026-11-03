package com.exam.mapper;

import com.exam.entity.Question;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface QuestionMapper {

    List<Question> selectPage(@Param("courseId") Integer courseId,
                              @Param("questionType") Integer questionType,
                              @Param("difficulty") Integer difficulty,
                              @Param("keyword") String keyword,
                              @Param("createBy") Integer createBy);

    Question selectById(Integer id);

    List<Question> selectByIds(@Param("ids") List<Integer> ids);

    List<Question> randomSelect(@Param("courseId") Integer courseId,
                                @Param("questionType") Integer questionType,
                                @Param("difficulty") Integer difficulty,
                                @Param("limit") Integer limit);

    int countByCondition(@Param("courseId") Integer courseId,
                         @Param("questionType") Integer questionType,
                         @Param("difficulty") Integer difficulty);

    int insert(Question question);

    int insertBatch(@Param("list") List<Question> list);

    int update(Question question);

    int deleteById(Integer id);

    int countByCourseId(Integer courseId);
}

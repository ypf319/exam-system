package com.exam.mapper;

import com.exam.entity.Course;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CourseMapper {

    List<Course> selectList(@Param("keyword") String keyword);

    Course selectById(Integer id);

    int insert(Course course);

    int update(Course course);

    int deleteById(Integer id);

    int countQuestions(Integer courseId);

    int countPapers(Integer courseId);
}

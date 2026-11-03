package com.exam.mapper;

import com.exam.entity.Paper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PaperMapper {

    List<Paper> selectByTeacher(@Param("createBy") Integer createBy,
                                @Param("courseId") Integer courseId,
                                @Param("status") Integer status,
                                @Param("keyword") String keyword);

    List<Paper> selectAvailable(@Param("studentId") Integer studentId,
                                @Param("courseId") Integer courseId);

    Paper selectById(Integer id);

    int insert(Paper paper);

    int update(Paper paper);

    int updateStatus(@Param("id") Integer id, @Param("status") Integer status);

    int deleteById(Integer id);

    int countByCourseId(Integer courseId);
}

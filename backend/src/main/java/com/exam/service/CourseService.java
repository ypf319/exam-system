package com.exam.service;

import com.exam.entity.Course;
import com.github.pagehelper.PageInfo;

import java.util.List;

public interface CourseService {

    PageInfo<Course> page(String keyword, Integer pageNum, Integer pageSize);

    List<Course> list();

    void add(Course course);

    void update(Course course);

    void delete(Integer id);
}

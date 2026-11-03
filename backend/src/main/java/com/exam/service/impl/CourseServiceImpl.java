package com.exam.service.impl;

import com.exam.common.BusinessException;
import com.exam.entity.Course;
import com.exam.mapper.CourseMapper;
import com.exam.service.CourseService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseMapper courseMapper;

    @Override
    public PageInfo<Course> page(String keyword, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Course> list = courseMapper.selectList(keyword);
        return new PageInfo<>(list);
    }

    @Override
    public List<Course> list() {
        return courseMapper.selectList(null);
    }

    @Override
    public void add(Course course) {
        if (!StringUtils.hasText(course.getCourseName())) {
            throw new BusinessException("课程名称不能为空");
        }
        course.setCourseName(course.getCourseName().trim());
        courseMapper.insert(course);
    }

    @Override
    public void update(Course course) {
        if (course.getId() == null || courseMapper.selectById(course.getId()) == null) {
            throw new BusinessException("课程不存在");
        }
        if (!StringUtils.hasText(course.getCourseName())) {
            throw new BusinessException("课程名称不能为空");
        }
        course.setCourseName(course.getCourseName().trim());
        courseMapper.update(course);
    }

    @Override
    public void delete(Integer id) {
        if (courseMapper.countQuestions(id) > 0) {
            throw new BusinessException("该课程下存在题目，无法删除");
        }
        if (courseMapper.countPapers(id) > 0) {
            throw new BusinessException("该课程下存在试卷，无法删除");
        }
        courseMapper.deleteById(id);
    }
}

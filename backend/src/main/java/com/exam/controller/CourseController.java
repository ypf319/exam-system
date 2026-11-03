package com.exam.controller;

import com.exam.common.Result;
import com.exam.entity.Course;
import com.exam.interceptor.RequireRole;
import com.exam.service.CourseService;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    public Result<List<Course>> list() {
        return Result.ok(courseService.list());
    }

    @GetMapping("/page")
    @RequireRole(1)
    public Result<PageInfo<Course>> page(@RequestParam(required = false) String keyword,
                                         @RequestParam(defaultValue = "1") Integer pageNum,
                                         @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.ok(courseService.page(keyword, pageNum, pageSize));
    }

    @PostMapping
    @RequireRole(1)
    public Result<Void> add(@RequestBody Course course) {
        courseService.add(course);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @RequireRole(1)
    public Result<Void> update(@PathVariable Integer id, @RequestBody Course course) {
        course.setId(id);
        courseService.update(course);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @RequireRole(1)
    public Result<Void> delete(@PathVariable Integer id) {
        courseService.delete(id);
        return Result.ok();
    }
}

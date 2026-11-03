package com.exam.service;

import com.exam.entity.Question;
import com.github.pagehelper.PageInfo;
import org.springframework.web.multipart.MultipartFile;

public interface QuestionService {

    PageInfo<Question> page(Integer courseId, Integer questionType, Integer difficulty,
                            String keyword, Integer pageNum, Integer pageSize);

    Question getById(Integer id);

    void add(Question question, Integer createBy);

    void update(Question question);

    void delete(Integer id);

    int importExcel(MultipartFile file, Integer courseId, Integer createBy);
}

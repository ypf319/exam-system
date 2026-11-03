package com.exam.service.impl;

import com.exam.common.BusinessException;
import com.exam.entity.Question;
import com.exam.mapper.CourseMapper;
import com.exam.mapper.PaperQuestionMapper;
import com.exam.mapper.QuestionMapper;
import com.exam.service.QuestionService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuestionMapper questionMapper;
    private final CourseMapper courseMapper;
    private final PaperQuestionMapper paperQuestionMapper;

    @Override
    public PageInfo<Question> page(Integer courseId, Integer questionType, Integer difficulty,
                                   String keyword, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Question> list = questionMapper.selectPage(courseId, questionType, difficulty, keyword, null);
        return new PageInfo<>(list);
    }

    @Override
    public Question getById(Integer id) {
        return questionMapper.selectById(id);
    }

    @Override
    public void add(Question question, Integer createBy) {
        validate(question);
        question.setCreateBy(createBy);
        questionMapper.insert(question);
    }

    @Override
    public void update(Question question) {
        if (question.getId() == null || questionMapper.selectById(question.getId()) == null) {
            throw new BusinessException("题目不存在");
        }
        validate(question);
        questionMapper.update(question);
    }

    @Override
    public void delete(Integer id) {
        if (paperQuestionMapper.countByQuestionId(id) > 0) {
            throw new BusinessException("该题目已被试卷引用，无法删除");
        }
        questionMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int importExcel(MultipartFile file, Integer courseId, Integer createBy) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择要导入的 Excel 文件");
        }
        if (courseMapper.selectById(courseId) == null) {
            throw new BusinessException("课程不存在");
        }
        List<Question> questions = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                String title = getCell(row, 1, formatter);
                if (!StringUtils.hasText(title)) {
                    continue;
                }
                Question question = new Question();
                question.setCourseId(courseId);
                question.setCreateBy(createBy);
                question.setQuestionType(parseType(getCell(row, 0, formatter), i));
                question.setTitle(title.trim());
                question.setOptionA(getCell(row, 2, formatter));
                question.setOptionB(getCell(row, 3, formatter));
                question.setOptionC(getCell(row, 4, formatter));
                question.setOptionD(getCell(row, 5, formatter));
                question.setAnswer(normalizeAnswer(question.getQuestionType(), getCell(row, 6, formatter), i));
                question.setScore(parseScore(getCell(row, 7, formatter), i));
                question.setDifficulty(parseDifficulty(getCell(row, 8, formatter), i));
                validate(question);
                questions.add(question);
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("Excel 解析失败：" + e.getMessage());
        }
        if (questions.isEmpty()) {
            throw new BusinessException("Excel 中没有可导入的题目数据");
        }
        questionMapper.insertBatch(questions);
        return questions.size();
    }

    private void validate(Question question) {
        if (question.getCourseId() == null || courseMapper.selectById(question.getCourseId()) == null) {
            throw new BusinessException("请选择有效的课程");
        }
        if (!StringUtils.hasText(question.getTitle())) {
            throw new BusinessException("题干不能为空");
        }
        Integer type = question.getQuestionType();
        if (type == null || type < 1 || type > 3) {
            throw new BusinessException("题型不合法");
        }
        if (question.getScore() == null || question.getScore() <= 0) {
            throw new BusinessException("分值必须大于 0");
        }
        if (question.getDifficulty() == null || question.getDifficulty() < 1 || question.getDifficulty() > 3) {
            throw new BusinessException("难度不合法");
        }
        if (!StringUtils.hasText(question.getAnswer())) {
            throw new BusinessException("标准答案不能为空");
        }
        if (type == 1 || type == 2) {
            if (!StringUtils.hasText(question.getOptionA()) || !StringUtils.hasText(question.getOptionB())) {
                throw new BusinessException("选择题至少需要填写 A、B 两个选项");
            }
            String answer = question.getAnswer().replaceAll("[\\s,，;；、]", "").toUpperCase(Locale.ROOT);
            if (!answer.matches("[A-D]+")) {
                throw new BusinessException("选择题答案只能由 A、B、C、D 组成");
            }
            if (type == 1 && answer.length() != 1) {
                throw new BusinessException("单选题答案只能有一个选项");
            }
            if (type == 2 && answer.length() < 2) {
                throw new BusinessException("多选题答案至少包含两个选项");
            }
            question.setAnswer(answer);
        } else {
            String answer = question.getAnswer().trim();
            if (!("true".equalsIgnoreCase(answer) || "false".equalsIgnoreCase(answer)
                    || "正确".equals(answer) || "错误".equals(answer))) {
                throw new BusinessException("判断题答案只能为 true 或 false");
            }
            question.setAnswer(("true".equalsIgnoreCase(answer) || "正确".equals(answer)) ? "true" : "false");
            question.setOptionA(null);
            question.setOptionB(null);
            question.setOptionC(null);
            question.setOptionD(null);
        }
    }

    private String getCell(Row row, int index, DataFormatter formatter) {
        Cell cell = row.getCell(index);
        return cell == null ? "" : formatter.formatCellValue(cell).trim();
    }

    private Integer parseType(String value, int rowNum) {
        String text = value.trim();
        if ("1".equals(text) || text.contains("单选")) {
            return 1;
        }
        if ("2".equals(text) || text.contains("多选")) {
            return 2;
        }
        if ("3".equals(text) || text.contains("判断")) {
            return 3;
        }
        throw new BusinessException("第 " + (rowNum + 1) + " 行题型不合法，应为单选/多选/判断");
    }

    private String normalizeAnswer(Integer type, String value, int rowNum) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException("第 " + (rowNum + 1) + " 行答案不能为空");
        }
        String answer = value.trim();
        if (type == 3) {
            if ("true".equalsIgnoreCase(answer) || "正确".equals(answer) || "对".equals(answer)) {
                return "true";
            }
            if ("false".equalsIgnoreCase(answer) || "错误".equals(answer) || "错".equals(answer)) {
                return "false";
            }
            throw new BusinessException("第 " + (rowNum + 1) + " 行判断题答案应为 true/false");
        }
        String normalized = answer.replaceAll("[\\s,，;；、]", "").toUpperCase(Locale.ROOT);
        if (!normalized.matches("[A-D]+")) {
            throw new BusinessException("第 " + (rowNum + 1) + " 行选择题答案不合法");
        }
        return normalized;
    }

    private Integer parseScore(String value, int rowNum) {
        try {
            int score = Integer.parseInt(value.trim());
            if (score <= 0) {
                throw new NumberFormatException();
            }
            return score;
        } catch (Exception e) {
            throw new BusinessException("第 " + (rowNum + 1) + " 行分值必须为正整数");
        }
    }

    private Integer parseDifficulty(String value, int rowNum) {
        String text = value.trim();
        if ("1".equals(text) || text.contains("简单") || text.contains("易")) {
            return 1;
        }
        if ("2".equals(text) || text.contains("中等") || text.contains("中")) {
            return 2;
        }
        if ("3".equals(text) || text.contains("困难") || text.contains("难")) {
            return 3;
        }
        throw new BusinessException("第 " + (rowNum + 1) + " 行难度不合法，应为简单/中等/困难");
    }
}

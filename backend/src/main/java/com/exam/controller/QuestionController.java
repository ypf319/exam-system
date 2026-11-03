package com.exam.controller;

import com.exam.common.Result;
import com.exam.entity.Question;
import com.exam.interceptor.RequireRole;
import com.exam.service.QuestionService;
import com.exam.vo.LoginUserVO;
import com.github.pagehelper.PageInfo;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
@RequireRole(2)
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping
    public Result<PageInfo<Question>> page(@RequestParam(required = false) Integer courseId,
                                           @RequestParam(required = false) Integer questionType,
                                           @RequestParam(required = false) Integer difficulty,
                                           @RequestParam(required = false) String keyword,
                                           @RequestParam(defaultValue = "1") Integer pageNum,
                                           @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.ok(questionService.page(courseId, questionType, difficulty, keyword, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public Result<Question> detail(@PathVariable Integer id) {
        return Result.ok(questionService.getById(id));
    }

    @PostMapping
    public Result<Void> add(@RequestBody Question question,
                            @SessionAttribute("loginUser") LoginUserVO user) {
        questionService.add(question, user.getId());
        return Result.ok();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Integer id, @RequestBody Question question,
                               @SessionAttribute("loginUser") LoginUserVO user) {
        question.setId(id);
        question.setCreateBy(user.getId());
        questionService.update(question);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        questionService.delete(id);
        return Result.ok();
    }

    @PostMapping("/import")
    public Result<Integer> importExcel(@RequestParam("file") MultipartFile file,
                                       @RequestParam Integer courseId,
                                       @SessionAttribute("loginUser") LoginUserVO user) {
        int count = questionService.importExcel(file, courseId, user.getId());
        return Result.ok("成功导入 " + count + " 道题目", count);
    }

    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("题目导入模板");
            String[] headers = {"题型", "题干", "选项A", "选项B", "选项C", "选项D", "答案", "分值", "难度"};
            Row header = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
            }
            Row example1 = sheet.createRow(1);
            String[] single = {"单选", "HTTP 协议默认端口是？", "21", "80", "443", "8080", "B", "5", "简单"};
            for (int i = 0; i < single.length; i++) {
                example1.createCell(i).setCellValue(single[i]);
            }
            Row example2 = sheet.createRow(2);
            String[] multi = {"多选", "下列属于应用层协议的是？", "HTTP", "FTP", "TCP", "DNS", "ABD", "5", "中等"};
            for (int i = 0; i < multi.length; i++) {
                example2.createCell(i).setCellValue(multi[i]);
            }
            Row example3 = sheet.createRow(3);
            String[] judge = {"判断", "TCP 是面向连接的可靠传输协议。", "", "", "", "", "true", "5", "简单"};
            for (int i = 0; i < judge.length; i++) {
                example3.createCell(i).setCellValue(judge[i]);
            }
            for (int i = 0; i < headers.length; i++) {
                sheet.setColumnWidth(i, 22 * 256);
            }
            String fileName = URLEncoder.encode("题目导入模板.xlsx", StandardCharsets.UTF_8).replace("+", "%20");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + fileName);
            try (OutputStream outputStream = response.getOutputStream()) {
                workbook.write(outputStream);
                outputStream.flush();
            }
        } catch (Exception e) {
            throw new RuntimeException("模板下载失败：" + e.getMessage(), e);
        }
    }
}

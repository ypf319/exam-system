package com.exam.service.impl;

import com.exam.common.BusinessException;
import com.exam.entity.Paper;
import com.exam.mapper.ExamRecordMapper;
import com.exam.mapper.PaperMapper;
import com.exam.mapper.PaperQuestionMapper;
import com.exam.service.RecordService;
import com.exam.vo.PaperQuestionVO;
import com.exam.vo.RecordVO;
import com.exam.vo.StatisticsVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecordServiceImpl implements RecordService {

    private final ExamRecordMapper examRecordMapper;
    private final PaperMapper paperMapper;
    private final PaperQuestionMapper paperQuestionMapper;

    @Override
    public PageInfo<RecordVO> myRecords(Integer studentId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<RecordVO> list = examRecordMapper.selectByStudent(studentId);
        return new PageInfo<>(list);
    }

    @Override
    public PageInfo<RecordVO> paperRecords(Integer paperId, Integer pageNum, Integer pageSize) {
        Paper paper = paperMapper.selectById(paperId);
        if (paper == null) {
            throw new BusinessException("试卷不存在");
        }
        PageHelper.startPage(pageNum, pageSize);
        List<RecordVO> list = examRecordMapper.selectByPaper(paperId);
        return new PageInfo<>(list);
    }

    @Override
    public RecordVO detail(Integer recordId, Integer userId, Integer role) {
        RecordVO vo = examRecordMapper.selectDetailById(recordId);
        if (vo == null) {
            throw new BusinessException("答卷记录不存在");
        }
        if (role == 3 && !vo.getStudentId().equals(userId)) {
            throw new BusinessException("无权查看他人的答卷");
        }
        if (role == 2) {
            Paper paper = paperMapper.selectById(vo.getPaperId());
            if (paper == null || !paper.getCreateBy().equals(userId)) {
                throw new BusinessException("无权查看其他教师试卷的答卷");
            }
        }
        return vo;
    }

    @Override
    public List<PaperQuestionVO> questions(Integer recordId, Integer userId, Integer role) {
        RecordVO record = detail(recordId, userId, role);
        return paperQuestionMapper.selectQuestionVOByPaperId(record.getPaperId());
    }

    @Override
    public StatisticsVO statistics(Integer paperId, Integer teacherId) {
        Paper paper = getOwnedPaper(paperId, teacherId);
        List<RecordVO> records = examRecordMapper.selectByPaper(paperId);
        long submitted = records.stream().filter(item -> item.getStatus() != null && item.getStatus() == 1).count();
        int max = 0;
        int min = 0;
        double avg = 0;
        long passCount = 0;
        boolean first = true;
        for (RecordVO record : records) {
            if (record.getStatus() == null || record.getStatus() != 1) {
                continue;
            }
            int score = record.getScore() == null ? 0 : record.getScore();
            if (first) {
                max = score;
                min = score;
                first = false;
            } else {
                max = Math.max(max, score);
                min = Math.min(min, score);
            }
            avg += score;
            if (paper.getTotalScore() != null && score >= paper.getTotalScore() * 0.6) {
                passCount++;
            }
        }
        if (submitted > 0) {
            avg = Math.round(avg / submitted * 10) / 10.0;
        }
        StatisticsVO vo = new StatisticsVO();
        vo.setPaperId(paperId);
        vo.setPaperName(paper.getPaperName());
        vo.setTotalScore(paper.getTotalScore());
        vo.setStudentCount(records.size());
        vo.setSubmittedCount((int) submitted);
        vo.setMaxScore(max);
        vo.setMinScore(min);
        vo.setAvgScore(avg);
        vo.setPassRate(submitted == 0 ? 0 : Math.round(passCount * 1000.0 / submitted) / 10.0);
        return vo;
    }

    @Override
    public void export(Integer paperId, Integer teacherId, HttpServletResponse response) {
        Paper paper = getOwnedPaper(paperId, teacherId);
        List<RecordVO> records = examRecordMapper.selectByPaper(paperId);
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("成绩表");
            String[] headers = {"序号", "学号", "姓名", "课程", "试卷", "得分", "总分", "状态", "提交时间"};
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = buildHeaderStyle(workbook);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
                headerRow.getCell(i).setCellStyle(headerStyle);
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            int index = 1;
            for (RecordVO record : records) {
                Row row = sheet.createRow(index);
                row.createCell(0).setCellValue(index);
                row.createCell(1).setCellValue(record.getUsername() == null ? "" : record.getUsername());
                row.createCell(2).setCellValue(record.getStudentName() == null ? "" : record.getStudentName());
                row.createCell(3).setCellValue(record.getCourseName() == null ? "" : record.getCourseName());
                row.createCell(4).setCellValue(record.getPaperName() == null ? "" : record.getPaperName());
                row.createCell(5).setCellValue(record.getScore() == null ? 0 : record.getScore());
                row.createCell(6).setCellValue(record.getTotalScore() == null ? paper.getTotalScore() : record.getTotalScore());
                row.createCell(7).setCellValue(record.getStatus() != null && record.getStatus() == 1 ? "已提交" : "未提交");
                row.createCell(8).setCellValue(record.getSubmitTime() == null ? "" : record.getSubmitTime().format(formatter));
                index++;
            }
            for (int i = 0; i < headers.length; i++) {
                sheet.setColumnWidth(i, 18 * 256);
            }
            String fileName = URLEncoder.encode("成绩表_" + paper.getPaperName() + ".xlsx", StandardCharsets.UTF_8)
                    .replace("+", "%20");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + fileName);
            try (OutputStream outputStream = response.getOutputStream()) {
                workbook.write(outputStream);
                outputStream.flush();
            }
        } catch (Exception e) {
            throw new BusinessException("导出成绩失败：" + e.getMessage());
        }
    }

    private CellStyle buildHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private Paper getOwnedPaper(Integer paperId, Integer teacherId) {
        Paper paper = paperMapper.selectById(paperId);
        if (paper == null) {
            throw new BusinessException("试卷不存在");
        }
        if (!paper.getCreateBy().equals(teacherId)) {
            throw new BusinessException("无权操作其他教师的试卷");
        }
        return paper;
    }
}

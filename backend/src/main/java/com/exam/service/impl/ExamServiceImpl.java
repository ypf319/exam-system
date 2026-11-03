package com.exam.service.impl;

import com.exam.common.BusinessException;
import com.exam.dto.SubmitDTO;
import com.exam.entity.ExamRecord;
import com.exam.entity.Paper;
import com.exam.mapper.ExamRecordMapper;
import com.exam.mapper.PaperMapper;
import com.exam.mapper.PaperQuestionMapper;
import com.exam.service.ExamService;
import com.exam.util.AnswerUtil;
import com.exam.vo.AvailablePaperVO;
import com.exam.vo.ExamStartVO;
import com.exam.vo.PaperQuestionVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExamServiceImpl implements ExamService {

    private final PaperMapper paperMapper;
    private final PaperQuestionMapper paperQuestionMapper;
    private final ExamRecordMapper examRecordMapper;
    private final ObjectMapper objectMapper;

    @Override
    public List<AvailablePaperVO> available(Integer studentId) {
        List<Paper> papers = paperMapper.selectAvailable(studentId, null);
        List<AvailablePaperVO> result = new ArrayList<>();
        for (Paper paper : papers) {
            AvailablePaperVO vo = new AvailablePaperVO();
            BeanUtils.copyProperties(paper, vo);
            ExamRecord record = examRecordMapper.selectByPaperAndStudent(paper.getId(), studentId);
            if (record != null) {
                vo.setRecordId(record.getId());
                vo.setExamStatus(record.getStatus());
                vo.setScore(record.getScore());
            }
            result.add(vo);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExamStartVO start(Integer paperId, Integer studentId) {
        Paper paper = paperMapper.selectById(paperId);
        if (paper == null) {
            throw new BusinessException("试卷不存在");
        }
        if (paper.getStatus() == null || paper.getStatus() != 1) {
            throw new BusinessException("该试卷未启用，无法参加考试");
        }
        LocalDateTime now = LocalDateTime.now();
        if (paper.getStartTime() != null && now.isBefore(paper.getStartTime())) {
            throw new BusinessException("考试尚未开始");
        }
        if (paper.getEndTime() != null && now.isAfter(paper.getEndTime())) {
            throw new BusinessException("考试已结束");
        }
        ExamRecord record = examRecordMapper.selectByPaperAndStudent(paperId, studentId);
        if (record != null && record.getStatus() != null && record.getStatus() == 1) {
            throw new BusinessException("您已提交过该试卷，不能重复考试");
        }
        if (record == null) {
            record = new ExamRecord();
            record.setPaperId(paperId);
            record.setStudentId(studentId);
            record.setScore(0);
            record.setStatus(0);
            examRecordMapper.insert(record);
        }
        List<PaperQuestionVO> questions = paperQuestionMapper.selectQuestionVOByPaperId(paperId);
        questions.forEach(item -> item.setAnswer(null));

        ExamStartVO vo = new ExamStartVO();
        vo.setRecordId(record.getId());
        vo.setPaperId(paper.getId());
        vo.setPaperName(paper.getPaperName());
        vo.setTotalScore(paper.getTotalScore());
        vo.setDuration(paper.getDuration());
        vo.setStartTime(paper.getStartTime());
        vo.setEndTime(paper.getEndTime());
        vo.setQuestions(questions);
        vo.setSavedAnswer(record.getStudentAnswer());
        vo.setRemainSeconds(calculateRemainSeconds(paper, now));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer submit(Integer paperId, SubmitDTO dto, Integer studentId) {
        Paper paper = paperMapper.selectById(paperId);
        if (paper == null) {
            throw new BusinessException("试卷不存在");
        }
        ExamRecord record = examRecordMapper.selectById(dto.getRecordId());
        if (record == null || !record.getPaperId().equals(paperId)) {
            throw new BusinessException("答卷记录不存在");
        }
        if (!record.getStudentId().equals(studentId)) {
            throw new BusinessException("无权提交他人的答卷");
        }
        if (record.getStatus() != null && record.getStatus() == 1) {
            throw new BusinessException("该答卷已提交，请勿重复提交");
        }
        LocalDateTime now = LocalDateTime.now();
        if (paper.getEndTime() != null && now.isAfter(paper.getEndTime().plusSeconds(10))) {
            throw new BusinessException("考试已结束，提交失败");
        }

        List<PaperQuestionVO> questions = paperQuestionMapper.selectQuestionVOByPaperId(paperId);
        Map<Integer, String> answers = dto.getAnswers() == null ? Map.of() : dto.getAnswers();
        int total = 0;
        for (PaperQuestionVO question : questions) {
            String studentAnswer = answers.get(question.getQuestionId());
            if (AnswerUtil.isCorrect(question.getAnswer(), studentAnswer)) {
                total += question.getScore();
            }
        }
        record.setScore(total);
        record.setStatus(1);
        record.setSubmitTime(now);
        try {
            record.setStudentAnswer(objectMapper.writeValueAsString(answers));
        } catch (JsonProcessingException e) {
            throw new BusinessException("答卷保存失败");
        }
        examRecordMapper.updateSubmit(record);
        return total;
    }

    private Long calculateRemainSeconds(Paper paper, LocalDateTime now) {
        long durationSeconds = (paper.getDuration() == null ? 0 : paper.getDuration() * 60L);
        if (paper.getEndTime() == null) {
            return Math.max(durationSeconds, 0);
        }
        long untilEnd = Duration.between(now, paper.getEndTime()).getSeconds();
        if (durationSeconds <= 0) {
            return Math.max(untilEnd, 0);
        }
        return Math.max(Math.min(durationSeconds, untilEnd), 0);
    }
}

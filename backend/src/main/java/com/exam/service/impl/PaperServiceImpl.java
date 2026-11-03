package com.exam.service.impl;

import com.exam.common.BusinessException;
import com.exam.dto.PaperAutoDTO;
import com.exam.dto.PaperManualDTO;
import com.exam.entity.Paper;
import com.exam.entity.PaperQuestion;
import com.exam.entity.Question;
import com.exam.mapper.CourseMapper;
import com.exam.mapper.ExamRecordMapper;
import com.exam.mapper.PaperMapper;
import com.exam.mapper.PaperQuestionMapper;
import com.exam.mapper.QuestionMapper;
import com.exam.service.PaperService;
import com.exam.vo.PaperDetailVO;
import com.exam.vo.PaperQuestionVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PaperServiceImpl implements PaperService {

    private final PaperMapper paperMapper;
    private final PaperQuestionMapper paperQuestionMapper;
    private final QuestionMapper questionMapper;
    private final CourseMapper courseMapper;
    private final ExamRecordMapper examRecordMapper;

    @Override
    public PageInfo<Paper> pageByTeacher(Integer teacherId, Integer courseId, Integer status,
                                         String keyword, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Paper> list = paperMapper.selectByTeacher(teacherId, courseId, status, keyword);
        return new PageInfo<>(list);
    }

    @Override
    public PaperDetailVO detail(Integer paperId, boolean withAnswer) {
        Paper paper = paperMapper.selectById(paperId);
        if (paper == null) {
            throw new BusinessException("试卷不存在");
        }
        List<PaperQuestionVO> questions = paperQuestionMapper.selectQuestionVOByPaperId(paperId);
        if (!withAnswer) {
            questions.forEach(item -> item.setAnswer(null));
        }
        PaperDetailVO vo = new PaperDetailVO();
        BeanUtils.copyProperties(paper, vo);
        vo.setQuestions(questions);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaperDetailVO createManual(PaperManualDTO dto, Integer teacherId) {
        checkCourse(dto.getCourseId());
        checkTime(dto.getStartTime(), dto.getEndTime());
        Set<Integer> idSet = new LinkedHashSet<>(dto.getQuestionIds());
        List<Question> questions = questionMapper.selectByIds(new ArrayList<>(idSet));
        if (questions.size() != idSet.size()) {
            throw new BusinessException("部分题目不存在，请刷新后重试");
        }
        for (Question question : questions) {
            if (!dto.getCourseId().equals(question.getCourseId())) {
                throw new BusinessException("题目【" + question.getTitle() + "】不属于所选课程");
            }
        }
        int totalScore = questions.stream().mapToInt(Question::getScore).sum();
        Paper paper = buildPaper(dto.getPaperName(), dto.getCourseId(), totalScore,
                dto.getDuration(), dto.getStartTime(), dto.getEndTime(), teacherId);
        paperMapper.insert(paper);
        savePaperQuestions(paper.getId(), questions);
        return detail(paper.getId(), true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaperDetailVO createAuto(PaperAutoDTO dto, Integer teacherId) {
        checkCourse(dto.getCourseId());
        checkTime(dto.getStartTime(), dto.getEndTime());
        int singleCount = valueOrZero(dto.getSingleCount());
        int multiCount = valueOrZero(dto.getMultiCount());
        int judgeCount = valueOrZero(dto.getJudgeCount());
        if (singleCount < 0 || multiCount < 0 || judgeCount < 0) {
            throw new BusinessException("抽题数量不能为负数");
        }
        if (singleCount + multiCount + judgeCount == 0) {
            throw new BusinessException("请至少设置一种题型的抽题数量");
        }
        List<Question> questions = new ArrayList<>();
        questions.addAll(randomQuestions(dto.getCourseId(), 1, singleCount, dto.getDifficulty(), "单选题"));
        questions.addAll(randomQuestions(dto.getCourseId(), 2, multiCount, dto.getDifficulty(), "多选题"));
        questions.addAll(randomQuestions(dto.getCourseId(), 3, judgeCount, dto.getDifficulty(), "判断题"));
        int totalScore = questions.stream().mapToInt(Question::getScore).sum();
        Paper paper = buildPaper(dto.getPaperName(), dto.getCourseId(), totalScore,
                dto.getDuration(), dto.getStartTime(), dto.getEndTime(), teacherId);
        paperMapper.insert(paper);
        savePaperQuestions(paper.getId(), questions);
        return detail(paper.getId(), true);
    }

    @Override
    public void updateStatus(Integer paperId, Integer status, Integer teacherId) {
        Paper paper = getOwnedPaper(paperId, teacherId);
        if (status == null || status < 0 || status > 2) {
            throw new BusinessException("状态值不合法");
        }
        if (status == 1) {
            Integer count = paper.getQuestionCount();
            if (count == null || count == 0) {
                throw new BusinessException("试卷中没有题目，无法启用");
            }
        }
        paperMapper.updateStatus(paperId, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Integer paperId, Integer teacherId) {
        getOwnedPaper(paperId, teacherId);
        paperQuestionMapper.deleteByPaperId(paperId);
        examRecordMapper.deleteByPaperId(paperId);
        paperMapper.deleteById(paperId);
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

    private void checkCourse(Integer courseId) {
        if (courseId == null || courseMapper.selectById(courseId) == null) {
            throw new BusinessException("请选择有效的课程");
        }
    }

    private void checkTime(java.time.LocalDateTime startTime, java.time.LocalDateTime endTime) {
        if (startTime != null && endTime != null && endTime.isBefore(startTime)) {
            throw new BusinessException("考试截止时间不能早于开始时间");
        }
    }

    private Paper buildPaper(String paperName, Integer courseId, Integer totalScore, Integer duration,
                             java.time.LocalDateTime startTime, java.time.LocalDateTime endTime, Integer teacherId) {
        Paper paper = new Paper();
        paper.setPaperName(paperName.trim());
        paper.setCourseId(courseId);
        paper.setTotalScore(totalScore);
        paper.setDuration(duration);
        paper.setStartTime(startTime);
        paper.setEndTime(endTime);
        paper.setCreateBy(teacherId);
        paper.setStatus(0);
        return paper;
    }

    private void savePaperQuestions(Integer paperId, List<Question> questions) {
        List<PaperQuestion> list = new ArrayList<>();
        for (Question question : questions) {
            PaperQuestion pq = new PaperQuestion();
            pq.setPaperId(paperId);
            pq.setQuestionId(question.getId());
            pq.setScore(question.getScore());
            list.add(pq);
        }
        if (!list.isEmpty()) {
            paperQuestionMapper.insertBatch(list);
        }
    }

    private List<Question> randomQuestions(Integer courseId, Integer type, int count,
                                           Integer difficulty, String typeName) {
        if (count == 0) {
            return new ArrayList<>();
        }
        int available = questionMapper.countByCondition(courseId, type, difficulty);
        if (available < count) {
            throw new BusinessException(typeName + "数量不足，当前可用 " + available + " 道，需要 " + count + " 道");
        }
        return questionMapper.randomSelect(courseId, type, difficulty, count);
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }
}

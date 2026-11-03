package com.exam.controller;

import com.exam.common.Result;
import com.exam.dto.SubmitDTO;
import com.exam.interceptor.RequireRole;
import com.exam.service.ExamService;
import com.exam.vo.AvailablePaperVO;
import com.exam.vo.ExamStartVO;
import com.exam.vo.LoginUserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.List;

@RestController
@RequestMapping("/api/exam")
@RequiredArgsConstructor
@RequireRole(3)
public class ExamController {

    private final ExamService examService;

    @GetMapping("/available")
    public Result<List<AvailablePaperVO>> available(@SessionAttribute("loginUser") LoginUserVO user) {
        return Result.ok(examService.available(user.getId()));
    }

    @PostMapping("/{paperId}/start")
    public Result<ExamStartVO> start(@PathVariable Integer paperId,
                                     @SessionAttribute("loginUser") LoginUserVO user) {
        return Result.ok(examService.start(paperId, user.getId()));
    }

    @PostMapping("/{paperId}/submit")
    public Result<Integer> submit(@PathVariable Integer paperId, @Valid @RequestBody SubmitDTO dto,
                                  @SessionAttribute("loginUser") LoginUserVO user) {
        Integer score = examService.submit(paperId, dto, user.getId());
        return Result.ok("交卷成功", score);
    }
}

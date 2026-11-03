package com.exam.controller;

import com.exam.common.Result;
import com.exam.interceptor.RequireRole;
import com.exam.service.RecordService;
import com.exam.vo.LoginUserVO;
import com.exam.vo.PaperQuestionVO;
import com.exam.vo.RecordVO;
import com.exam.vo.StatisticsVO;
import com.github.pagehelper.PageInfo;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.List;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
public class RecordController {

    private final RecordService recordService;

    @GetMapping("/my")
    @RequireRole(3)
    public Result<PageInfo<RecordVO>> my(@RequestParam(defaultValue = "1") Integer pageNum,
                                         @RequestParam(defaultValue = "10") Integer pageSize,
                                         @SessionAttribute("loginUser") LoginUserVO user) {
        return Result.ok(recordService.myRecords(user.getId(), pageNum, pageSize));
    }

    @GetMapping
    @RequireRole(2)
    public Result<PageInfo<RecordVO>> page(@RequestParam Integer paperId,
                                           @RequestParam(defaultValue = "1") Integer pageNum,
                                           @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.ok(recordService.paperRecords(paperId, pageNum, pageSize));
    }

    @GetMapping("/statistics")
    @RequireRole(2)
    public Result<StatisticsVO> statistics(@RequestParam Integer paperId,
                                           @SessionAttribute("loginUser") LoginUserVO user) {
        return Result.ok(recordService.statistics(paperId, user.getId()));
    }

    @GetMapping("/export")
    @RequireRole(2)
    public void export(@RequestParam Integer paperId,
                       @SessionAttribute("loginUser") LoginUserVO user,
                       HttpServletResponse response) {
        recordService.export(paperId, user.getId(), response);
    }

    @GetMapping("/{id}")
    public Result<RecordVO> detail(@PathVariable Integer id,
                                   @SessionAttribute("loginUser") LoginUserVO user) {
        return Result.ok(recordService.detail(id, user.getId(), user.getRole()));
    }

    @GetMapping("/{id}/questions")
    public Result<List<PaperQuestionVO>> questions(@PathVariable Integer id,
                                                   @SessionAttribute("loginUser") LoginUserVO user) {
        return Result.ok(recordService.questions(id, user.getId(), user.getRole()));
    }
}

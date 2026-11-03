package com.exam.controller;

import com.exam.common.Result;
import com.exam.dto.PaperAutoDTO;
import com.exam.dto.PaperManualDTO;
import com.exam.entity.Paper;
import com.exam.interceptor.RequireRole;
import com.exam.service.PaperService;
import com.exam.vo.LoginUserVO;
import com.exam.vo.PaperDetailVO;
import com.github.pagehelper.PageInfo;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.SessionAttribute;

@RestController
@RequestMapping("/api/papers")
@RequiredArgsConstructor
@RequireRole(2)
public class PaperController {

    private final PaperService paperService;

    @GetMapping
    public Result<PageInfo<Paper>> page(@RequestParam(required = false) Integer courseId,
                                        @RequestParam(required = false) Integer status,
                                        @RequestParam(required = false) String keyword,
                                        @RequestParam(defaultValue = "1") Integer pageNum,
                                        @RequestParam(defaultValue = "10") Integer pageSize,
                                        @SessionAttribute("loginUser") LoginUserVO user) {
        return Result.ok(paperService.pageByTeacher(user.getId(), courseId, status, keyword, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public Result<PaperDetailVO> detail(@PathVariable Integer id) {
        return Result.ok(paperService.detail(id, true));
    }

    @PostMapping("/manual")
    public Result<PaperDetailVO> createManual(@Valid @RequestBody PaperManualDTO dto,
                                              @SessionAttribute("loginUser") LoginUserVO user) {
        return Result.ok("组卷成功", paperService.createManual(dto, user.getId()));
    }

    @PostMapping("/auto")
    public Result<PaperDetailVO> createAuto(@Valid @RequestBody PaperAutoDTO dto,
                                            @SessionAttribute("loginUser") LoginUserVO user) {
        return Result.ok("组卷成功", paperService.createAuto(dto, user.getId()));
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Integer id, @RequestParam Integer status,
                                     @SessionAttribute("loginUser") LoginUserVO user) {
        paperService.updateStatus(id, status, user.getId());
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id,
                               @SessionAttribute("loginUser") LoginUserVO user) {
        paperService.delete(id, user.getId());
        return Result.ok();
    }
}

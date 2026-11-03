package com.exam.controller;

import com.exam.common.Result;
import com.exam.entity.User;
import com.exam.interceptor.RequireRole;
import com.exam.service.UserService;
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

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@RequireRole(1)
public class UserController {

    private final UserService userService;

    @GetMapping
    public Result<PageInfo<User>> page(@RequestParam(required = false) String keyword,
                                       @RequestParam(required = false) Integer role,
                                       @RequestParam(defaultValue = "1") Integer pageNum,
                                       @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.ok(userService.page(keyword, role, pageNum, pageSize));
    }

    @PostMapping
    public Result<Void> add(@RequestBody User user) {
        userService.add(user);
        return Result.ok();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Integer id, @RequestBody User user) {
        user.setId(id);
        userService.update(user);
        return Result.ok();
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Integer id, @RequestParam Integer status) {
        userService.updateStatus(id, status);
        return Result.ok();
    }

    @PutMapping("/{id}/password/reset")
    public Result<Void> resetPassword(@PathVariable Integer id) {
        userService.resetPassword(id);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        userService.delete(id);
        return Result.ok();
    }
}

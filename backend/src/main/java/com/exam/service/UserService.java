package com.exam.service;

import com.exam.dto.LoginDTO;
import com.exam.entity.User;
import com.exam.vo.LoginUserVO;
import com.github.pagehelper.PageInfo;

public interface UserService {

    LoginUserVO login(LoginDTO dto);

    PageInfo<User> page(String keyword, Integer role, Integer pageNum, Integer pageSize);

    void add(User user);

    void update(User user);

    void updateStatus(Integer id, Integer status);

    void resetPassword(Integer id);

    void delete(Integer id);

    User getById(Integer id);
}

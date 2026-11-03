package com.exam.service.impl;

import com.exam.common.BusinessException;
import com.exam.dto.LoginDTO;
import com.exam.entity.User;
import com.exam.mapper.UserMapper;
import com.exam.service.UserService;
import com.exam.util.MD5Util;
import com.exam.vo.LoginUserVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Override
    public LoginUserVO login(LoginDTO dto) {
        User user = userMapper.selectByUsername(dto.getUsername().trim());
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }
        if (!MD5Util.encrypt(dto.getPassword()).equals(user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        LoginUserVO vo = new LoginUserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setRole(user.getRole());
        return vo;
    }

    @Override
    public PageInfo<User> page(String keyword, Integer role, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<User> list = userMapper.selectPage(keyword, role);
        return new PageInfo<>(list);
    }

    @Override
    public void add(User user) {
        if (!StringUtils.hasText(user.getUsername()) || !StringUtils.hasText(user.getPassword())) {
            throw new BusinessException("账号和密码不能为空");
        }
        if (user.getRole() == null || (user.getRole() != 2 && user.getRole() != 3)) {
            throw new BusinessException("只能创建教师或学生账号");
        }
        if (userMapper.countByUsername(user.getUsername().trim(), null) > 0) {
            throw new BusinessException("账号已存在");
        }
        user.setUsername(user.getUsername().trim());
        user.setPassword(MD5Util.encrypt(user.getPassword()));
        user.setStatus(1);
        userMapper.insert(user);
    }

    @Override
    public void update(User user) {
        User exist = userMapper.selectById(user.getId());
        if (exist == null) {
            throw new BusinessException("用户不存在");
        }
        if (StringUtils.hasText(user.getUsername())
                && userMapper.countByUsername(user.getUsername().trim(), user.getId()) > 0) {
            throw new BusinessException("账号已存在");
        }
        if (user.getRole() != null && user.getRole() != 2 && user.getRole() != 3) {
            throw new BusinessException("角色只能为教师或学生");
        }
        if (StringUtils.hasText(user.getUsername())) {
            user.setUsername(user.getUsername().trim());
        }
        userMapper.update(user);
    }

    @Override
    public void updateStatus(Integer id, Integer status) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (user.getRole() == 1) {
            throw new BusinessException("不能操作管理员账号");
        }
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("状态值不合法");
        }
        userMapper.updateStatus(id, status);
    }

    @Override
    public void resetPassword(Integer id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        userMapper.updatePassword(id, MD5Util.encrypt("123456"));
    }

    @Override
    public void delete(Integer id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (user.getRole() == 1) {
            throw new BusinessException("不能删除管理员账号");
        }
        userMapper.deleteById(id);
    }

    @Override
    public User getById(Integer id) {
        return userMapper.selectById(id);
    }
}

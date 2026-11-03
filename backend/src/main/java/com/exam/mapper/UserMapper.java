package com.exam.mapper;

import com.exam.entity.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserMapper {

    User selectByUsername(String username);

    User selectById(Integer id);

    List<User> selectPage(@Param("keyword") String keyword, @Param("role") Integer role);

    int insert(User user);

    int update(User user);

    int updateStatus(@Param("id") Integer id, @Param("status") Integer status);

    int updatePassword(@Param("id") Integer id, @Param("password") String password);

    int deleteById(Integer id);

    int countByUsername(@Param("username") String username, @Param("excludeId") Integer excludeId);
}

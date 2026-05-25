package com.example.shixun7.service;

import com.example.shixun7.entity.User;
import com.example.shixun7.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 登录 - 明文密码版本
     */
    public User login(String username, String password) {
        User user = userMapper.selectByUsername(username);
        // 直接比对明文密码
        if (user != null && user.getPassword().equals(password)) {
            userMapper.updateLoginTime(user.getId());
            return user;
        }
        return null;
    }

    /**
     * 注册 - 明文密码版本
     */
    public boolean register(User user) {
        if (userMapper.selectByUsername(user.getUsername()) != null) {
            return false;
        }
        // 直接保存明文密码
        return userMapper.insert(user) > 0;
    }

    public User getProfile(Long id) {
        return userMapper.selectById(id);
    }

    public boolean updateProfile(User user) {
        return userMapper.updateProfile(user) > 0;
    }

    // ========== 管理员服务方法 ==========

    public List<User> getUserList(String keyword, Integer role) {
        return userMapper.getUserList(keyword, role);
    }

    public boolean updateStatus(Long id, Integer status) {
        return userMapper.updateStatus(id, status) > 0;
    }

    public boolean resetPassword(Long id) {
        // 明文密码：123456
        return userMapper.resetPassword(id, "123456") > 0;
    }

    public boolean resetPassword(Long id, String newPassword) {
        return userMapper.resetPassword(id, newPassword) > 0;
    }

    // ========== 统计方法（新增） ==========

    /**
     * 获取用户总数
     */
    public int getUserCount() {
        return userMapper.getUserCount();
    }

    /**
     * 获取今日注册用户数
     */
    public int getTodayRegisterCount() {
        return userMapper.getTodayRegisterCount();
    }
}
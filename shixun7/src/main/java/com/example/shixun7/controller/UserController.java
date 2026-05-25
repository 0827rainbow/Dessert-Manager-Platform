package com.example.shixun7.controller;

import com.example.shixun7.entity.*;
import com.example.shixun7.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"}, allowCredentials = "true")
@RestController
@RequestMapping("/user")
@Tag(name = "用户管理", description = "用户登录、注册、个人信息获取及登出")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "使用用户名和密码登录，返回用户信息和token")
    public ResponseResult login(@RequestBody LoginRequest request, HttpSession session) {
        String username = request.getUsername();
        String password = request.getPassword();

        System.out.println("登录请求 - 用户名: " + username);

        User user = userService.login(username, password);
        if (user != null) {
            session.setAttribute("user", user);
            System.out.println("登录成功 - 用户ID: " + user.getId());
            System.out.println("Session ID: " + session.getId());

            Map<String, Object> result = new HashMap<>();
            result.put("token", "token-" + System.currentTimeMillis());
            result.put("user", user);

            return ResponseResult.success(result);
        }
        System.out.println("登录失败 - 用户名或密码错误");
        return ResponseResult.error("用户名或密码错误");
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "注册新账号，用户名不能重复")
    public ResponseResult register(@RequestBody User user) {
        if (userService.register(user)) {
            return ResponseResult.success();
        }
        return ResponseResult.error("用户名已存在");
    }

    @GetMapping("/profile")
    @Operation(summary = "获取个人信息", description = "返回当前登录用户的详细信息")
    public ResponseResult getProfile(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            return ResponseResult.success(userService.getProfile(user.getId()));
        }
        return ResponseResult.error("未登录");
    }

    @GetMapping("/logout")
    @Operation(summary = "用户登出", description = "使当前会话失效")
    public ResponseResult logout(HttpSession session) {
        session.invalidate();
        return ResponseResult.success();
    }
}
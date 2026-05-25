package com.example.shixun7.controller;

import com.example.shixun7.entity.ResponseResult;
import com.example.shixun7.entity.User;
import com.example.shixun7.service.DessertService;
import com.example.shixun7.service.OrderService;
import com.example.shixun7.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175"}, allowCredentials = "true")
@RestController
@RequestMapping("/dashboard")
@Tag(name = "仪表盘统计", description = "管理员获取系统统计数据")
public class DashboardController {

    @Autowired
    private UserService userService;

    @Autowired
    private DessertService dessertService;

    @Autowired
    private OrderService orderService;

    @GetMapping("/statistics")
    @Operation(summary = "获取统计数据", description = "仅管理员可访问，返回用户数、甜点数、订单数、总销售额")
    public ResponseResult getStatistics(HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || currentUser.getRole() != 1) {
            return ResponseResult.error("无权访问");
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("userCount", userService.getUserCount());
        stats.put("dessertCount", dessertService.getDessertCount());
        stats.put("orderCount", orderService.getOrderCount());
        stats.put("totalSales", orderService.getTotalSales());

        return ResponseResult.success(stats);
    }
}
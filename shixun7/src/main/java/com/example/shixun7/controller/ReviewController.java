package com.example.shixun7.controller;

import com.example.shixun7.entity.Review;
import com.example.shixun7.entity.ResponseResult;
import com.example.shixun7.entity.User;
import com.example.shixun7.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175"}, allowCredentials = "true")
@RestController
@RequestMapping("/review")
@Tag(name = "评价管理", description = "商品评价的增删改查")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @GetMapping("/dessert/{dessertId}")
    @Operation(summary = "查询甜点的评价列表", description = "返回甜点下所有可见评价")
    public ResponseResult getDessertReviews(@Parameter(description = "甜点ID", required = true) @PathVariable Long dessertId) {
        System.out.println("获取甜点评价，dessertId: " + dessertId);
        List<Review> reviews = reviewService.getReviewsByDessert(dessertId);
        return ResponseResult.success(reviews);
    }

    @GetMapping("/my")
    @Operation(summary = "查询我的评价列表")
    public ResponseResult getMyReviews(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseResult.error("请先登录");
        }
        List<Review> reviews = reviewService.getReviewsByUser(user.getId());
        return ResponseResult.success(reviews);
    }

    @GetMapping("/check/{orderId}/{dessertId}")
    @Operation(summary = "检查是否已评价")
    public ResponseResult checkReviewed(
            @Parameter(description = "订单ID") @PathVariable Long orderId,
            @Parameter(description = "甜点ID") @PathVariable Long dessertId) {
        System.out.println("检查评价状态，orderId: " + orderId + ", dessertId: " + dessertId);
        Review review = reviewService.getByOrderAndDessert(orderId, dessertId);
        return ResponseResult.success(review != null);
    }

    @PostMapping
    @Operation(summary = "添加评价", description = "订单完成后才能评价，不能重复评价")
    public ResponseResult addReview(@RequestBody Review review, HttpSession session) {
        System.out.println("添加评价，收到数据: " + review);

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseResult.error("请先登录");
        }

        review.setUserId(user.getId());
        if (review.getRating() == null) {
            review.setRating(5);
        }

        try {
            boolean result = reviewService.addReview(review);
            return result ? ResponseResult.success() : ResponseResult.error("评价失败");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.error("评价失败: " + e.getMessage());
        }
    }
}
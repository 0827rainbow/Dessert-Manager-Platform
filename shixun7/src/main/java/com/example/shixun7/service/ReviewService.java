package com.example.shixun7.service;

import com.example.shixun7.entity.Review;
import com.example.shixun7.mapper.ReviewMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewService {

    @Autowired
    private ReviewMapper reviewMapper;

    public List<Review> getReviewsByDessert(Long dessertId) {
        return reviewMapper.selectByDessertId(dessertId);
    }

    public List<Review> getReviewsByUser(Long userId) {
        return reviewMapper.selectByUserId(userId);
    }

    public Review getByOrderAndDessert(Long orderId, Long dessertId) {
        return reviewMapper.selectByOrderAndDessert(orderId, dessertId);
    }

    @Transactional
    public boolean addReview(Review review) {
        System.out.println("保存评价: " + review);
        int result = reviewMapper.insert(review);
        System.out.println("插入结果: " + result);
        if (result > 0) {
            reviewMapper.updateDessertRating(review.getDessertId());
            return true;
        }
        return false;
    }
}

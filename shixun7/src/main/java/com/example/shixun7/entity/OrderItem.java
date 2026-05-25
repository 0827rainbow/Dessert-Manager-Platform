package com.example.shixun7.entity;

import lombok.Data;

@Data
public class OrderItem {
    private Long id;
    private Long orderId;
    private Long dessertId;
    private String dessertName;
    private String dessertImage;
    private Double price;      // 改为 Double
    private Integer quantity;
    private Double subtotal;   // 改为 Double
}
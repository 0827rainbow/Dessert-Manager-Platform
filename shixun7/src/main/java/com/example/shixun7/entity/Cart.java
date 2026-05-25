package com.example.shixun7.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.Date;

@Data
public class Cart {

    @Schema(description = "购物车项ID")
    private Long id;

    private Long userId;

    @Schema(description = "甜点ID")
    private Long dessertId;

    @Schema(description = "数量", example = "2")
    private Integer quantity;

    private Date createTime;
    private Date updateTime;

    @Schema(description = "甜点名称")
    private String dessertName;

    @Schema(description = "甜点图片")
    private String dessertPhoto;

    @Schema(description = "甜点价格")
    private Double dessertPrice;
}
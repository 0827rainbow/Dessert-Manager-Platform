package com.example.shixun7.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.Date;

@Data
public class Dessert {

    @Schema(description = "甜点ID", example = "1")
    private Long id;

    @Schema(description = "甜点名称", example = "提拉米苏", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "甜点描述", example = "意大利经典甜点，咖啡与奶酪的完美结合")
    private String descp;

    @Schema(description = "图片URL", example = "/photo/abc123.jpg")
    private String photoUrl;

    @Schema(description = "单价", example = "32.0")
    private Double price;

    @Schema(description = "原价", example = "45.0")
    private Double originalPrice;

    @Schema(description = "折扣率", example = "0.8")
    private Double discount;

    @Schema(description = "发布日期")
    private Date releaseDate;

    @Schema(description = "所属分类ID", example = "1")
    private Long catId;

    @Schema(description = "分类名称（联查返回）", example = "蛋糕系列")
    private String categoryName;

    @Schema(description = "库存数量", example = "50")
    private Integer stock;

    @Schema(description = "最低库存预警值", example = "5")
    private Integer minStock;

    @Schema(description = "累计销量", example = "320")
    private Integer sales;

    @Schema(description = "评分", example = "4.8")
    private Double rating;

    @Schema(description = "评价数量", example = "128")
    private Integer reviewCount;

    @Schema(description = "状态：0下架 1上架 2缺货", example = "1")
    private Integer status;

    @Schema(description = "是否热门：0否 1是", example = "0")
    private Integer isHot;

    @Schema(description = "是否推荐：0否 1是", example = "1")
    private Integer isRecommend;

    private Date createTime;
    private Date updateTime;
}
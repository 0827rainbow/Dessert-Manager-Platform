package com.example.shixun7.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.Date;

@Data
@Schema(description = "评价实体")
public class Review {

    @Schema(description = "评价ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "甜点ID")
    private Long dessertId;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "评分 1-5", example = "5")
    private Integer rating = 5;

    @Schema(description = "评价内容", example = "味道很好，推荐！")
    private String content;

    @Schema(description = "状态 1-显示 0-隐藏", example = "1")
    private Integer status = 1;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "用户昵称")
    private String userNickname;

    @Schema(description = "甜点名称")
    private String dessertName;

    @Schema(description = "甜点图片")
    private String dessertImage;
}
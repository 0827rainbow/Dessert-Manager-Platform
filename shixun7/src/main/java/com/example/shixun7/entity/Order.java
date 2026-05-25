package com.example.shixun7.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
public class Order {
    public static final int STATUS_PENDING = 1;
    public static final int STATUS_PAID = 2;
    public static final int STATUS_DELIVERING = 3;
    public static final int STATUS_COMPLETED = 4;
    public static final int STATUS_CANCELLED = 5;

    @Schema(description = "订单ID", example = "1")
    private Long id;

    @Schema(description = "订单号", example = "20250315123456")
    private String orderNo;

    private Long userId;

    @Schema(description = "总金额", example = "128.0")
    private Double totalAmount;

    @Schema(description = "优惠金额", example = "10.0")
    private Double discountAmount;

    @Schema(description = "实付金额", example = "118.0")
    private Double actualAmount;

    @Schema(description = "订单状态：1待付款 2已付款 3配送中 4已完成 5已取消", example = "1")
    private Integer status;

    @Schema(description = "收货地址", example = "北京市朝阳区xxx路1号")
    private String address;

    @Schema(description = "收货人姓名", example = "张三")
    private String receiverName;

    @Schema(description = "联系电话", example = "13800000000")
    private String receiverPhone;

    @Schema(description = "备注")
    private String remark;

    private Date payTime;
    private Date deliveryTime;
    private Date completeTime;
    private Date createTime;

    @Schema(description = "是否已评价：0未评价 1已评价")
    private Integer reviewed;

    @Schema(description = "订单项列表")
    private List<OrderItem> items;
}
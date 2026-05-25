package com.example.shixun7.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "直接购买创建订单请求参数")
public class OrderDirectRequest {

    @Schema(description = "甜点ID", example = "13", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long dessertId;

    @Schema(description = "购买数量", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer quantity;

    @Schema(description = "收货地址", example = "北京市朝阳区xxx路1号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String address;

    @Schema(description = "收货人姓名", example = "张三", requiredMode = Schema.RequiredMode.REQUIRED)
    private String receiverName;

    @Schema(description = "联系电话", example = "13800000000", requiredMode = Schema.RequiredMode.REQUIRED)
    private String receiverPhone;

    @Schema(description = "备注", example = "尽快发货")
    private String remark;
}
package com.example.shixun7.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
@Schema(description = "从购物车创建订单请求参数")
public class OrderFromCartRequest {

    @Schema(description = "收货地址", example = "北京市朝阳区xxx路1号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String address;

    @Schema(description = "收货人姓名", example = "张三", requiredMode = Schema.RequiredMode.REQUIRED)
    private String receiverName;

    @Schema(description = "联系电话", example = "13800000000", requiredMode = Schema.RequiredMode.REQUIRED)
    private String receiverPhone;

    @Schema(description = "备注", example = "请放快递柜")
    private String remark;

    @Schema(description = "选中的购物车商品ID列表（部分结算时使用）")
    private List<Long> cartItemIds;
}
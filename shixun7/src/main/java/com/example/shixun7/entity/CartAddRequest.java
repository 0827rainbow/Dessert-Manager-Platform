package com.example.shixun7.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "添加到购物车请求参数")
public class CartAddRequest {

    @Schema(description = "甜点ID", example = "13", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long dessertId;

    @Schema(description = "数量", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer quantity;
}
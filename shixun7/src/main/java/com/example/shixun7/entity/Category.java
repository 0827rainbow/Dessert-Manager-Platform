package com.example.shixun7.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
public class Category implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "分类ID", example = "1")
    private Long id;

    @Schema(description = "分类名称", example = "蛋糕系列", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "分类描述", example = "松软香甜，每一口都是幸福的味道")
    private String descp;
}
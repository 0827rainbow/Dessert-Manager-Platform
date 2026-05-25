package com.example.shixun7.entity;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class DessertQueryParam {
    private Long catId;       // 分类ID
    private String name;      // 甜点名称
    private String descp;     // 甜点描述
    private BigDecimal price1; // 最低价
    private BigDecimal price2; // 最高价
}

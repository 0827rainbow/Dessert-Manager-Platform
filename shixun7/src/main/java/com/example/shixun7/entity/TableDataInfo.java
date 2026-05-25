package com.example.shixun7.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
@Schema(description = "分页数据封装对象")
public class TableDataInfo {

    @Schema(description = "总记录数", example = "100")
    private long total;

    @Schema(description = "当前页数据列表")
    private List<?> rows;

    @Schema(description = "状态码", example = "200")
    private int code;

    @Schema(description = "提示信息", example = "success")
    private String msg;

    @Schema(description = "时间戳", example = "1700000000000")
    private long timestamp;

    public TableDataInfo() {
        this.code = 200;
        this.msg = "success";
        this.timestamp = System.currentTimeMillis();
    }
}
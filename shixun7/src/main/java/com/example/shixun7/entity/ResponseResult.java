package com.example.shixun7.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "统一响应结果")
public class ResponseResult {
    private int code;
    private String message;
    private Object data;

    public ResponseResult(int code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static ResponseResult success() {
        return new ResponseResult(200, "success", null);
    }

    public static ResponseResult success(Object data) {
        return new ResponseResult(200, "success", data);
    }

    public static ResponseResult error(String message) {
        return new ResponseResult(500, message, null);
    }

    public static ResponseResult error() {
        return new ResponseResult(500, "error", null);
    }
}
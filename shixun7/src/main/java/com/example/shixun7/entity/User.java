package com.example.shixun7.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.Date;

@Data
public class User {

    @Schema(description = "用户ID", example = "1")
    private Long id;

    @Schema(description = "用户名", example = "admin")
    private String username;

    @Schema(description = "密码")
    private String password;

    @Schema(description = "昵称", example = "管理员")
    private String nickname;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "邮箱", example = "admin@example.com")
    private String email;

    @Schema(description = "手机号", example = "13800000000")
    private String phone;

    @Schema(description = "角色：0普通用户 1管理员", example = "0")
    private Integer role;

    @Schema(description = "状态：0禁用 1启用", example = "1")
    private Integer status;

    private Date lastLoginTime;
    private Date createTime;
    private Date updateTime;
}
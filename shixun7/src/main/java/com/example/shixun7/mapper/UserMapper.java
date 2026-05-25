package com.example.shixun7.mapper;

import com.example.shixun7.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM user WHERE username = #{username}")
    User selectByUsername(String username);

    @Select("SELECT * FROM user WHERE id = #{id}")
    User selectById(Long id);

    @Insert("INSERT INTO user(username, password, nickname, email, phone, role) " +
            "VALUES(#{username}, #{password}, #{nickname}, #{email}, #{phone}, #{role})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Update("UPDATE user SET last_login_time = NOW() WHERE id = #{id}")
    int updateLoginTime(Long id);

    @Update("UPDATE user SET nickname = #{nickname}, email = #{email}, phone = #{phone} WHERE id = #{id}")
    int updateProfile(User user);

    // ========== 管理员接口方法 ==========

    @Select("<script>" +
            "SELECT id, username, nickname, avatar, email, phone, role, status, last_login_time, create_time, update_time " +
            "FROM user " +
            "<where>" +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (username LIKE CONCAT('%', #{keyword}, '%') " +
            "OR nickname LIKE CONCAT('%', #{keyword}, '%') " +
            "OR phone LIKE CONCAT('%', #{keyword}, '%'))" +
            "</if>" +
            "<if test='role != null'>" +
            "AND role = #{role}" +
            "</if>" +
            "</where>" +
            "ORDER BY id DESC" +
            "</script>")
    List<User> getUserList(@Param("keyword") String keyword, @Param("role") Integer role);

    @Update("UPDATE user SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Update("UPDATE user SET password = #{password} WHERE id = #{id}")
    int resetPassword(@Param("id") Long id, @Param("password") String password);

    // ========== 统计方法（新增） ==========

    /**
     * 获取用户总数
     */
    @Select("SELECT COUNT(*) FROM user")
    int getUserCount();

    /**
     * 获取今日注册用户数
     */
    @Select("SELECT COUNT(*) FROM user WHERE DATE(create_time) = CURDATE()")
    int getTodayRegisterCount();

    // ========== 更新用户方法 ==========

    @Update("<script>" +
            "UPDATE user " +
            "<set>" +
            "<if test='nickname != null'>nickname = #{nickname},</if>" +
            "<if test='email != null'>email = #{email},</if>" +
            "<if test='phone != null'>phone = #{phone},</if>" +
            "<if test='role != null'>role = #{role},</if>" +
            "<if test='status != null'>status = #{status},</if>" +
            "</set>" +
            "WHERE id = #{id}" +
            "</script>")
    int updateUser(User user);
}
package com.example.shixun7.mapper;

import com.example.shixun7.entity.Review;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ReviewMapper {

    @Select("SELECT c.*, u.nickname as user_nickname, d.name as dessert_name, d.photoUrl as dessert_image " +
            "FROM comment c " +
            "LEFT JOIN user u ON c.user_id = u.id " +
            "LEFT JOIN dessert d ON c.dessert_id = d.id " +
            "WHERE c.dessert_id = #{dessertId} AND c.status = 1 " +
            "ORDER BY c.create_time DESC")
    List<Review> selectByDessertId(Long dessertId);

    @Select("SELECT c.*, u.nickname as user_nickname, d.name as dessert_name " +
            "FROM comment c " +
            "LEFT JOIN user u ON c.user_id = u.id " +
            "LEFT JOIN dessert d ON c.dessert_id = d.id " +
            "WHERE c.user_id = #{userId} " +
            "ORDER BY c.create_time DESC")
    List<Review> selectByUserId(Long userId);

    @Select("SELECT * FROM comment WHERE order_id = #{orderId} AND dessert_id = #{dessertId}")
    Review selectByOrderAndDessert(@Param("orderId") Long orderId, @Param("dessertId") Long dessertId);

    // 去掉 images 字段
    @Insert("INSERT INTO comment(user_id, dessert_id, order_id, rating, content, status, create_time) " +
            "VALUES(#{userId}, #{dessertId}, #{orderId}, #{rating}, #{content}, #{status}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Review review);

    @Update("UPDATE dessert SET rating = (SELECT AVG(rating) FROM comment WHERE dessert_id = #{dessertId} AND rating IS NOT NULL), " +
            "review_count = (SELECT COUNT(*) FROM comment WHERE dessert_id = #{dessertId}) " +
            "WHERE id = #{dessertId}")
    int updateDessertRating(Long dessertId);

    @Delete("DELETE FROM comment WHERE id = #{id}")
    int deleteById(Long id);
}

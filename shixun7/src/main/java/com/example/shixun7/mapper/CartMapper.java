package com.example.shixun7.mapper;

import com.example.shixun7.entity.Cart;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CartMapper {

    @Select("SELECT c.*, d.name as dessert_name, d.photoUrl as dessert_photo, d.price as dessert_price " +
            "FROM cart c LEFT JOIN dessert d ON c.dessert_id = d.id WHERE c.user_id = #{userId}")
    List<Cart> selectByUserId(Long userId);

    @Insert("INSERT INTO cart(user_id, dessert_id, quantity) VALUES(#{userId}, #{dessertId}, #{quantity}) " +
            "ON DUPLICATE KEY UPDATE quantity = quantity + #{quantity}")
    int insertOrUpdate(Cart cart);

    @Update("UPDATE cart SET quantity = #{quantity} WHERE id = #{id} AND user_id = #{userId}")
    int updateQuantity(@Param("id") Long id, @Param("userId") Long userId, @Param("quantity") Integer quantity);

    @Delete("DELETE FROM cart WHERE id = #{id} AND user_id = #{userId}")
    int deleteById(@Param("id") Long id, @Param("userId") Long userId);

    @Delete("DELETE FROM cart WHERE user_id = #{userId}")
    int clearByUserId(Long userId);

    @Select("SELECT COALESCE(SUM(quantity), 0) FROM cart WHERE user_id = #{userId}")
    int getCountByUserId(Long userId);

    // ========== 新增方法 ==========

    /**
     * 根据ID查询单个购物车商品（用于归属校验）
     */
    @Select("SELECT * FROM cart WHERE id = #{id}")
    Cart selectById(Long id);

    /**
     * 根据ID列表批量查询购物车商品（用于结算）
     */
    @Select("<script>" +
            "SELECT c.*, d.name as dessert_name, d.photoUrl as dessert_photo, d.price as dessert_price " +
            "FROM cart c LEFT JOIN dessert d ON c.dessert_id = d.id " +
            "WHERE c.id IN " +
            "<foreach collection='ids' item='id' open='(' close=')' separator=','>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    List<Cart> selectByIds(@Param("ids") List<Long> ids);
}
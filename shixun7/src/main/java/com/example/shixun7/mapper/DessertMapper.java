package com.example.shixun7.mapper;

import com.example.shixun7.entity.Dessert;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DessertMapper {

    @Select("<script>" +
            "SELECT d.*, c.name as category_name FROM dessert d " +
            "LEFT JOIN category c ON d.cat_id = c.id " +
            "<where>" +
            "<if test='name != null and name != \"\"'>AND d.name LIKE CONCAT('%', #{name}, '%')</if>" +
            "<if test='catId != null and catId != 0'>AND d.cat_id = #{catId}</if>" +
            "<if test='status != null'>AND d.status = #{status}</if>" +
            "</where>" +
            "ORDER BY d.create_time DESC" +
            "</script>")
    List<Dessert> list(@Param("name") String name, @Param("catId") Long catId, @Param("status") Integer status);

    @Select("SELECT * FROM dessert WHERE id = #{id}")
    Dessert selectById(Long id);

    // 修正：originalPrice -> original_price
    @Insert("INSERT INTO dessert(name, descp, photoUrl, price, original_price, discount, " +
            "release_date, cat_id, stock, min_stock, status, is_recommend) " +
            "VALUES(#{name}, #{descp}, #{photoUrl}, #{price}, #{originalPrice}, #{discount}, " +
            "#{releaseDate}, #{catId}, #{stock}, #{minStock}, #{status}, #{isRecommend})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Dessert dessert);

    // 修正：originalPrice -> original_price
    @Update("<script>" +
            "UPDATE dessert " +
            "<set>" +
            "<if test='name != null'>name = #{name},</if>" +
            "<if test='descp != null'>descp = #{descp},</if>" +
            "<if test='photoUrl != null'>photoUrl = #{photoUrl},</if>" +
            "<if test='price != null'>price = #{price},</if>" +
            "<if test='originalPrice != null'>original_price = #{originalPrice},</if>" +
            "<if test='discount != null'>discount = #{discount},</if>" +
            "<if test='releaseDate != null'>release_date = #{releaseDate},</if>" +
            "<if test='catId != null'>cat_id = #{catId},</if>" +
            "<if test='stock != null'>stock = #{stock},</if>" +
            "<if test='minStock != null'>min_stock = #{minStock},</if>" +
            "<if test='status != null'>status = #{status},</if>" +
            "<if test='isRecommend != null'>is_recommend = #{isRecommend},</if>" +
            "</set>" +
            "WHERE id = #{id}" +
            "</script>")
    int update(Dessert dessert);

    @Delete("DELETE FROM dessert WHERE id = #{id}")
    int deleteById(Long id);

    @Update("UPDATE dessert SET stock = stock - #{quantity}, sales = sales + #{quantity} " +
            "WHERE id = #{dessertId} AND stock >= #{quantity}")
    int reduceStock(@Param("dessertId") Long dessertId, @Param("quantity") Integer quantity);

    @Update("UPDATE dessert SET stock = stock + #{quantity} WHERE id = #{dessertId}")
    int addStock(@Param("dessertId") Long dessertId, @Param("quantity") Integer quantity);

    @Select("SELECT * FROM dessert WHERE status = 1 AND is_recommend = 1 ORDER BY sales DESC LIMIT #{limit}")
    List<Dessert> getRecommendList(int limit);

    // ========== 统计方法 ==========
    @Select("SELECT COUNT(*) FROM dessert")
    int getCount();

    @Select("SELECT COUNT(*) FROM dessert WHERE stock <= min_stock")
    int getLowStockCount();

    // ========== 删除关联数据方法 ==========
    @Delete("DELETE FROM cart WHERE dessert_id = #{dessertId}")
    int deleteCartByDessertId(Long dessertId);

    @Delete("DELETE FROM comment WHERE dessert_id = #{dessertId}")
    int deleteCommentByDessertId(Long dessertId);

    @Delete("DELETE FROM order_item WHERE dessert_id = #{dessertId}")
    int deleteOrderItemByDessertId(Long dessertId);
}
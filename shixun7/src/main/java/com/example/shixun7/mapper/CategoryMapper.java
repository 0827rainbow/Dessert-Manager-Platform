package com.example.shixun7.mapper;

import com.example.shixun7.entity.Category;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CategoryMapper {

    /**
     * 新增分类
     */
    @Insert("INSERT INTO category(name, descp) VALUES(#{name}, #{descp})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Category category);

    /**
     * 条件查询分类列表（支持模糊查询）
     */
    @Select("<script>" +
            "SELECT id, name, descp FROM category " +
            "<where>" +
            "<if test='name != null and name != \"\"'>AND name LIKE CONCAT('%', #{name}, '%')</if>" +
            "<if test='descp != null and descp != \"\"'>AND descp LIKE CONCAT('%', #{descp}, '%')</if>" +
            "</where>" +
            "</script>")
    List<Category> list(Category category);

    /**
     * 编辑分类
     */
    @Update("<script>" +
            "UPDATE category " +
            "<set>" +
            "<if test='name != null'>name = #{name},</if>" +
            "<if test='descp != null'>descp = #{descp},</if>" +
            "</set>" +
            "WHERE id = #{id}" +
            "</script>")
    int edit(Category category);

    /**
     * 批量删除分类
     */
    @Delete("<script>" +
            "DELETE FROM category WHERE id IN " +
            "<foreach collection='array' item='id' open='(' close=')' separator=','>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int delete(Long[] ids);
}

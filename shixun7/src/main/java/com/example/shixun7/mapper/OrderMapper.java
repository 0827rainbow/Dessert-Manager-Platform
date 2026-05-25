package com.example.shixun7.mapper;

import com.example.shixun7.entity.Order;
import com.example.shixun7.entity.OrderItem;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface OrderMapper {

    @Insert("INSERT INTO `order`(order_no, user_id, total_amount, discount_amount, actual_amount, " +
            "address, receiver_name, receiver_phone, remark, status, reviewed) " +
            "VALUES(#{orderNo}, #{userId}, #{totalAmount}, #{discountAmount}, #{actualAmount}, " +
            "#{address}, #{receiverName}, #{receiverPhone}, #{remark}, #{status}, 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Order order);

    @Insert("INSERT INTO order_item(order_id, dessert_id, dessert_name, dessert_image, price, quantity, subtotal) " +
            "VALUES(#{orderId}, #{dessertId}, #{dessertName}, #{dessertImage}, #{price}, #{quantity}, #{subtotal})")
    int insertItem(OrderItem item);

    // 删除订单
    @Delete("DELETE FROM `order` WHERE id = #{id}")
    int deleteById(Long id);

    // 删除订单项
    @Delete("DELETE FROM order_item WHERE order_id = #{orderId}")
    int deleteItemsByOrderId(Long orderId);

    // 查询用户订单列表
    @Select("SELECT * FROM `order` WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Order> selectByUserId(Long userId);

    // 根据订单号查询
    @Select("SELECT * FROM `order` WHERE order_no = #{orderNo}")
    Order selectByOrderNo(String orderNo);

    // 根据ID查询订单
    @Select("SELECT * FROM `order` WHERE id = #{id}")
    Order selectById(Long id);

    // 查询订单项
    @Select("SELECT * FROM order_item WHERE order_id = #{orderId}")
    List<OrderItem> selectItemsByOrderId(Long orderId);

    // 更新订单状态
    @Update("UPDATE `order` SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    // 支付订单
    @Update("UPDATE `order` SET status = 2, pay_time = NOW() WHERE id = #{id}")
    int pay(Long id);

    // 管理员查询所有订单
    @Select("SELECT * FROM `order` ORDER BY create_time DESC")
    List<Order> selectAllOrders();

    // 管理员查询待发货订单（status=2）
    @Select("SELECT * FROM `order` WHERE status = 2 ORDER BY create_time DESC")
    List<Order> selectPendingDeliverOrders();

    // ========== 统计方法 ==========

    /**
     * 获取订单总数
     */
    @Select("SELECT COUNT(*) FROM `order`")
    int getCount();

    /**
     * 获取总销售额（已完成订单的实际金额总和）
     */
    @Select("SELECT COALESCE(SUM(actual_amount), 0) FROM `order` WHERE status = 4")
    Double getTotalSales();

    /**
     * 获取今日订单数
     */
    @Select("SELECT COUNT(*) FROM `order` WHERE DATE(create_time) = CURDATE()")
    int getTodayOrderCount();

    /**
     * 获取今日销售额
     */
    @Select("SELECT COALESCE(SUM(actual_amount), 0) FROM `order` WHERE status = 4 AND DATE(complete_time) = CURDATE()")
    Double getTodaySales();

    // ========== 修改地址方法 ==========

    /**
     * 更新订单地址信息
     */
    @Update("UPDATE `order` SET " +
            "receiver_name = #{receiverName}, " +
            "receiver_phone = #{receiverPhone}, " +
            "address = #{address}, " +
            "remark = #{remark} " +
            "WHERE id = #{orderId}")
    int updateAddress(@Param("orderId") Long orderId,
                      @Param("receiverName") String receiverName,
                      @Param("receiverPhone") String receiverPhone,
                      @Param("address") String address,
                      @Param("remark") String remark);
}
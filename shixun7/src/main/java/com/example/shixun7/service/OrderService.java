package com.example.shixun7.service;

import com.example.shixun7.entity.*;
import com.example.shixun7.mapper.CartMapper;
import com.example.shixun7.mapper.DessertMapper;
import com.example.shixun7.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private DessertMapper dessertMapper;

    private String generateOrderNo() {
        return new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) +
                UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    // ========== 统计方法 ==========

    public int getOrderCount() {
        return orderMapper.getCount();
    }

    public Double getTotalSales() {
        Double sales = orderMapper.getTotalSales();
        return sales != null ? sales : 0.0;
    }

    // ========== 管理员方法 ==========

    public List<Order> getAllOrders() {
        List<Order> orders = orderMapper.selectAllOrders();
        for (Order order : orders) {
            order.setItems(orderMapper.selectItemsByOrderId(order.getId()));
        }
        return orders;
    }

    public List<Order> getPendingDeliverOrders() {
        List<Order> orders = orderMapper.selectPendingDeliverOrders();
        for (Order order : orders) {
            order.setItems(orderMapper.selectItemsByOrderId(order.getId()));
        }
        return orders;
    }

    public Order getOrderById(Long orderId) {
        return orderMapper.selectById(orderId);
    }

    public boolean updateStatus(Long orderId, Integer status) {
        return orderMapper.updateStatus(orderId, status) > 0;
    }

    @Transactional
    public boolean confirmOrder(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            return false;
        }
        if (order.getStatus() != Order.STATUS_DELIVERING) {
            throw new RuntimeException("当前状态不能确认收货");
        }
        return orderMapper.updateStatus(orderId, Order.STATUS_COMPLETED) > 0;
    }

    // ========== 修改地址方法 ==========

    @Transactional
    public boolean updateAddress(Long orderId, String receiverName, String receiverPhone, String address, String remark) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            return false;
        }
        if (order.getStatus() != Order.STATUS_PENDING && order.getStatus() != Order.STATUS_PAID) {
            return false;
        }
        return orderMapper.updateAddress(orderId, receiverName, receiverPhone, address, remark) > 0;
    }

    // ========== 订单创建方法 ==========

    // 从购物车创建订单（全部商品）- 直接已付款
    @Transactional
    public Order createOrderFromCart(Long userId, String address, String receiverName,
                                     String receiverPhone, String remark) {
        if (address == null || address.trim().isEmpty()) {
            address = "默认收货地址";
        }
        if (receiverName == null || receiverName.trim().isEmpty()) {
            receiverName = "默认收货人";
        }
        if (receiverPhone == null || receiverPhone.trim().isEmpty()) {
            receiverPhone = "13800000000";
        }

        List<Cart> cartList = cartMapper.selectByUserId(userId);
        if (cartList.isEmpty()) {
            throw new RuntimeException("购物车为空");
        }

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setAddress(address);
        order.setReceiverName(receiverName);
        order.setReceiverPhone(receiverPhone);
        order.setRemark(remark);
        order.setStatus(Order.STATUS_PAID);  // 改为已付款状态
        order.setPayTime(new Date());         // 设置支付时间
        order.setItems(new ArrayList<>());

        Double total = 0.0;

        for (Cart cart : cartList) {
            Dessert dessert = dessertMapper.selectById(cart.getDessertId());
            if (dessert.getStock() < cart.getQuantity()) {
                throw new RuntimeException("库存不足: " + dessert.getName());
            }
            dessertMapper.reduceStock(dessert.getId(), cart.getQuantity());

            Double subtotal = dessert.getPrice() * cart.getQuantity();
            total += subtotal;

            OrderItem item = new OrderItem();
            item.setDessertId(dessert.getId());
            item.setDessertName(dessert.getName());
            item.setDessertImage(dessert.getPhotoUrl());
            item.setPrice(dessert.getPrice());
            item.setQuantity(cart.getQuantity());
            item.setSubtotal(subtotal);
            order.getItems().add(item);
        }

        order.setTotalAmount(total);
        order.setActualAmount(total);
        orderMapper.insert(order);

        for (OrderItem item : order.getItems()) {
            item.setOrderId(order.getId());
            orderMapper.insertItem(item);
        }

        cartMapper.clearByUserId(userId);
        return order;
    }

    // 根据选中的购物车商品ID创建订单（支持部分结算）- 直接已付款
    @Transactional
    public Order createOrderFromSelectedCart(Long userId, List<Long> cartItemIds,
                                             String address, String receiverName,
                                             String receiverPhone, String remark) {
        if (address == null || address.trim().isEmpty()) {
            address = "默认收货地址";
        }
        if (receiverName == null || receiverName.trim().isEmpty()) {
            receiverName = "默认收货人";
        }
        if (receiverPhone == null || receiverPhone.trim().isEmpty()) {
            receiverPhone = "13800000000";
        }

        List<Cart> cartList = cartMapper.selectByIds(cartItemIds);
        if (cartList.isEmpty()) {
            throw new RuntimeException("请选择要结算的商品");
        }

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setAddress(address);
        order.setReceiverName(receiverName);
        order.setReceiverPhone(receiverPhone);
        order.setRemark(remark);
        order.setStatus(Order.STATUS_PAID);  // 改为已付款状态
        order.setPayTime(new Date());         // 设置支付时间
        order.setItems(new ArrayList<>());

        Double total = 0.0;

        for (Cart cart : cartList) {
            Dessert dessert = dessertMapper.selectById(cart.getDessertId());
            if (dessert.getStock() < cart.getQuantity()) {
                throw new RuntimeException("库存不足: " + dessert.getName());
            }
            dessertMapper.reduceStock(dessert.getId(), cart.getQuantity());

            Double subtotal = dessert.getPrice() * cart.getQuantity();
            total += subtotal;

            OrderItem item = new OrderItem();
            item.setDessertId(dessert.getId());
            item.setDessertName(dessert.getName());
            item.setDessertImage(dessert.getPhotoUrl());
            item.setPrice(dessert.getPrice());
            item.setQuantity(cart.getQuantity());
            item.setSubtotal(subtotal);
            order.getItems().add(item);

            // 从购物车中删除已下单的商品
            cartMapper.deleteById(cart.getId(), userId);
        }

        order.setTotalAmount(total);
        order.setActualAmount(total);
        orderMapper.insert(order);

        for (OrderItem item : order.getItems()) {
            item.setOrderId(order.getId());
            orderMapper.insertItem(item);
        }

        return order;
    }

    // 直接购买创建订单 - 直接已付款
    @Transactional
    public Order createOrderDirect(Long userId, Long dessertId, Integer quantity,
                                   String address, String receiverName, String receiverPhone, String remark) {
        if (address == null || address.trim().isEmpty()) {
            address = "默认收货地址";
        }
        if (receiverName == null || receiverName.trim().isEmpty()) {
            receiverName = "默认收货人";
        }
        if (receiverPhone == null || receiverPhone.trim().isEmpty()) {
            receiverPhone = "13800000000";
        }

        Dessert dessert = dessertMapper.selectById(dessertId);
        if (dessert.getStock() < quantity) {
            throw new RuntimeException("库存不足");
        }
        dessertMapper.reduceStock(dessertId, quantity);

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setAddress(address);
        order.setReceiverName(receiverName);
        order.setReceiverPhone(receiverPhone);
        order.setRemark(remark);
        order.setStatus(Order.STATUS_PAID);  // 改为已付款状态
        order.setPayTime(new Date());         // 设置支付时间
        order.setItems(new ArrayList<>());

        Double subtotal = dessert.getPrice() * quantity;
        order.setTotalAmount(subtotal);
        order.setActualAmount(subtotal);
        orderMapper.insert(order);

        OrderItem item = new OrderItem();
        item.setOrderId(order.getId());
        item.setDessertId(dessertId);
        item.setDessertName(dessert.getName());
        item.setDessertImage(dessert.getPhotoUrl());
        item.setPrice(dessert.getPrice());
        item.setQuantity(quantity);
        item.setSubtotal(subtotal);
        order.getItems().add(item);
        orderMapper.insertItem(item);

        return order;
    }

    // 获取用户订单列表
    public List<Order> getUserOrders(Long userId) {
        List<Order> orders = orderMapper.selectByUserId(userId);
        for (Order order : orders) {
            order.setItems(orderMapper.selectItemsByOrderId(order.getId()));
        }
        return orders;
    }

    // 删除订单（只能删除已取消的订单）
    @Transactional
    public boolean deleteOrder(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            return false;
        }
        if (order.getStatus() != Order.STATUS_CANCELLED) {
            throw new RuntimeException("只能删除已取消的订单");
        }
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此订单");
        }
        orderMapper.deleteItemsByOrderId(orderId);
        return orderMapper.deleteById(orderId) > 0;
    }

    // 支付订单
    public boolean payOrder(Long orderId) {
        return orderMapper.pay(orderId) > 0;
    }

    // 取消订单
    @Transactional
    public boolean cancelOrder(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getStatus() != Order.STATUS_PENDING) {
            return false;
        }
        List<OrderItem> items = orderMapper.selectItemsByOrderId(orderId);
        for (OrderItem item : items) {
            dessertMapper.addStock(item.getDessertId(), item.getQuantity());
        }
        return orderMapper.updateStatus(orderId, Order.STATUS_CANCELLED) > 0;
    }
}
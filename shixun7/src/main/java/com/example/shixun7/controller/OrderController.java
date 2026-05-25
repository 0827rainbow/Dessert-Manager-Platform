package com.example.shixun7.controller;

import com.example.shixun7.entity.*;
import com.example.shixun7.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175"}, allowCredentials = "true")
@RestController
@RequestMapping("/order")
@Tag(name = "订单管理", description = "订单创建、支付、取消、发货、确认收货等操作")
public class OrderController {

    @Autowired
    private OrderService orderService;

    private Long getCurrentUserId(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return user != null ? user.getId() : null;
    }

    @GetMapping("/list")
    @Operation(summary = "查询我的订单列表", description = "返回当前登录用户的所有订单，包含订单项")
    public ResponseResult getUserOrders(HttpSession session) {
        Long userId = getCurrentUserId(session);
        if (userId == null) {
            return ResponseResult.error("请先登录");
        }
        List<Order> orders = orderService.getUserOrders(userId);
        return ResponseResult.success(orders);
    }

    @GetMapping("/admin/list")
    @Operation(summary = "管理员获取所有订单", description = "仅管理员可访问")
    public ResponseResult adminGetAllOrders(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != 1) {
            return ResponseResult.error("无权访问");
        }
        List<Order> orders = orderService.getAllOrders();
        return ResponseResult.success(orders);
    }

    @PostMapping("/createFromCart")
    @Operation(summary = "从购物车创建订单", description = "支持全部结算或部分结算（传入cartItemIds）")
    public ResponseResult createFromCart(@RequestBody OrderFromCartRequest request, HttpSession session) {
        Long userId = getCurrentUserId(session);
        if (userId == null) {
            return ResponseResult.error("请先登录");
        }

        String address = request.getAddress();
        String receiverName = request.getReceiverName();
        String receiverPhone = request.getReceiverPhone();
        String remark = request.getRemark();
        List<Long> cartItemIds = request.getCartItemIds();

        if (address == null || address.trim().isEmpty()) {
            return ResponseResult.error("收货地址不能为空");
        }
        if (receiverName == null || receiverName.trim().isEmpty()) {
            return ResponseResult.error("收货人姓名不能为空");
        }
        if (receiverPhone == null || receiverPhone.trim().isEmpty()) {
            return ResponseResult.error("联系电话不能为空");
        }

        try {
            Order order;
            if (cartItemIds != null && !cartItemIds.isEmpty()) {
                order = orderService.createOrderFromSelectedCart(userId, cartItemIds,
                        address, receiverName, receiverPhone, remark);
            } else {
                order = orderService.createOrderFromCart(userId, address, receiverName, receiverPhone, remark);
            }
            return ResponseResult.success(order);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.error(e.getMessage());
        }
    }

    @PostMapping("/createDirect")
    @Operation(summary = "直接购买创建订单", description = "直接购买指定甜点生成订单")
    public ResponseResult createDirect(@RequestBody OrderDirectRequest request, HttpSession session) {
        Long userId = getCurrentUserId(session);
        if (userId == null) {
            return ResponseResult.error("请先登录");
        }

        String address = request.getAddress();
        String receiverName = request.getReceiverName();
        String receiverPhone = request.getReceiverPhone();
        String remark = request.getRemark();

        if (address == null || address.trim().isEmpty()) {
            return ResponseResult.error("收货地址不能为空");
        }
        if (receiverName == null || receiverName.trim().isEmpty()) {
            return ResponseResult.error("收货人姓名不能为空");
        }
        if (receiverPhone == null || receiverPhone.trim().isEmpty()) {
            return ResponseResult.error("联系电话不能为空");
        }

        try {
            Order order = orderService.createOrderDirect(
                    userId,
                    request.getDessertId(),
                    request.getQuantity(),
                    address, receiverName, receiverPhone, remark
            );
            return ResponseResult.success(order);
        } catch (Exception e) {
            return ResponseResult.error(e.getMessage());
        }
    }

    @PostMapping("/pay/{orderId}")
    @Operation(summary = "支付订单", description = "根据订单ID完成支付，订单状态变为已付款")
    public ResponseResult payOrder(
            @Parameter(description = "订单ID", example = "1", required = true) @PathVariable Long orderId,
            HttpSession session) {
        Long userId = getCurrentUserId(session);
        if (userId == null) {
            return ResponseResult.error("请先登录");
        }
        Order order = orderService.getOrderById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            return ResponseResult.error("无权操作此订单");
        }
        if (order.getStatus() != Order.STATUS_PENDING) {
            return ResponseResult.error("当前状态无法支付");
        }
        boolean result = orderService.payOrder(orderId);
        return result ? ResponseResult.success() : ResponseResult.error("支付失败");
    }

    @PostMapping("/cancel/{orderId}")
    @Operation(summary = "取消订单", description = "仅待付款状态的订单可取消，取消后恢复库存")
    public ResponseResult cancelOrder(
            @Parameter(description = "订单ID", example = "1", required = true) @PathVariable Long orderId,
            HttpSession session) {
        Long userId = getCurrentUserId(session);
        if (userId == null) {
            return ResponseResult.error("请先登录");
        }
        boolean result = orderService.cancelOrder(orderId);
        return result ? ResponseResult.success() : ResponseResult.error("取消失败");
    }

    @DeleteMapping("/{orderId}")
    @Operation(summary = "删除订单", description = "只能删除已取消的订单")
    public ResponseResult deleteOrder(
            @Parameter(description = "订单ID", example = "1", required = true) @PathVariable Long orderId,
            HttpSession session) {
        Long userId = getCurrentUserId(session);
        if (userId == null) {
            return ResponseResult.error("请先登录");
        }
        try {
            boolean result = orderService.deleteOrder(orderId, userId);
            return result ? ResponseResult.success() : ResponseResult.error("删除失败");
        } catch (Exception e) {
            return ResponseResult.error(e.getMessage());
        }
    }

    @PutMapping("/deliver/{orderId}")
    @Operation(summary = "发货", description = "仅管理员可以发货")
    public ResponseResult deliverOrder(
            @Parameter(description = "订单ID", example = "1", required = true) @PathVariable Long orderId,
            HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != 1) {
            return ResponseResult.error("只有管理员可以发货");
        }
        boolean result = orderService.updateStatus(orderId, 3);
        return result ? ResponseResult.success() : ResponseResult.error("发货失败");
    }

    @PutMapping("/confirm/{orderId}")
    @Operation(summary = "确认收货", description = "用户确认收货，订单状态变为已完成")
    public ResponseResult confirmOrder(
            @Parameter(description = "订单ID", example = "1", required = true) @PathVariable Long orderId,
            HttpSession session) {
        Long userId = getCurrentUserId(session);
        if (userId == null) {
            return ResponseResult.error("请先登录");
        }
        try {
            boolean result = orderService.confirmOrder(orderId, userId);
            return result ? ResponseResult.success() : ResponseResult.error("确认收货失败");
        } catch (Exception e) {
            return ResponseResult.error(e.getMessage());
        }
    }

    @PutMapping("/address/{orderId}")
    @Operation(summary = "修改订单地址", description = "仅管理员可以修改")
    public ResponseResult updateAddress(
            @Parameter(description = "订单ID", example = "1", required = true) @PathVariable Long orderId,
            @RequestBody Map<String, String> params,
            HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != 1) {
            return ResponseResult.error("只有管理员可以修改订单地址");
        }

        String receiverName = params.get("receiverName");
        String receiverPhone = params.get("receiverPhone");
        String address = params.get("address");
        String remark = params.get("remark");

        boolean result = orderService.updateAddress(orderId, receiverName, receiverPhone, address, remark);
        return result ? ResponseResult.success() : ResponseResult.error("修改地址失败");
    }
}
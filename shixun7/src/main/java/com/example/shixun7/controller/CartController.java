package com.example.shixun7.controller;

import com.example.shixun7.entity.*;
import com.example.shixun7.service.CartService;
import com.example.shixun7.mapper.DessertMapper;   // 新增：用于库存校验
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RestController
@RequestMapping("/cart")
@Tag(name = "购物车管理", description = "购物车商品添加、数量修改、删除及清空操作")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private DessertMapper dessertMapper;   // 库存校验依赖

    private Long getUserId(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return user != null ? user.getId() : null;
    }

    @GetMapping
    @Operation(summary = "获取购物车列表", description = "返回当前登录用户的购物车所有商品")
    public ResponseResult getCart(HttpSession session) {
        Long userId = getUserId(session);
        if (userId == null) {
            return ResponseResult.error("请先登录");
        }
        return ResponseResult.success(cartService.getCart(userId));
    }

    @GetMapping("/count")
    @Operation(summary = "获取购物车商品总数", description = "返回购物车中商品的总数量")
    public ResponseResult getCount(HttpSession session) {
        Long userId = getUserId(session);
        if (userId == null) {
            return ResponseResult.success(new HashMap<String, Integer>() {{ put("count", 0); }});
        }
        Map<String, Integer> result = new HashMap<>();
        result.put("count", cartService.getCartCount(userId));
        return ResponseResult.success(result);
    }

    @PostMapping("/add")
    @Operation(summary = "添加商品到购物车", description = "指定甜点ID和数量，若已存在则累加数量，并校验库存")
    public ResponseResult addToCart(@RequestBody CartAddRequest request, HttpSession session) {
        Long userId = getUserId(session);
        if (userId == null) {
            return ResponseResult.error("请先登录");
        }

        Long dessertId = request.getDessertId();
        Integer quantity = request.getQuantity();

        // 库存校验（来自第二个版本）
        Dessert dessert = dessertMapper.selectById(dessertId);
        if (dessert == null || dessert.getStock() < quantity) {
            return ResponseResult.error("库存不足，当前库存只有 " + (dessert != null ? dessert.getStock() : 0) + " 件");
        }

        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setDessertId(dessertId);
        cart.setQuantity(quantity);
        cartService.addToCart(cart);
        return ResponseResult.success();
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改购物车商品数量", description = "更新指定购物车项的数量，若数量≤0则删除该项，并校验库存和归属")
    public ResponseResult updateQuantity(
            @Parameter(description = "购物车项ID", example = "1", required = true) @PathVariable Long id,
            // 【修改点1】将 @RequestParam Integer quantity 改为 @RequestBody Map<String, Integer> body
            @RequestBody Map<String, Integer> body,
            HttpSession session) {
        Long userId = getUserId(session);
        if (userId == null) {
            return ResponseResult.error("请先登录");
        }

        // 【修改点2】从 body 中提取 quantity 参数
        Integer quantity = body.get("quantity");
        if (quantity == null) {
            return ResponseResult.error("请提供数量参数");
        }

        // 归属校验
        Cart cart = cartService.getCartById(id);
        if (cart == null || !cart.getUserId().equals(userId)) {
            return ResponseResult.error("购物车商品不存在");
        }

        // 库存校验
        Dessert dessert = dessertMapper.selectById(cart.getDessertId());
        if (dessert == null || dessert.getStock() < quantity) {
            int maxStock = dessert != null ? dessert.getStock() : 0;
            return ResponseResult.error("库存不足，最多只能购买 " + maxStock + " 件");
        }

        cartService.updateQuantity(id, userId, quantity);
        return ResponseResult.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除购物车商品", description = "从购物车中移除指定商品")
    public ResponseResult removeFromCart(
            @Parameter(description = "购物车项ID", example = "1", required = true) @PathVariable Long id,
            HttpSession session) {
        Long userId = getUserId(session);
        if (userId == null) {
            return ResponseResult.error("请先登录");
        }
        cartService.removeFromCart(id, userId);
        return ResponseResult.success();
    }

    @DeleteMapping("/clear")
    @Operation(summary = "清空购物车", description = "删除当前用户购物车中的所有商品")
    public ResponseResult clearCart(HttpSession session) {
        Long userId = getUserId(session);
        if (userId == null) {
            return ResponseResult.error("请先登录");
        }
        cartService.clearCart(userId);
        return ResponseResult.success();
    }

    @PostMapping("/getByIds")
    @Operation(summary = "根据ID列表获取购物车商品", description = "用于结算时批量查询选中的商品")
    public ResponseResult getCartByIds(@RequestBody List<Long> ids, HttpSession session) {
        Long userId = getUserId(session);
        if (userId == null) {
            return ResponseResult.error("请先登录");
        }
        List<Cart> cartList = cartService.getCartByIds(ids);
        return ResponseResult.success(cartList);
    }
}
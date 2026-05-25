package com.example.shixun7.service;

import com.example.shixun7.entity.Cart;
import com.example.shixun7.mapper.CartMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CartService {

    @Autowired
    private CartMapper cartMapper;

    public List<Cart> getCart(Long userId) {
        return cartMapper.selectByUserId(userId);
    }

    public int getCartCount(Long userId) {
        return cartMapper.getCountByUserId(userId);
    }

    @Transactional
    public void addToCart(Cart cart) {
        cartMapper.insertOrUpdate(cart);
    }

    @Transactional
    public void updateQuantity(Long id, Long userId, Integer quantity) {
        if (quantity <= 0) {
            cartMapper.deleteById(id, userId);
        } else {
            cartMapper.updateQuantity(id, userId, quantity);
        }
    }

    @Transactional
    public void removeFromCart(Long id, Long userId) {
        cartMapper.deleteById(id, userId);
    }

    public Cart getCartById(Long id) {
        return cartMapper.selectById(id);
    }
    @Transactional
    public void clearCart(Long userId) {
        cartMapper.clearByUserId(userId);
    }

    public List<Cart> getCartByIds(List<Long> ids) {
        return cartMapper.selectByIds(ids);
    }
}
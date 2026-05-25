package com.example.shixun7.service;

import com.example.shixun7.entity.Dessert;
import com.example.shixun7.mapper.DessertMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DessertService {

    @Autowired
    private DessertMapper dessertMapper;

    public List<Dessert> list(String name, Long catId, Integer status) {
        return dessertMapper.list(name, catId, status);
    }

    public Dessert getById(Long id) {
        return dessertMapper.selectById(id);
    }

    public boolean add(Dessert dessert) {
        return dessertMapper.insert(dessert) > 0;
    }

    public boolean update(Dessert dessert) {
        return dessertMapper.update(dessert) > 0;
    }

    @Transactional
    public boolean delete(Long id) {
        // 1. 删除购物车中的关联记录
        dessertMapper.deleteCartByDessertId(id);
        // 2. 删除评价表中的关联记录
        dessertMapper.deleteCommentByDessertId(id);
        // 3. 删除订单明细表中的关联记录
        dessertMapper.deleteOrderItemByDessertId(id);
        // 4. 最后删除甜点本身
        return dessertMapper.deleteById(id) > 0;
    }

    public boolean reduceStock(Long dessertId, Integer quantity) {
        return dessertMapper.reduceStock(dessertId, quantity) > 0;
    }

    public List<Dessert> getRecommendList(int limit) {
        return dessertMapper.getRecommendList(limit);
    }

    public int getDessertCount() {
        return dessertMapper.getCount();
    }

    public int getLowStockCount() {
        return dessertMapper.getLowStockCount();
    }
}
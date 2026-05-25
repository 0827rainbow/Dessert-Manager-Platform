package com.example.shixun7.service;
import com.example.shixun7.entity.Category;
import com.example.shixun7.mapper.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    public int add(Category category) {
        return categoryMapper.insert(category);
    }

    public int delete(Long[] ids) {
        return categoryMapper.delete(ids);
    }

    public int update(Category category) {
        return categoryMapper.edit(category);
    }

    public List<Category> query(Category category) {
        return categoryMapper.list(category);
    }
}


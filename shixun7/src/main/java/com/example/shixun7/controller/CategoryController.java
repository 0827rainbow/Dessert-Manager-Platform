package com.example.shixun7.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.example.shixun7.entity.Category;
import com.example.shixun7.entity.ResponseResult;
import com.example.shixun7.entity.TableDataInfo;
import com.example.shixun7.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/category")
@Tag(name = "分类管理", description = "甜点分类的增删改查接口")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @PostMapping
    @Operation(summary = "新增分类")
    public ResponseResult add(@RequestBody Category category) {
        return categoryService.add(category) > 0 ? ResponseResult.success() : ResponseResult.error("添加失败");
    }

    @GetMapping
    @Operation(summary = "分页查询分类列表")
    public TableDataInfo list(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "排序字段", example = "name") @RequestParam(defaultValue = "name") String orderByColumn,
            @Parameter(description = "排序方式", example = "asc") @RequestParam(defaultValue = "asc") String isAsc,
            Category category) {

        String orderBy = orderByColumn + " " + isAsc;
        PageHelper.startPage(pageNum, pageSize, orderBy);

        List<Category> list = categoryService.query(category);
        PageInfo<Category> pageInfo = new PageInfo<>(list);

        TableDataInfo info = new TableDataInfo();
        info.setCode(200);
        info.setMsg("success");
        info.setRows(list);
        info.setTotal(pageInfo.getTotal());
        info.setTimestamp(System.currentTimeMillis());

        return info;
    }

    @PutMapping
    @Operation(summary = "编辑分类")
    public ResponseResult edit(@RequestBody Category category) {
        return categoryService.update(category) > 0 ? ResponseResult.success() : ResponseResult.error("编辑失败");
    }

    @DeleteMapping("/{ids}")
    @Operation(summary = "删除分类", description = "支持批量删除，多个ID用逗号分隔")
    public ResponseResult remove(@Parameter(description = "分类ID，多个用逗号分隔", required = true) @PathVariable Long[] ids) {
        return categoryService.delete(ids) > 0 ? ResponseResult.success() : ResponseResult.error("删除失败");
    }
}
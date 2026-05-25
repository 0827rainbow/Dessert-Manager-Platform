package com.example.shixun7.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.example.shixun7.entity.Dessert;
import com.example.shixun7.entity.ResponseResult;
import com.example.shixun7.entity.TableDataInfo;
import com.example.shixun7.service.DessertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RestController
@RequestMapping("/dessert")
@Tag(name = "甜点管理", description = "甜点的增删改查及上传图片接口")
public class DessertController {

    @Autowired
    private DessertService dessertService;

    @GetMapping("/list")
    @Operation(summary = "分页查询甜点列表", description = "支持按名称、分类、状态筛选，可排序")
    public TableDataInfo list(
            @Parameter(description = "甜点名称（模糊匹配）") @RequestParam(required = false) String name,
            @Parameter(description = "分类ID") @RequestParam(required = false) Long catId,
            @Parameter(description = "状态：0下架 1上架") @RequestParam(required = false) Integer status,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小", example = "12") @RequestParam(defaultValue = "12") Integer pageSize,
            @Parameter(description = "排序字段", example = "id") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "排序方式：asc/desc", example = "desc") @RequestParam(defaultValue = "desc") String order) {
        String orderBy = sortBy + " " + order;
        PageHelper.startPage(pageNum, pageSize, orderBy);
        List<Dessert> list = dessertService.list(name, catId, status);
        PageInfo<Dessert> pageInfo = new PageInfo<>(list);

        TableDataInfo info = new TableDataInfo();
        info.setRows(list);
        info.setTotal(pageInfo.getTotal());
        return info;
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询甜点详情")
    public ResponseResult getById(@Parameter(description = "甜点ID", required = true) @PathVariable Long id) {
        return ResponseResult.success(dessertService.getById(id));
    }

    @GetMapping("/recommend")
    @Operation(summary = "获取推荐甜点列表", description = "根据销量排序，取前limit条")
    public ResponseResult getRecommend(@Parameter(description = "获取数量", example = "6") @RequestParam(defaultValue = "6") Integer limit) {
        return ResponseResult.success(dessertService.getRecommendList(limit));
    }

    @PostMapping
    @Operation(summary = "新增甜点")
    public ResponseResult add(@RequestBody Dessert dessert) {
        return dessertService.add(dessert) ? ResponseResult.success() : ResponseResult.error("添加失败");
    }

    @PutMapping
    @Operation(summary = "修改甜点")
    public ResponseResult update(@RequestBody Dessert dessert) {
        return dessertService.update(dessert) ? ResponseResult.success() : ResponseResult.error("更新失败");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除甜点")
    public ResponseResult delete(@Parameter(description = "甜点ID", required = true) @PathVariable Long id) {
        return dessertService.delete(id) ? ResponseResult.success() : ResponseResult.error("删除失败");
    }

    @PostMapping("/upload")
    @Operation(summary = "上传甜点图片", description = "返回图片访问路径")
    public ResponseResult upload(@Parameter(description = "图片文件", required = true) @RequestParam("file") MultipartFile file) throws IOException {
        File dir = new File("./photo");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String ext = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
        String fileName = UUID.randomUUID().toString().replace("-", "") + ext;
        File dest = new File(dir, fileName);
        FileUtils.copyInputStreamToFile(file.getInputStream(), dest);
        return ResponseResult.success("/photo/" + fileName);
    }
}
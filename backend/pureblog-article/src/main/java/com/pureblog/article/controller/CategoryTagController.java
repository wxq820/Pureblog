package com.pureblog.article.controller;

import com.pureblog.article.entity.CategoryDO;
import com.pureblog.article.entity.TagDO;
import com.pureblog.article.mapper.CategoryMapper;
import com.pureblog.article.mapper.TagMapper;
import com.pureblog.common.enums.ErrorCode;
import com.pureblog.common.exception.BusinessException;
import com.pureblog.common.result.ApiResponse;
import com.pureblog.common.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class CategoryTagController {

    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;

    @PostMapping("/category/create")
    public ApiResponse<Void> createCategory(@RequestBody CategoryDO category) {
        if (StringUtils.isBlank(category.getName())) throw new BusinessException(ErrorCode.PARAM_INVALID);
        categoryMapper.insert(category);
        return ApiResponse.success();
    }

    @PutMapping("/category/update")
    public ApiResponse<Void> updateCategory(@RequestBody CategoryDO category) {
        categoryMapper.updateById(category);
        return ApiResponse.success();
    }

    @DeleteMapping("/category/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        categoryMapper.deleteById(id);
        return ApiResponse.success();
    }

    @PostMapping("/tag/create")
    public ApiResponse<Void> createTag(@RequestBody TagDO tag) {
        if (StringUtils.isBlank(tag.getName())) throw new BusinessException(ErrorCode.PARAM_INVALID);
        tagMapper.insert(tag);
        return ApiResponse.success();
    }

    @PutMapping("/tag/update")
    public ApiResponse<Void> updateTag(@RequestBody TagDO tag) {
        tagMapper.updateById(tag);
        return ApiResponse.success();
    }

    @DeleteMapping("/tag/{id}")
    public ApiResponse<Void> deleteTag(@PathVariable Long id) {
        tagMapper.deleteById(id);
        return ApiResponse.success();
    }
}

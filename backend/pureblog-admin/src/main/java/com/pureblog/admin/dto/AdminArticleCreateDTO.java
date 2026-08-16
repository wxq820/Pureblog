package com.pureblog.admin.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class AdminArticleCreateDTO {

    @NotBlank(message = "标题不能为空")
    @Size(max = 255)
    private String title;

    @Size(max = 500)
    private String summary;

    private String coverUrl;

    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    @NotNull(message = "目录树叶子节点ID不能为空")
    private Long treeNodeId;

    private List<Long> tagIds;

    @NotBlank(message = "正文不能为空")
    private String content;

    private String htmlContent;
}

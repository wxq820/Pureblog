package com.pureblog.article.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class ArticleUpdateDTO {
    
    @NotNull(message = "文章ID不能为空")
    private Long id;
    
    @NotBlank(message = "标题不能为空")
    @Size(max = 255, message = "标题长度不超过255个字符")
    private String title;
    
    @Size(max = 500, message = "摘要长度不超过500个字符")
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

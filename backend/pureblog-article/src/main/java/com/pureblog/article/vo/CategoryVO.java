package com.pureblog.article.vo;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryVO {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private Integer articleCount;
}

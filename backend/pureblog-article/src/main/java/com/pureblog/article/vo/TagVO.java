package com.pureblog.article.vo;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagVO {
    private Long id;
    private String name;
    private String slug;
    private Integer articleCount;
}

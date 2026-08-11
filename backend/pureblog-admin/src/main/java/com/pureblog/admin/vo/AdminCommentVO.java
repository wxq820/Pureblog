package com.pureblog.admin.vo;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCommentVO {
    private Long id;
    private Long articleId;
    private String articleTitle;
    private Long userId;
    private String username;
    private String nickname;
    private String content;
    private String status;
    private LocalDateTime createdAt;
}

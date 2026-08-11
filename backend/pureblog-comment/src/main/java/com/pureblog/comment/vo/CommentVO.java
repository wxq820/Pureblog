package com.pureblog.comment.vo;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentVO {
    private Long id;
    private Long articleId;
    private Long userId;
    private String username;
    private String nickname;
    private String avatarUrl;
    private Long parentId;
    private Long replyToId;
    private Long replyToUid;
    private String replyToNickname;
    private String content;
    private Integer likeCount;
    private Boolean isLiked;
    private String status;
    private LocalDateTime createdAt;
    private List<CommentVO> replies;
}

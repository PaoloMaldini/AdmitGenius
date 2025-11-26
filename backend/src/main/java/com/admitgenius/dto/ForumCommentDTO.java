package com.admitgenius.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ForumCommentDTO {
    private Long id;
    private String content;
    private Long parentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 作者信息
    private Long authorId;
    private String authorName;
    private String authorEmail;
    private String authorProfilePicture;
}
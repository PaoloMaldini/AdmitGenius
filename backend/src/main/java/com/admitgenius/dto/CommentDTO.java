package com.admitgenius.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CommentDTO {
    private Long id;
    private Long postId;
    private String postTitle;
    private Long authorId;
    private String authorName;
    private String authorRole;
    private String authorProfilePicture;
    private UserDTO author;
    private String content;
    private LocalDateTime createdAt;
    private Integer likes;
}
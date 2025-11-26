package com.admitgenius.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ForumPostDTO {
    private Long id;
    private UserDTO author;
    private String title;
    private String content;
    private String category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer likes;
    private Integer commentCount;
    private Integer views;
    private List<CommentDTO> comments;
    private boolean likedByCurrentUser;
    private List<String> tags;
    private String excerpt;
    private Long authorId;
    private String authorName;
    private String authorProfilePicture;

    // 专家帖子相关字段
    private Boolean isExpertPost;
    private String expertTag;
}
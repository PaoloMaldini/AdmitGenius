package com.admitgenius.controller;

import com.admitgenius.dto.ApiResponse;
import com.admitgenius.dto.CommentDTO;
import com.admitgenius.dto.ForumPostDTO;
import com.admitgenius.dto.UserDTO;
import com.admitgenius.service.ForumService;
import com.admitgenius.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/forum")
public class ForumController {
    @Autowired
    private ForumService forumService;

    @Autowired
    private UserService userService;

    // 辅助方法，从 Authentication 对象获取用户ID (与AdminController中的类似)
    private Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            // 对于某些公共接口，可能允许匿名访问，此时userId可以为null
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            try {
                UserDTO user = userService.findByEmail(username);
                return user.getId();
            } catch (Exception e) {
                // 如果找不到用户，记录日志但不抛出异常，允许匿名访问
                System.err.println("警告: 无法根据认证信息找到用户: " + username + ", 错误: " + e.getMessage());
                return null;
            }
        } else if (principal instanceof String) {
            if ("anonymousUser".equals(principal)) {
                return null; // 明确处理匿名用户
            }
            // 避免将其他字符串传递给 findByEmail
            try {
                UserDTO user = userService.findByEmail((String) principal);
                return user.getId();
            } catch (Exception e) {
                System.err.println("警告: 无法根据认证信息找到用户: " + principal + ", 错误: " + e.getMessage());
                return null;
            }
        }
        // 如果 principal 不是预期的类型，也视为无法获取用户ID，返回null
        return null;
    }

    // 获取帖子列表，支持分页和搜索
    @GetMapping("/posts")
    public ResponseEntity<ApiResponse<Page<ForumPostDTO>>> getAllPosts(
            Pageable pageable,
            @RequestParam(required = false) String category,
            @RequestParam(required = false, name = "query") String searchQuery,
            @RequestParam(required = false, name = "sort") String sortOption,
            Authentication authentication) {

        try {
            System.out.println("开始获取帖子列表");
            System.out.println("认证信息: " + (authentication != null ? authentication.getName() : "未认证"));

            // 获取用户ID（可以为null，表示未登录用户）
            Long userId = null;
            try {
                userId = getUserIdFromAuthentication(authentication);
                System.out.println("当前用户ID: " + userId);
            } catch (Exception e) {
                System.err.println("获取用户ID时出错: " + e.getMessage());
                // 不抛出异常，继续处理，将用户视为未登录
            }

            // Default page and size if not provided by Pageable
            int page = pageable.isPaged() ? pageable.getPageNumber() : 0;
            int size = pageable.isPaged() ? pageable.getPageSize() : 10;
            System.out.println("分页信息 - 页码: " + page + ", 每页大小: " + size);

            Sort sort = Sort.by(Sort.Direction.DESC, "createdAt"); // Default sort

            if (sortOption != null) {
                System.out.println("排序选项: " + sortOption);
                switch (sortOption) {
                    case "latest":
                        sort = Sort.by(Sort.Direction.DESC, "createdAt");
                        break;
                    case "popular":
                        sort = Sort.by(Sort.Direction.DESC, "likes");
                        break;
                    case "replies":
                        // 对于回复最多的排序，我们需要使用服务层的特殊处理
                        // 这里先用默认排序，在服务层中处理评论数排序
                        sort = Sort.by(Sort.Direction.DESC, "createdAt");
                        break;
                    default:
                        sort = Sort.by(Sort.Direction.DESC, "createdAt");
                        break;
                }
            }

            Pageable effectivePageable = PageRequest.of(page, size, sort);

            // 获取帖子列表
            System.out.println("开始调用 forumService.getAllPosts");
            Page<ForumPostDTO> posts = forumService.getAllPosts(effectivePageable, searchQuery, category, sortOption);
            System.out.println("成功获取帖子列表，总数: " + (posts != null ? posts.getTotalElements() : 0));

            if (posts == null || !posts.hasContent()) {
                System.out.println("没有找到帖子");
                return ResponseEntity.ok(ApiResponse.success(Page.empty(), "没有找到帖子"));
            }

            return ResponseEntity.ok(ApiResponse.success(posts, "获取帖子列表成功"));
        } catch (Exception e) {
            System.err.println("获取帖子列表时发生错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(ApiResponse.error("获取帖子列表失败：" + e.getMessage()));
        }
    }

    // 创建帖子 (需要认证，userId应从安全上下文中获取)
    @PostMapping("/posts")
    public ResponseEntity<ApiResponse<ForumPostDTO>> createPost(@RequestBody ForumPostDTO postDTO,
            Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            if (userId == null) {
                return ResponseEntity.ok(ApiResponse.error("用户未登录"));
            }

            System.out.println("创建帖子，用户ID: " + userId + ", 标题: " + postDTO.getTitle());

            ForumPostDTO createdPost = forumService.createPost(postDTO, userId);

            System.out.println("帖子创建成功，帖子ID: " + createdPost.getId());

            return ResponseEntity.ok(ApiResponse.success(createdPost, "帖子创建成功"));
        } catch (Exception e) {
            System.err.println("创建帖子时发生错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(ApiResponse.error("创建帖子失败：" + e.getMessage()));
        }
    }

    // 获取帖子详情
    @GetMapping("/posts/{postId}")
    public ResponseEntity<ApiResponse<ForumPostDTO>> getPostById(@PathVariable Long postId,
            Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication); // userId 可以为 null，表示匿名查看
            System.out.println("获取帖子详情，ID: " + postId + ", 当前用户ID: " + userId);

            ForumPostDTO post = forumService.getPostById(postId, userId);
            System.out.println("成功获取帖子详情: " + post.getTitle());

            return ResponseEntity.ok(ApiResponse.success(post, "获取帖子详情成功"));
        } catch (EntityNotFoundException e) {
            System.err.println("帖子不存在: " + e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("未找到该帖子或已被删除"));
        } catch (Exception e) {
            System.err.println("获取帖子详情时发生错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(ApiResponse.error("获取帖子详情失败：" + e.getMessage()));
        }
    }

    // 发表评论 (需要认证，userId应从安全上下文中获取)
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<CommentDTO>> addComment(@PathVariable Long postId,
            @RequestBody CommentDTO commentDTO,
            Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            if (userId == null) {
                return ResponseEntity.ok(ApiResponse.error("用户未登录"));
            }

            System.out.println("发表评论，帖子ID: " + postId + ", 用户ID: " + userId + ", 内容: " + commentDTO.getContent());

            CommentDTO createdComment = forumService.addComment(postId, commentDTO, userId);

            System.out.println("评论发表成功，评论ID: " + createdComment.getId());

            return ResponseEntity.ok(ApiResponse.success(createdComment, "评论发表成功"));
        } catch (EntityNotFoundException e) {
            System.err.println("发表评论失败: " + e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("帖子不存在或用户不存在"));
        } catch (Exception e) {
            System.err.println("发表评论时发生错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(ApiResponse.error("评论发表失败：" + e.getMessage()));
        }
    }

    // 点赞帖子 (需要认证)
    @PostMapping("/posts/{postId}/toggle-like")
    public ResponseEntity<ApiResponse<ForumPostDTO>> togglePostLike(@PathVariable Long postId,
            Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            if (userId == null) {
                return ResponseEntity.ok(ApiResponse.error("用户未登录"));
            }

            System.out.println("点赞操作，帖子ID: " + postId + ", 用户ID: " + userId);

            ForumPostDTO updatedPost = forumService.togglePostLike(postId, userId);

            System.out.println("点赞操作成功，当前点赞状态: " + updatedPost.isLikedByCurrentUser());

            return ResponseEntity.ok(ApiResponse.success(updatedPost, "操作成功"));
        } catch (EntityNotFoundException e) {
            System.err.println("点赞操作失败: " + e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("帖子不存在或用户不存在"));
        } catch (Exception e) {
            System.err.println("点赞操作时发生错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(ApiResponse.error("操作失败：" + e.getMessage()));
        }
    }

    // 删除帖子 (需要认证，只有作者或管理员可以删除)
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<ApiResponse<String>> deletePost(@PathVariable Long postId, Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            if (userId == null) {
                return ResponseEntity.ok(ApiResponse.error("用户未登录"));
            }

            System.out.println("删除帖子，帖子ID: " + postId + ", 用户ID: " + userId);

            forumService.deletePost(postId, userId);

            System.out.println("帖子删除成功，帖子ID: " + postId);

            return ResponseEntity.ok(ApiResponse.success("删除成功", "帖子删除成功"));
        } catch (EntityNotFoundException e) {
            System.err.println("删除帖子失败: " + e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("帖子不存在或用户不存在"));
        } catch (AccessDeniedException e) {
            System.err.println("删除帖子权限不足: " + e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("权限不足：" + e.getMessage()));
        } catch (Exception e) {
            System.err.println("删除帖子时发生错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(ApiResponse.error("删除失败：" + e.getMessage()));
        }
    }

    // 获取用户发布的帖子
    @GetMapping("/users/{userId}/posts")
    public ResponseEntity<ApiResponse<Page<ForumPostDTO>>> getUserPosts(
            @PathVariable Long userId,
            Pageable pageable,
            Authentication authentication) {
        try {
            System.out.println("获取用户帖子，用户ID: " + userId);

            Long currentUserId = getUserIdFromAuthentication(authentication);
            System.out.println("当前用户ID: " + currentUserId);

            Page<ForumPostDTO> userPosts = forumService.getUserPosts(userId, pageable, currentUserId);
            System.out.println("成功获取用户帖子，总数: " + (userPosts != null ? userPosts.getTotalElements() : 0));

            return ResponseEntity.ok(ApiResponse.success(userPosts, "获取用户帖子成功"));
        } catch (EntityNotFoundException e) {
            System.err.println("用户不存在: " + e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("用户不存在"));
        } catch (Exception e) {
            System.err.println("获取用户帖子时发生错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(ApiResponse.error("获取用户帖子失败：" + e.getMessage()));
        }
    }

    // 获取用户发表的评论
    @GetMapping("/users/{userId}/comments")
    public ResponseEntity<ApiResponse<Page<CommentDTO>>> getUserComments(
            @PathVariable Long userId,
            Pageable pageable,
            Authentication authentication) {
        try {
            System.out.println("获取用户评论，用户ID: " + userId);

            Long currentUserId = getUserIdFromAuthentication(authentication);
            System.out.println("当前用户ID: " + currentUserId);

            Page<CommentDTO> userComments = forumService.getUserComments(userId, pageable, currentUserId);
            System.out.println("成功获取用户评论，总数: " + (userComments != null ? userComments.getTotalElements() : 0));

            return ResponseEntity.ok(ApiResponse.success(userComments, "获取用户评论成功"));
        } catch (EntityNotFoundException e) {
            System.err.println("用户不存在: " + e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("用户不存在"));
        } catch (Exception e) {
            System.err.println("获取用户评论时发生错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(ApiResponse.error("获取用户评论失败：" + e.getMessage()));
        }
    }

    // 获取社区统计信息
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCommunityStats() {
        try {
            System.out.println("获取社区统计信息");
            Map<String, Object> stats = forumService.getCommunityStats();
            System.out.println("成功获取社区统计信息: " + stats);
            return ResponseEntity.ok(ApiResponse.success(stats, "获取社区统计信息成功"));
        } catch (Exception e) {
            System.err.println("获取社区统计信息时发生错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(ApiResponse.error("获取社区统计信息失败：" + e.getMessage()));
        }
    }

    // 获取热门话题
    @GetMapping("/hot-topics")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getHotTopics() {
        try {
            System.out.println("获取热门话题");
            List<Map<String, Object>> hotTopics = forumService.getHotTopics();
            System.out.println("成功获取热门话题，数量: " + hotTopics.size());
            return ResponseEntity.ok(ApiResponse.success(hotTopics, "获取热门话题成功"));
        } catch (Exception e) {
            System.err.println("获取热门话题时发生错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(ApiResponse.error("获取热门话题失败：" + e.getMessage()));
        }
    }

    // 获取活跃用户
    @GetMapping("/active-users")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getActiveUsers() {
        try {
            System.out.println("获取活跃用户");
            List<Map<String, Object>> activeUsers = forumService.getActiveUsers();
            System.out.println("成功获取活跃用户，数量: " + activeUsers.size());
            return ResponseEntity.ok(ApiResponse.success(activeUsers, "获取活跃用户成功"));
        } catch (Exception e) {
            System.err.println("获取活跃用户时发生错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(ApiResponse.error("获取活跃用户失败：" + e.getMessage()));
        }
    }
}
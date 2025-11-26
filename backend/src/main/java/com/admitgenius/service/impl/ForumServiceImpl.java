package com.admitgenius.service.impl;

import com.admitgenius.dto.CommentDTO;
import com.admitgenius.dto.ForumPostDTO;
import com.admitgenius.model.*;
import com.admitgenius.repository.CommentRepository;
import com.admitgenius.repository.ForumRepository;
import com.admitgenius.repository.PostLikeRepository;
import com.admitgenius.repository.UserRepository;
import com.admitgenius.service.ForumService;
import com.admitgenius.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

@Service
public class ForumServiceImpl implements ForumService {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ForumServiceImpl.class);
    private static final List<String> SUPPORTED_SORT_PROPERTIES = Arrays.asList("createdAt", "title", "likes",
            "updatedAt");

    @Autowired
    private ForumRepository forumRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostLikeRepository postLikeRepository;

    @Autowired
    private UserService userService;

    @Override
    @Transactional(readOnly = true)
    public Page<ForumPostDTO> getAllPosts(Pageable pageable, String keyword, String category, String sortOption) {
        // 处理特殊的replies排序选项
        if ("replies".equals(sortOption)) {
            // 对于replies排序，需要先获取所有数据，全局排序后再分页
            List<ForumPost> allPosts;

            boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
            boolean hasCategory = category != null && !category.trim().isEmpty() && !"all".equalsIgnoreCase(category);

            if (hasKeyword && hasCategory) {
                allPosts = forumRepository
                        .findByCategoryIgnoreCaseAndTitleContainingIgnoreCaseOrCategoryIgnoreCaseAndContentContainingIgnoreCase(
                                category, keyword, category, keyword);
            } else if (hasCategory) {
                allPosts = forumRepository.findByCategoryIgnoreCase(category);
            } else if (hasKeyword) {
                allPosts = forumRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(keyword,
                        keyword);
            } else {
                allPosts = forumRepository.findAll();
            }

            // 转为DTO并按回复数降序排序
            List<ForumPostDTO> sortedPosts = allPosts.stream()
                    .map(post -> convertToDTO(post, null))
                    .sorted((a, b) -> Integer.compare(b.getCommentCount(), a.getCommentCount()))
                    .collect(Collectors.toList());

            // 手动分页
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), sortedPosts.size());
            List<ForumPostDTO> pageContent = start >= sortedPosts.size() ? new ArrayList<>()
                    : sortedPosts.subList(start, end);

            return new PageImpl<>(pageContent, pageable, sortedPosts.size());
        }

        // 对于其他排序方式，使用原有的数据库排序分页
        Page<ForumPost> postPage;

        Sort sort = pageable.getSort();
        boolean modifiedSort = false;

        if (sort.isSorted()) {
            for (Sort.Order order : sort) {
                if (!SUPPORTED_SORT_PROPERTIES.contains(order.getProperty())) {
                    logger.warn("Unsupported sort property '{}' requested. Falling back to default sort.",
                            order.getProperty());
                    modifiedSort = true;
                    break;
                }
            }
        }

        Pageable effectivePageable = pageable;
        if (modifiedSort || !sort.isSorted()) {
            effectivePageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "createdAt"));
        }

        // Determine which repository method to call based on keyword and category
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        boolean hasCategory = category != null && !category.trim().isEmpty() && !"all".equalsIgnoreCase(category);

        if (hasKeyword && hasCategory) {
            postPage = forumRepository
                    .findByCategoryIgnoreCaseAndTitleContainingIgnoreCaseOrCategoryIgnoreCaseAndContentContainingIgnoreCase(
                            category, keyword, category, keyword, effectivePageable);
        } else if (hasCategory) {
            postPage = forumRepository.findByCategoryIgnoreCase(category, effectivePageable);
        } else if (hasKeyword) {
            postPage = forumRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(keyword, keyword,
                    effectivePageable);
        } else {
            postPage = forumRepository.findAll(effectivePageable);
        }

        return postPage.map(post -> convertToDTO(post, null));
    }

    @Override
    @Transactional
    public ForumPostDTO createPost(ForumPostDTO postDTO, Long userId) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在，ID: " + userId));

        // 权限检查
        if (UserRole.SCHOOL_ASSISTANT.equals(author.getRole())) {
            throw new AccessDeniedException("择校助手无权创建帖子");
        }

        // 输入验证
        if (postDTO == null) {
            throw new IllegalArgumentException("帖子信息不能为空");
        }
        if (postDTO.getTitle() == null || postDTO.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("帖子标题不能为空");
        }
        if (postDTO.getContent() == null || postDTO.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("帖子内容不能为空");
        }

        ForumPost post = new ForumPost();
        post.setTitle(postDTO.getTitle().trim());
        post.setContent(postDTO.getContent().trim());
        if (postDTO.getCategory() != null && !postDTO.getCategory().trim().isEmpty()) {
            post.setCategory(postDTO.getCategory().trim());
        }
        // Tags handling - Log if tags are provided but not fully implemented on backend
        // entity yet
        if (postDTO.getTags() != null && !postDTO.getTags().isEmpty()) {
            logger.info("Received tags for new post, but backend entity does not yet store tags: {}",
                    postDTO.getTags());
        }
        post.setAuthor(author);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        post.setLikes(0);

        // 处理专家帖子相关字段
        if (postDTO.getIsExpertPost() != null && postDTO.getIsExpertPost()) {
            // 验证用户是否有权限发布专家帖子
            if (author.getRole() == UserRole.EXPERT || author.getRole() == UserRole.ADMIN) {
                post.setIsExpertPost(true);
                // 设置专家标签
                if (postDTO.getExpertTag() != null && !postDTO.getExpertTag().trim().isEmpty()) {
                    post.setExpertTag(postDTO.getExpertTag().trim());
                }
            } else {
                logger.warn("用户 {} 尝试发布专家帖子但权限不足", userId);
                post.setIsExpertPost(false);
            }
        } else {
            post.setIsExpertPost(false);
        }

        ForumPost savedPost = forumRepository.save(post);
        return convertToDTO(savedPost, userId);
    }

    @Override
    @Transactional // Ensures view increment is persisted
    public ForumPostDTO getPostById(Long postId, Long userId) {
        ForumPost post = forumRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("帖子不存在，ID: " + postId));

        post.setViews(post.getViews() + 1);
        forumRepository.save(post);

        return convertToDTOWithComments(post, userId);
    }

    @Override
    @Transactional
    public CommentDTO addComment(Long postId, CommentDTO commentDTO, Long userId) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在，ID: " + userId));
        ForumPost post = forumRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("帖子不存在，ID: " + postId));

        Comment comment = new Comment();
        comment.setContent(commentDTO.getContent());
        comment.setAuthor(author);
        comment.setPost(post);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setLikes(0);

        Comment savedComment = commentRepository.save(comment);

        post.setUpdatedAt(LocalDateTime.now());
        forumRepository.save(post);

        return convertToDTO(savedComment, userId);
    }

    @Override
    @Transactional
    public ForumPostDTO togglePostLike(Long postId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在，ID: " + userId));
        ForumPost post = forumRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("帖子不存在，ID: " + postId));

        Optional<PostLike> existingLike = postLikeRepository.findByUserAndPost(user, post);

        if (existingLike.isPresent()) {
            postLikeRepository.delete(existingLike.get());
        } else {
            PostLike newLike = new PostLike(user, post);
            postLikeRepository.save(newLike);
        }

        long currentLikes = postLikeRepository.countByPost(post);
        post.setLikes((int) currentLikes);
        post.setUpdatedAt(LocalDateTime.now());
        ForumPost savedPost = forumRepository.save(post);

        return convertToDTO(savedPost, userId);
    }

    @Override
    @Transactional
    public ForumPostDTO updatePost(Long postId, ForumPostDTO postDTO, Long userId) {
        ForumPost post = forumRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("帖子不存在，ID: " + postId));
        User requestingUser = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在，ID: " + userId));

        // 权限检查：择校助手不能修改帖子
        if (UserRole.SCHOOL_ASSISTANT.equals(requestingUser.getRole())) {
            throw new AccessDeniedException("择校助手无权修改帖子");
        }

        // 权限检查：只有作者自己或管理员可以修改帖子
        if (!post.getAuthor().getId().equals(requestingUser.getId()) &&
                !UserRole.ADMIN.equals(requestingUser.getRole())) {
            throw new AccessDeniedException("只有帖子作者或管理员可以修改此帖子");
        }

        // 输入验证
        if (postDTO.getTitle() == null || postDTO.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("帖子标题不能为空");
        }
        if (postDTO.getContent() == null || postDTO.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("帖子内容不能为空");
        }

        post.setTitle(postDTO.getTitle().trim());
        post.setContent(postDTO.getContent().trim());
        post.setUpdatedAt(LocalDateTime.now());

        ForumPost updatedPost = forumRepository.save(post);
        return convertToDTO(updatedPost, userId);
    }

    @Override
    @Transactional
    public void deletePost(Long postId, Long userId) {
        ForumPost post = forumRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("帖子不存在，ID: " + postId));
        User requestingUser = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在，ID: " + userId));

        if (UserRole.SCHOOL_ASSISTANT.equals(requestingUser.getRole())) {
            throw new AccessDeniedException("择校助手无权删除帖子");
        }
        if (!post.getAuthor().getId().equals(requestingUser.getId())
                && !UserRole.ADMIN.equals(requestingUser.getRole())) {
            throw new AccessDeniedException("用户无权删除此帖子");
        }

        commentRepository.deleteByPost(post);
        forumRepository.delete(post);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("评论不存在，ID: " + commentId));
        User requestingUser = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在，ID: " + userId));

        if (!comment.getAuthor().getId().equals(requestingUser.getId())
                && !UserRole.ADMIN.equals(requestingUser.getRole())) {
            throw new AccessDeniedException("用户无权删除此评论");
        }

        ForumPost post = comment.getPost();
        commentRepository.delete(comment);

        if (post != null) {
            post.setUpdatedAt(LocalDateTime.now());
            forumRepository.save(post);
        }
    }

    @Override
    public Long countTotalPosts() {
        return forumRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ForumPostDTO> getUserPosts(Long userId, Pageable pageable, Long currentUserId) {
        // 验证用户是否存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在，ID: " + userId));

        // 获取用户的帖子，按创建时间倒序排列
        Page<ForumPost> userPosts = forumRepository.findByAuthorOrderByCreatedAtDesc(user, pageable);

        return userPosts.map(post -> convertToDTO(post, currentUserId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentDTO> getUserComments(Long userId, Pageable pageable, Long currentUserId) {
        // 验证用户是否存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在，ID: " + userId));

        // 获取用户的评论，按创建时间倒序排列
        Page<Comment> userComments = commentRepository.findByAuthorOrderByCreatedAtDesc(user, pageable);

        return userComments.map(comment -> {
            CommentDTO dto = convertToDTO(comment, currentUserId);
            // 添加帖子标题信息
            if (comment.getPost() != null) {
                dto.setPostTitle(comment.getPost().getTitle());
            }
            return dto;
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getCommunityStats() {
        Map<String, Object> stats = new HashMap<>();

        // 社区成员总数
        long totalMembers = userRepository.count();
        stats.put("members", totalMembers);

        // 帖子总数
        long totalPosts = forumRepository.count();
        stats.put("posts", totalPosts);

        // 今日活跃用户数（今天发过帖子或评论的用户）
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        long activeUsersToday = forumRepository.countDistinctAuthorsByCreatedAtAfter(todayStart) +
                commentRepository.countDistinctAuthorsByCreatedAtAfter(todayStart);
        stats.put("activeUsers", activeUsersToday);

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getHotTopics() {
        List<Map<String, Object>> hotTopics = new ArrayList<>();

        // 获取各分类的帖子数量
        List<Object[]> categoryStats = forumRepository.findCategoryStats();

        for (Object[] stat : categoryStats) {
            String category = (String) stat[0];
            Long count = (Long) stat[1];

            Map<String, Object> topic = new HashMap<>();
            topic.put("id", category.hashCode()); // 生成一个简单的ID
            topic.put("name", getCategoryDisplayName(category));
            topic.put("count", count);

            hotTopics.add(topic);
        }

        // 限制返回前5个热门话题
        return hotTopics.stream().limit(5).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getActiveUsers() {
        List<Map<String, Object>> activeUsers = new ArrayList<>();

        // 获取发帖最多的用户（前5名）
        List<Object[]> userStats = forumRepository.findTopActiveUsers(PageRequest.of(0, 5));

        for (Object[] stat : userStats) {
            User user = (User) stat[0];
            Long postCount = (Long) stat[1];

            Map<String, Object> activeUser = new HashMap<>();
            activeUser.put("id", user.getId());
            activeUser.put("name", user.getFullName() != null ? user.getFullName() : user.getEmail());
            activeUser.put("avatar", user.getProfilePicture() != null ? user.getProfilePicture()
                    : "/default-avatar.png");
            activeUser.put("posts", postCount);

            activeUsers.add(activeUser);
        }

        return activeUsers;
    }

    // Helper method to get category display name
    private String getCategoryDisplayName(String category) {
        if (category == null)
            return "未分类";

        switch (category.toLowerCase()) {
            case "experience":
                return "申请经验";
            case "schools":
                return "院校讨论";
            case "exams":
                return "考试准备";
            case "visa":
                return "签证问题";
            case "life":
                return "生活交流";
            default:
                return category;
        }
    }

    // --- Helper Methods ---

    private ForumPostDTO convertToDTO(ForumPost post, Long currentUserId) {
        ForumPostDTO dto = new ForumPostDTO();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        if (post.getContent() != null) {
            dto.setExcerpt(
                    post.getContent().length() > 150 ? post.getContent().substring(0, 150) + "..." : post.getContent());
        }
        dto.setCategory(post.getCategory());
        dto.setViews(post.getViews());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setUpdatedAt(post.getUpdatedAt());
        dto.setLikes((int) postLikeRepository.countByPost(post));

        if (post.getAuthor() != null) {
            dto.setAuthorId(post.getAuthor().getId());
            String authorName = post.getAuthor().getFullName();
            if (authorName == null || authorName.trim().isEmpty()) {
                authorName = post.getAuthor().getEmail();
            }
            dto.setAuthorName(authorName);
            dto.setAuthorProfilePicture(post.getAuthor().getProfilePicture());

            // 设置完整的作者UserDTO对象，包含统计数据
            dto.setAuthor(userService.getUserById(post.getAuthor().getId()));
        }
        dto.setCommentCount((int) commentRepository.countByPost(post));

        if (currentUserId != null) {
            User currentUser = userRepository.findById(currentUserId).orElse(null);
            if (currentUser != null) {
                dto.setLikedByCurrentUser(postLikeRepository.findByUserAndPost(currentUser, post).isPresent());
            }
        }

        // Initialize tags to an empty list as ForumPost entity doesn't have tags yet
        dto.setTags(new ArrayList<>());

        // 设置专家帖子相关字段
        dto.setIsExpertPost(post.getIsExpertPost());
        dto.setExpertTag(post.getExpertTag());

        return dto;
    }

    private ForumPostDTO convertToDTOWithComments(ForumPost post, Long currentUserId) {
        ForumPostDTO dto = convertToDTO(post, currentUserId);
        List<CommentDTO> commentDTOs = commentRepository.findByPostOrderByCreatedAtAsc(post)
                .stream()
                .map(comment -> convertToDTO(comment, currentUserId))
                .collect(Collectors.toList());
        dto.setComments(commentDTOs);
        return dto;
    }

    private CommentDTO convertToDTO(Comment comment, Long currentUserId) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setLikes(comment.getLikes());
        if (comment.getPost() != null) {
            dto.setPostId(comment.getPost().getId());
        }
        if (comment.getAuthor() != null) {
            String authorName = comment.getAuthor().getFullName();
            if (authorName == null || authorName.trim().isEmpty()) {
                authorName = comment.getAuthor().getEmail();
            }
            dto.setAuthorId(comment.getAuthor().getId());
            dto.setAuthorName(authorName);
            dto.setAuthorRole(comment.getAuthor().getRole() != null ? comment.getAuthor().getRole().toString() : null);
            dto.setAuthorProfilePicture(comment.getAuthor().getProfilePicture());
        }
        return dto;
    }
}
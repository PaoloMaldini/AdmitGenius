package com.admitgenius.service;

import com.admitgenius.dto.CommentDTO;
import com.admitgenius.dto.ForumPostDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface ForumService {

    /**
     * 获取帖子列表（支持分页、关键字搜索和分类过滤）
     * 
     * @param pageable   分页和排序信息
     * @param keyword    搜索关键字（标题或内容）
     * @param category   帖子分类
     * @param sortOption 排序选项 (latest, popular, replies)
     * @return 帖子DTO的分页结果
     */
    Page<ForumPostDTO> getAllPosts(Pageable pageable, String keyword, String category, String sortOption);

    /**
     * 获取帖子列表（支持分页、关键字搜索和分类过滤） - 向后兼容版本
     * 
     * @param pageable 分页和排序信息
     * @param keyword  搜索关键字（标题或内容）
     * @param category 帖子分类
     * @return 帖子DTO的分页结果
     */
    default Page<ForumPostDTO> getAllPosts(Pageable pageable, String keyword, String category) {
        return getAllPosts(pageable, keyword, category, null);
    }

    /**
     * 创建新帖子
     * 
     * @param postDTO 包含帖子标题和内容的DTO
     * @param userId  发帖用户ID
     * @return 创建后的帖子DTO
     */
    ForumPostDTO createPost(ForumPostDTO postDTO, Long userId);

    /**
     * 根据ID获取帖子详情
     * 
     * @param postId 帖子ID
     * @param userId 当前查看用户的ID (用于判断是否点赞等)
     * @return 帖子DTO
     */
    ForumPostDTO getPostById(Long postId, Long userId);

    /**
     * 为帖子添加评论
     * 
     * @param postId     帖子ID
     * @param commentDTO 包含评论内容的DTO
     * @param userId     评论用户ID
     * @return 创建后的评论DTO
     */
    CommentDTO addComment(Long postId, CommentDTO commentDTO, Long userId);

    /**
     * 点赞或取消点赞帖子
     * 
     * @param postId 帖子ID
     * @param userId 点赞/取消点赞的用户ID
     * @return 更新点赞状态和数量后的帖子DTO
     */
    ForumPostDTO togglePostLike(Long postId, Long userId);

    /**
     * 更新帖子
     * 
     * @param postId  帖子ID
     * @param postDTO 包含更新后帖子标题和内容的DTO
     * @param userId  请求更新的用户ID（用于权限校验）
     * @return 更新后的帖子DTO
     */
    ForumPostDTO updatePost(Long postId, ForumPostDTO postDTO, Long userId);

    /**
     * 删除帖子
     * 
     * @param postId 帖子ID
     * @param userId 请求删除的用户ID（用于权限校验）
     */
    void deletePost(Long postId, Long userId);

    /**
     * 删除评论
     * 
     * @param commentId 评论ID
     * @param userId    请求删除的用户ID（用于权限校验，或判断是否为管理员）
     */
    void deleteComment(Long commentId, Long userId);

    /**
     * 统计帖子总数
     * 
     * @return 帖子总数
     */
    Long countTotalPosts();

    /**
     * 获取用户发布的帖子
     * 
     * @param userId        用户ID
     * @param pageable      分页信息
     * @param currentUserId 当前查看用户的ID (用于判断是否点赞等)
     * @return 用户帖子的分页结果
     */
    Page<ForumPostDTO> getUserPosts(Long userId, Pageable pageable, Long currentUserId);

    /**
     * 获取用户发表的评论
     * 
     * @param userId        用户ID
     * @param pageable      分页信息
     * @param currentUserId 当前查看用户的ID
     * @return 用户评论的分页结果
     */
    Page<CommentDTO> getUserComments(Long userId, Pageable pageable, Long currentUserId);

    /**
     * 获取社区统计信息
     * 
     * @return 包含社区成员数、帖子总数、今日活跃用户数的Map
     */
    Map<String, Object> getCommunityStats();

    /**
     * 获取热门话题
     * 
     * @return 热门话题列表，包含话题名称和帖子数量
     */
    List<Map<String, Object>> getHotTopics();

    /**
     * 获取活跃用户列表
     * 
     * @return 活跃用户列表，包含用户信息和发帖数量
     */
    List<Map<String, Object>> getActiveUsers();
}
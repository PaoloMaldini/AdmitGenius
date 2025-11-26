package com.admitgenius.repository;

import com.admitgenius.model.ForumPost;
import com.admitgenius.model.PostLike;
import com.admitgenius.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    Optional<PostLike> findByUserAndPost(User user, ForumPost post);

    long countByPost(ForumPost post);

    // 计算用户获赞总数（用户发布的帖子被点赞的总数）
    @Query("SELECT COUNT(pl) FROM PostLike pl WHERE pl.post.author.id = :userId")
    long countLikesByUserId(@Param("userId") Long userId);

    // 如果需要，还可以添加 existsByUserAndPost(User user, ForumPost post) 方法
}
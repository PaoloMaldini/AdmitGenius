package com.admitgenius.repository;

import com.admitgenius.model.Comment;
import com.admitgenius.model.ForumPost;
import com.admitgenius.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPost(ForumPost post);

    List<Comment> findByPostOrderByCreatedAtAsc(ForumPost post);

    Page<Comment> findByAuthorOrderByCreatedAtDesc(User author, Pageable pageable);

    long countByPost(ForumPost post);

    void deleteByPost(ForumPost post);

    @Query("SELECT COUNT(DISTINCT c.author) FROM Comment c WHERE c.createdAt >= :startDate")
    long countDistinctAuthorsByCreatedAtAfter(@Param("startDate") LocalDateTime startDate);

    // 计算用户评论数
    long countByAuthor(User author);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.author.id = :authorId")
    long countByAuthorId(@Param("authorId") Long authorId);
}
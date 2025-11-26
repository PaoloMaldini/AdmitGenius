package com.admitgenius.repository;

import com.admitgenius.model.ForumPost;
import com.admitgenius.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ForumPostRepository extends JpaRepository<ForumPost, Long> {
    Page<ForumPost> findByTitleContaining(String keyword, Pageable pageable);

    Page<ForumPost> findByCategory(String category, Pageable pageable);

    Page<ForumPost> findByTitleContainingAndCategory(String keyword, String category, Pageable pageable);

    long countByCategory(String category);

    @Query("SELECT COUNT(DISTINCT p.author) FROM ForumPost p")
    long countDistinctAuthors();

    @Query("SELECT COUNT(DISTINCT p.author) FROM ForumPost p WHERE p.createdAt >= :startDate")
    long countDistinctAuthorsByCreatedAtAfter(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT p.category, COUNT(p) FROM ForumPost p WHERE p.category IS NOT NULL GROUP BY p.category ORDER BY COUNT(p) DESC")
    List<Object[]> findCategoryStats();

    @Query("SELECT p.author, COUNT(p) FROM ForumPost p GROUP BY p.author ORDER BY COUNT(p) DESC")
    List<Object[]> findTopActiveUsers(Pageable pageable);

    // 计算用户发帖数
    long countByAuthor(User author);

    @Query("SELECT COUNT(p) FROM ForumPost p WHERE p.author.id = :authorId")
    long countByAuthorId(@Param("authorId") Long authorId);

    // 计算用户帖子总浏览量
    @Query("SELECT COALESCE(SUM(p.views), 0) FROM ForumPost p WHERE p.author.id = :authorId")
    Long sumViewsByAuthorId(@Param("authorId") Long authorId);
}
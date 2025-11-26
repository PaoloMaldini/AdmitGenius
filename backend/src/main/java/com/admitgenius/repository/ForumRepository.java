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
public interface ForumRepository extends JpaRepository<ForumPost, Long> {
        Page<ForumPost> findByAuthor(User author, Pageable pageable);

        Page<ForumPost> findByAuthorOrderByCreatedAtDesc(User author, Pageable pageable);

        Page<ForumPost> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(String titleKeyword,
                        String contentKeyword, Pageable pageable);

        Page<ForumPost> findByCategory(String category, Pageable pageable);

        Page<ForumPost> findByCategoryIgnoreCase(String category, Pageable pageable);

        Page<ForumPost> findByCategoryIgnoreCaseAndTitleContainingIgnoreCaseOrCategoryIgnoreCaseAndContentContainingIgnoreCase(
                        String category1, String title, String category2, String content, Pageable pageable);

        @Query("SELECT p FROM ForumPost p WHERE " +
                        "(LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
                        "p.category = :category")
        Page<ForumPost> findByKeywordAndCategory(
                        @Param("keyword") String keyword,
                        @Param("category") String category,
                        Pageable pageable);

        @Query("SELECT COUNT(DISTINCT p.author) FROM ForumPost p WHERE p.createdAt >= :startDate")
        long countDistinctAuthorsByCreatedAtAfter(@Param("startDate") LocalDateTime startDate);

        @Query("SELECT p.category, COUNT(p) FROM ForumPost p WHERE p.category IS NOT NULL GROUP BY p.category ORDER BY COUNT(p) DESC")
        List<Object[]> findCategoryStats();

        @Query("SELECT p.author, COUNT(p) FROM ForumPost p GROUP BY p.author ORDER BY COUNT(p) DESC")
        List<Object[]> findTopActiveUsers(Pageable pageable);

        // 不分页的查询方法，用于全局排序
        List<ForumPost> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(String titleKeyword,
                        String contentKeyword);

        List<ForumPost> findByCategoryIgnoreCase(String category);

        List<ForumPost> findByCategoryIgnoreCaseAndTitleContainingIgnoreCaseOrCategoryIgnoreCaseAndContentContainingIgnoreCase(
                        String category1, String title, String category2, String content);
}
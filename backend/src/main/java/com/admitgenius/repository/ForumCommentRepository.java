package com.admitgenius.repository;

import com.admitgenius.model.ForumComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ForumCommentRepository extends JpaRepository<ForumComment, Long> {
    @Query("SELECT COUNT(DISTINCT c.author) FROM ForumComment c WHERE c.createdAt >= :startDate")
    long countDistinctAuthorsByCreatedAtAfter(@Param("startDate") LocalDateTime startDate);
}
package com.admitgenius.repository;

import com.admitgenius.model.Recommendation;
import com.admitgenius.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {
    List<Recommendation> findByStudentOrderByCreatedAtDesc(User student);
}
package com.admitgenius.repository;

import com.admitgenius.model.Education;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EducationRepository extends JpaRepository<Education, Long> {

    /**
     * 根据用户ID查找所有教育经历，按结束时间倒序排列
     */
    List<Education> findByUserIdOrderByEndDateDesc(Long userId);

    /**
     * 根据用户ID和教育经历ID查找教育经历
     */
    Education findByIdAndUserId(Long id, Long userId);

    /**
     * 删除指定用户的指定教育经历
     */
    void deleteByIdAndUserId(Long id, Long userId);
}
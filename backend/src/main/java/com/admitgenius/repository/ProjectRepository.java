package com.admitgenius.repository;

import com.admitgenius.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    /**
     * 根据用户ID查找所有项目经历，按结束时间倒序排列
     */
    List<Project> findByUserIdOrderByEndDateDesc(Long userId);

    /**
     * 根据用户ID和项目ID查找项目经历
     */
    Project findByIdAndUserId(Long id, Long userId);

    /**
     * 删除指定用户的指定项目经历
     */
    void deleteByIdAndUserId(Long id, Long userId);
}
package com.admitgenius.service;

import com.admitgenius.dto.ProjectDTO;
import com.admitgenius.model.Project;
import com.admitgenius.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;

    /**
     * 获取用户的所有项目经历
     */
    public List<ProjectDTO> getUserProjects(Long userId) {
        log.info("获取用户项目经历，用户ID: {}", userId);
        List<Project> projects = projectRepository.findByUserIdOrderByEndDateDesc(userId);
        return projects.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 创建项目经历
     */
    public ProjectDTO createProject(Long userId, ProjectDTO projectDTO) {
        log.info("创建项目经历，用户ID: {}, 项目名称: {}", userId, projectDTO.getName());

        Project project = convertToEntity(projectDTO);
        project.setUserId(userId);

        Project savedProject = projectRepository.save(project);
        log.info("项目经历创建成功，ID: {}", savedProject.getId());

        return convertToDTO(savedProject);
    }

    /**
     * 更新项目经历
     */
    public ProjectDTO updateProject(Long userId, Long projectId, ProjectDTO projectDTO) {
        log.info("更新项目经历，用户ID: {}, 项目ID: {}", userId, projectId);

        Project existingProject = projectRepository.findByIdAndUserId(projectId, userId);
        if (existingProject == null) {
            throw new RuntimeException("项目经历不存在或无权限访问");
        }

        // 更新字段
        existingProject.setName(projectDTO.getName());
        existingProject.setStartDate(projectDTO.getStartDate());
        existingProject.setEndDate(projectDTO.getEndDate());
        existingProject.setContent(projectDTO.getContent());
        existingProject.setIsLeader(projectDTO.getIsLeader());

        Project updatedProject = projectRepository.save(existingProject);
        log.info("项目经历更新成功，ID: {}", updatedProject.getId());

        return convertToDTO(updatedProject);
    }

    /**
     * 删除项目经历
     */
    public void deleteProject(Long userId, Long projectId) {
        log.info("删除项目经历，用户ID: {}, 项目ID: {}", userId, projectId);

        Project project = projectRepository.findByIdAndUserId(projectId, userId);
        if (project == null) {
            throw new RuntimeException("项目经历不存在或无权限访问");
        }

        projectRepository.deleteByIdAndUserId(projectId, userId);
        log.info("项目经历删除成功，ID: {}", projectId);
    }

    /**
     * 将实体转换为DTO
     */
    private ProjectDTO convertToDTO(Project project) {
        ProjectDTO dto = new ProjectDTO();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setStartDate(project.getStartDate());
        dto.setEndDate(project.getEndDate());
        dto.setContent(project.getContent());
        dto.setIsLeader(project.getIsLeader());
        return dto;
    }

    /**
     * 将DTO转换为实体
     */
    private Project convertToEntity(ProjectDTO dto) {
        Project project = new Project();
        project.setName(dto.getName());
        project.setStartDate(dto.getStartDate());
        project.setEndDate(dto.getEndDate());
        project.setContent(dto.getContent());
        project.setIsLeader(dto.getIsLeader());
        return project;
    }
}
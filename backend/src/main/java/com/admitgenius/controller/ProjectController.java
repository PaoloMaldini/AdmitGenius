package com.admitgenius.controller;

import com.admitgenius.dto.ProjectDTO;
import com.admitgenius.dto.ApiResponse;
import com.admitgenius.dto.UserDTO;
import com.admitgenius.service.ProjectService;
import com.admitgenius.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final UserService userService;

    // 辅助方法，从 Authentication 对象获取用户ID
    private Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            try {
                UserDTO user = userService.findByEmail(username);
                return user.getId();
            } catch (Exception e) {
                log.error("无法根据认证信息找到用户: {}, 错误: {}", username, e.getMessage());
                return null;
            }
        } else if (principal instanceof String) {
            if ("anonymousUser".equals(principal)) {
                return null;
            }
            try {
                UserDTO user = userService.findByEmail((String) principal);
                return user.getId();
            } catch (Exception e) {
                log.error("无法根据认证信息找到用户: {}, 错误: {}", principal, e.getMessage());
                return null;
            }
        }
        return null;
    }

    /**
     * 获取当前用户的所有项目经历
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProjectDTO>>> getUserProjects(Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            if (userId == null) {
                return ResponseEntity.ok(ApiResponse.error("用户未登录"));
            }

            List<ProjectDTO> projects = projectService.getUserProjects(userId);
            return ResponseEntity.ok(ApiResponse.success(projects, "获取项目经历成功"));
        } catch (Exception e) {
            log.error("获取项目经历失败", e);
            return ResponseEntity.ok(ApiResponse.error("获取项目经历失败: " + e.getMessage()));
        }
    }

    /**
     * 创建项目经历
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ProjectDTO>> createProject(
            @Valid @RequestBody ProjectDTO projectDTO,
            Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            if (userId == null) {
                return ResponseEntity.ok(ApiResponse.error("用户未登录"));
            }

            ProjectDTO createdProject = projectService.createProject(userId, projectDTO);
            return ResponseEntity.ok(ApiResponse.success(createdProject, "创建项目经历成功"));
        } catch (Exception e) {
            log.error("创建项目经历失败", e);
            return ResponseEntity.ok(ApiResponse.error("创建项目经历失败: " + e.getMessage()));
        }
    }

    /**
     * 更新项目经历
     */
    @PutMapping("/{projectId}")
    public ResponseEntity<ApiResponse<ProjectDTO>> updateProject(
            @PathVariable Long projectId,
            @Valid @RequestBody ProjectDTO projectDTO,
            Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            if (userId == null) {
                return ResponseEntity.ok(ApiResponse.error("用户未登录"));
            }

            ProjectDTO updatedProject = projectService.updateProject(userId, projectId, projectDTO);
            return ResponseEntity.ok(ApiResponse.success(updatedProject, "更新项目经历成功"));
        } catch (Exception e) {
            log.error("更新项目经历失败", e);
            return ResponseEntity.ok(ApiResponse.error("更新项目经历失败: " + e.getMessage()));
        }
    }

    /**
     * 删除项目经历
     */
    @DeleteMapping("/{projectId}")
    public ResponseEntity<ApiResponse<Void>> deleteProject(
            @PathVariable Long projectId,
            Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            if (userId == null) {
                return ResponseEntity.ok(ApiResponse.error("用户未登录"));
            }

            projectService.deleteProject(userId, projectId);
            return ResponseEntity.ok(ApiResponse.<Void>success(null, "删除项目经历成功"));
        } catch (Exception e) {
            log.error("删除项目经历失败", e);
            return ResponseEntity.ok(ApiResponse.<Void>error("删除项目经历失败: " + e.getMessage()));
        }
    }
}
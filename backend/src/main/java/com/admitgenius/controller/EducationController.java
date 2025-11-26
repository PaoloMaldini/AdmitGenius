package com.admitgenius.controller;

import com.admitgenius.dto.EducationDTO;
import com.admitgenius.dto.ApiResponse;
import com.admitgenius.dto.UserDTO;
import com.admitgenius.service.EducationService;
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
@RequestMapping("/api/educations")
@RequiredArgsConstructor
public class EducationController {

    private final EducationService educationService;
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
     * 获取当前用户的所有教育经历
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<EducationDTO>>> getUserEducations(Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            if (userId == null) {
                return ResponseEntity.ok(ApiResponse.error("用户未登录"));
            }

            List<EducationDTO> educations = educationService.getUserEducations(userId);
            return ResponseEntity.ok(ApiResponse.success(educations, "获取教育经历成功"));
        } catch (Exception e) {
            log.error("获取教育经历失败", e);
            return ResponseEntity.ok(ApiResponse.error("获取教育经历失败: " + e.getMessage()));
        }
    }

    /**
     * 创建教育经历
     */
    @PostMapping
    public ResponseEntity<ApiResponse<EducationDTO>> createEducation(
            @Valid @RequestBody EducationDTO educationDTO,
            Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            if (userId == null) {
                return ResponseEntity.ok(ApiResponse.error("用户未登录"));
            }

            EducationDTO createdEducation = educationService.createEducation(userId, educationDTO);
            return ResponseEntity.ok(ApiResponse.success(createdEducation, "创建教育经历成功"));
        } catch (Exception e) {
            log.error("创建教育经历失败", e);
            return ResponseEntity.ok(ApiResponse.error("创建教育经历失败: " + e.getMessage()));
        }
    }

    /**
     * 更新教育经历
     */
    @PutMapping("/{educationId}")
    public ResponseEntity<ApiResponse<EducationDTO>> updateEducation(
            @PathVariable Long educationId,
            @Valid @RequestBody EducationDTO educationDTO,
            Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            if (userId == null) {
                return ResponseEntity.ok(ApiResponse.error("用户未登录"));
            }

            EducationDTO updatedEducation = educationService.updateEducation(userId, educationId, educationDTO);
            return ResponseEntity.ok(ApiResponse.success(updatedEducation, "更新教育经历成功"));
        } catch (Exception e) {
            log.error("更新教育经历失败", e);
            return ResponseEntity.ok(ApiResponse.error("更新教育经历失败: " + e.getMessage()));
        }
    }

    /**
     * 删除教育经历
     */
    @DeleteMapping("/{educationId}")
    public ResponseEntity<ApiResponse<Void>> deleteEducation(
            @PathVariable Long educationId,
            Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            if (userId == null) {
                return ResponseEntity.ok(ApiResponse.error("用户未登录"));
            }

            educationService.deleteEducation(userId, educationId);
            return ResponseEntity.ok(ApiResponse.<Void>success(null, "删除教育经历成功"));
        } catch (Exception e) {
            log.error("删除教育经历失败", e);
            return ResponseEntity.ok(ApiResponse.<Void>error("删除教育经历失败: " + e.getMessage()));
        }
    }
}
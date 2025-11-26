package com.admitgenius.controller;

import com.admitgenius.dto.ApiResponse;
import com.admitgenius.dto.UserDTO;
import com.admitgenius.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

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
                System.err.println("警告: 无法根据认证信息找到用户: " + username + ", 错误: " + e.getMessage());
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
                System.err.println("警告: 无法根据认证信息找到用户: " + principal + ", 错误: " + e.getMessage());
                return null;
            }
        }
        return null;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(userService.register(userDTO));
    }

    @PostMapping("/login")
    public ResponseEntity<UserDTO> login(@RequestBody UserDTO loginRequest) {
        return ResponseEntity.ok(userService.login(loginRequest.getEmail(), loginRequest.getPassword()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {
        try {
            UserDTO user = userService.getUserById(id);
            return ResponseEntity.ok(ApiResponse.success(user, "获取用户信息成功"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.ok(ApiResponse.error("用户不存在"));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("获取用户信息失败：" + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO,
            Authentication authentication) {
        try {
            // 验证权限：只有用户本人或管理员可以修改用户信息
            Long currentUserId = getUserIdFromAuthentication(authentication);
            if (currentUserId == null) {
                return ResponseEntity.ok(ApiResponse.error("用户未登录"));
            }

            // 检查是否是用户本人
            if (!currentUserId.equals(id)) {
                // 如果不是本人，检查是否是管理员
                UserDTO currentUser = userService.getUserById(currentUserId);
                if (!"ADMIN".equals(currentUser.getRole())) {
                    return ResponseEntity.ok(ApiResponse.error("权限不足：只能修改自己的信息"));
                }
            }

            UserDTO updatedUser = userService.updateUser(id, userDTO);
            return ResponseEntity.ok(ApiResponse.success(updatedUser, "用户信息更新成功"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.ok(ApiResponse.error("用户不存在"));
        } catch (AccessDeniedException e) {
            return ResponseEntity.ok(ApiResponse.error("权限不足：" + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("更新用户信息失败：" + e.getMessage()));
        }
    }

    // 获取当前用户的档案信息
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUserProfile(Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            if (userId == null) {
                return ResponseEntity.ok(ApiResponse.error("用户未登录"));
            }

            UserDTO user = userService.getUserById(userId);
            return ResponseEntity.ok(ApiResponse.success(user, "获取用户档案成功"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.ok(ApiResponse.error("用户不存在"));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("获取用户档案失败：" + e.getMessage()));
        }
    }

    // 更新当前用户的档案信息
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserDTO>> updateCurrentUserProfile(@RequestBody UserDTO userDTO,
            Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            if (userId == null) {
                return ResponseEntity.ok(ApiResponse.error("用户未登录"));
            }

            UserDTO updatedUser = userService.updateUser(userId, userDTO);
            return ResponseEntity.ok(ApiResponse.success(updatedUser, "用户档案更新成功"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.ok(ApiResponse.error("用户不存在"));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("更新用户档案失败：" + e.getMessage()));
        }
    }

    // 修改密码
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(@RequestBody Map<String, String> passwordData,
            Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            if (userId == null) {
                return ResponseEntity.ok(ApiResponse.error("用户未登录"));
            }

            String currentPassword = passwordData.get("currentPassword");
            String newPassword = passwordData.get("newPassword");

            if (currentPassword == null || newPassword == null) {
                return ResponseEntity.ok(ApiResponse.error("密码信息不完整"));
            }

            boolean success = userService.changePassword(userId, currentPassword, newPassword);
            if (success) {
                return ResponseEntity.ok(ApiResponse.success("密码修改成功", "密码修改成功"));
            } else {
                return ResponseEntity.ok(ApiResponse.error("当前密码不正确"));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("修改密码失败：" + e.getMessage()));
        }
    }

    // 上传头像
    @PostMapping("/upload-avatar")
    public ResponseEntity<ApiResponse<String>> uploadAvatar(@RequestParam("file") MultipartFile file,
            Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            if (userId == null) {
                return ResponseEntity.ok(ApiResponse.error("用户未登录"));
            }

            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
                return ResponseEntity.ok(ApiResponse.error("只支持JPG和PNG格式的图片"));
            }

            // 验证文件大小（2MB限制）
            if (file.getSize() > 2 * 1024 * 1024) {
                return ResponseEntity.ok(ApiResponse.error("图片大小不能超过2MB"));
            }

            String avatarUrl = userService.uploadAvatar(userId, file);
            return ResponseEntity.ok(ApiResponse.success(avatarUrl, "头像上传成功"));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("头像上传失败：" + e.getMessage()));
        }
    }

    // 获取用户保存的学校
    @GetMapping("/{userId}/saved-schools")
    public ResponseEntity<ApiResponse<List<Long>>> getUserSavedSchools(@PathVariable Long userId,
            Authentication authentication) {
        try {
            // 验证权限：只有用户本人可以查看自己保存的学校
            Long currentUserId = getUserIdFromAuthentication(authentication);
            if (currentUserId == null) {
                return ResponseEntity.ok(ApiResponse.error("用户未登录"));
            }

            if (!currentUserId.equals(userId)) {
                return ResponseEntity.ok(ApiResponse.error("权限不足：只能查看自己保存的学校"));
            }

            List<Long> savedSchools = userService.getSavedSchools(userId);
            return ResponseEntity.ok(ApiResponse.success(savedSchools, "获取保存的学校成功"));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("获取保存的学校失败：" + e.getMessage()));
        }
    }

    // 切换学校保存状态
    @PostMapping("/{userId}/saved-schools/{schoolId}")
    public ResponseEntity<ApiResponse<String>> toggleSavedSchool(
            @PathVariable Long userId,
            @PathVariable Long schoolId,
            @RequestBody Map<String, Boolean> requestBody,
            Authentication authentication) {
        try {
            // 验证权限：只有用户本人可以修改自己保存的学校
            Long currentUserId = getUserIdFromAuthentication(authentication);
            if (currentUserId == null) {
                return ResponseEntity.ok(ApiResponse.error("用户未登录"));
            }

            if (!currentUserId.equals(userId)) {
                return ResponseEntity.ok(ApiResponse.error("权限不足：只能修改自己保存的学校"));
            }

            Boolean saved = requestBody.get("saved");
            if (saved == null) {
                return ResponseEntity.ok(ApiResponse.error("请求参数不完整"));
            }

            userService.toggleSavedSchool(userId, schoolId, saved);
            String message = saved ? "学校已添加到收藏" : "学校已从收藏移除";
            return ResponseEntity.ok(ApiResponse.success("操作成功", message));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("操作失败：" + e.getMessage()));
        }
    }
}
package com.admitgenius.service;

import com.admitgenius.dto.UserDTO;
import com.admitgenius.model.User;
import com.admitgenius.model.UserRole;
import com.admitgenius.model.UserStatus;
import com.admitgenius.repository.UserRepository;
import com.admitgenius.repository.ForumPostRepository;
import com.admitgenius.repository.CommentRepository;
import com.admitgenius.repository.PostLikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ForumPostRepository forumPostRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostLikeRepository postLikeRepository;

    public UserDTO register(UserDTO userDTO) {
        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new RuntimeException("邮箱已被注册");
        }

        User user = convertToEntity(userDTO);
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }

    public UserDTO login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 检查用户状态
        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new RuntimeException("账户已被禁用"); // 或者更具体的 DisabledException/LockedException
        }
        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new RuntimeException("账户未激活或已停用");
        }
        // PENDING_VERIFICATION 也可以根据业务逻辑决定是否允许登录
        if (user.getStatus() == UserStatus.PENDING_VERIFICATION) {
            // throw new RuntimeException("账户待验证");
        }

        return convertToDTO(user);
    }

    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在 ID: " + id));
        return convertToDTO(user);
    }

    public UserDTO updateUser(Long id, UserDTO userDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在 ID: " + id));

        // 更新用户基本信息
        if (userDTO.getFullName() != null) {
            user.setFullName(userDTO.getFullName());
        }
        if (userDTO.getProfilePicture() != null) {
            user.setProfilePicture(userDTO.getProfilePicture());
        }
        if (userDTO.getPhone() != null) {
            user.setPhone(userDTO.getPhone());
        }
        if (userDTO.getBio() != null) {
            user.setBio(userDTO.getBio());
        }

        // 更新学术信息
        if (userDTO.getUndergraduateSchool() != null) {
            user.setCurrentSchool(userDTO.getUndergraduateSchool());
        }
        if (userDTO.getGpa() != null) {
            user.setGpa(userDTO.getGpa());
        }
        if (userDTO.getTargetMajor() != null) {
            user.setTargetMajor(userDTO.getTargetMajor());
        }

        // 更新考试成绩
        if (userDTO.getGreScore() != null) {
            user.setGreCombined(userDTO.getGreScore());
        }
        if (userDTO.getGmatScore() != null) {
            user.setGmatTotal(userDTO.getGmatScore());
        }
        if (userDTO.getToeflScore() != null) {
            user.setToeflScore(userDTO.getToeflScore());
        }
        if (userDTO.getIeltsScore() != null) {
            user.setIeltsScore(userDTO.getIeltsScore());
        }

        // The updatedAt field should be updated automatically via @PreUpdate in User
        // entity
        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public UserDTO findByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("用户不存在，邮箱: " + email));
        return convertToDTO(user);
    }

    /**
     * 删除用户
     * 管理员功能，用于删除指定ID的用户
     * 
     * @param id 用户ID
     * @throws RuntimeException 如果用户不存在
     */
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在 ID: " + id));

        // 执行删除操作
        userRepository.delete(user);
    }

    public UserDTO updateUserRole(Long userId, UserRole newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在，ID: " + userId));

        if (newRole == UserRole.ADMIN || user.getRole() == UserRole.ADMIN && newRole != UserRole.ADMIN) {
            throw new AccessDeniedException("不允许通过此接口直接操作ADMIN角色。");
        }

        user.setRole(newRole);
        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    public UserDTO suspendUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在，ID: " + userId));

        // 防止禁用ADMIN账户，除非有更高级别的逻辑允许
        if (user.getRole() == UserRole.ADMIN) {
            throw new AccessDeniedException("不允许通过此接口禁用ADMIN账户。");
        }

        user.setStatus(UserStatus.SUSPENDED);
        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    public UserDTO unsuspendUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在，ID: " + userId));

        // 一般来说，恢复账户时不需要对角色做特殊限制
        user.setStatus(UserStatus.ACTIVE);
        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    public void adminResetUserPassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在，ID: " + userId));

        // 通常不允许管理员直接重置其他ADMIN账户的密码，除非有特殊授权
        // 如果需要此功能，可以添加更复杂的权限检查或移除此限制
        if (user.getRole() == UserRole.ADMIN) {
            // 可以考虑允许重置自己的密码，或者需要一个特殊的权限来重置其他管理员密码
            // 此处简化为不允许通过此接口重置任何ADMIN密码
            throw new AccessDeniedException("不允许通过此接口重置ADMIN账户的密码。");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user); // 只需要保存，不需要返回DTO，因为密码不应该在DTO中泄露
    }

    public UserDTO createAdminUser(com.admitgenius.dto.CreateAdminRequestDTO adminRequestDTO) {
        // 校验管理员密钥
        if (!"20031227".equals(adminRequestDTO.getAdminSecret())) {
            throw new org.springframework.security.access.AccessDeniedException("无效的管理员密钥");
        }

        if (userRepository.existsByEmail(adminRequestDTO.getEmail())) {
            throw new RuntimeException("邮箱已被注册: " + adminRequestDTO.getEmail());
        }
        User adminUser = new User();
        adminUser.setEmail(adminRequestDTO.getEmail());
        adminUser.setPassword(passwordEncoder.encode(adminRequestDTO.getPassword())); // 新管理员的密码
        adminUser.setRole(UserRole.ADMIN);
        adminUser.setFullName("Administrator (Secret Provisioned)");
        // status 默认为 ACTIVE， createdAt 和 updatedAt 会自动处理

        User savedAdmin = userRepository.save(adminUser);
        return convertToDTO(savedAdmin);
    }

    /**
     * 修改用户密码
     * 
     * @param userId          用户ID
     * @param currentPassword 当前密码
     * @param newPassword     新密码
     * @return 是否修改成功
     */
    public boolean changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在 ID: " + userId));

        // 验证当前密码
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return false;
        }

        // 设置新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return true;
    }

    /**
     * 上传用户头像
     * 
     * @param userId 用户ID
     * @param file   头像文件
     * @return 头像URL
     */
    public String uploadAvatar(Long userId, MultipartFile file) throws IOException {
        System.out.println("开始上传头像 - 用户ID: " + userId + ", 文件名: " + file.getOriginalFilename());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在 ID: " + userId));

        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = userId + "_" + System.currentTimeMillis() + fileExtension;
        System.out.println("生成的文件名: " + fileName);

        // 使用项目目录下的data/uploads文件夹
        // 获取项目根目录（backend的父目录）
        String projectRoot = System.getProperty("user.dir");
        if (projectRoot.endsWith("backend")) {
            projectRoot = projectRoot.substring(0, projectRoot.length() - 8); // 移除 "/backend"
        }
        String uploadDir = projectRoot + "/data/uploads/avatars/";
        System.out.println("上传目录: " + uploadDir);

        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            System.out.println("创建上传目录: " + uploadPath);
            Files.createDirectories(uploadPath);
        }

        // 保存文件
        Path filePath = uploadPath.resolve(fileName);
        System.out.println("完整文件路径: " + filePath);

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("文件保存成功");

        // 构建头像URL - 这里需要配合文件访问控制器
        String avatarUrl = "/api/files/avatars/" + fileName;
        System.out.println("生成的头像URL: " + avatarUrl);

        // 删除旧头像文件（如果存在）
        if (user.getProfilePicture() != null && !user.getProfilePicture().isEmpty()) {
            String oldFileName = user.getProfilePicture().substring(user.getProfilePicture().lastIndexOf("/") + 1);
            Path oldFilePath = uploadPath.resolve(oldFileName);
            try {
                if (Files.deleteIfExists(oldFilePath)) {
                    System.out.println("删除旧头像文件: " + oldFileName);
                }
            } catch (IOException e) {
                System.err.println("删除旧头像文件失败: " + e.getMessage());
            }
        }

        // 更新用户头像URL
        user.setProfilePicture(avatarUrl);
        userRepository.save(user);
        System.out.println("用户头像URL已更新: " + avatarUrl);

        return avatarUrl;
    }

    // 转换方法
    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setProfilePicture(user.getProfilePicture());
        dto.setPhone(user.getPhone());
        dto.setBio(user.getBio());

        // 设置角色，转换为字符串
        dto.setRole(user.getRole() != null ? user.getRole().name() : UserRole.USER.name());

        dto.setUndergraduateSchool(user.getCurrentSchool());
        dto.setGpa(user.getGpa());
        dto.setGreScore(user.getGreCombined());
        dto.setToeflScore(user.getToeflScore());
        dto.setIeltsScore(user.getIeltsScore());
        dto.setTargetMajor(user.getTargetMajor());

        // 直接获取GMAT分数
        dto.setGmatScore(user.getGmatTotal());

        // 添加统计数据
        dto.setPostsCount(calculateUserPostsCount(user.getId()));
        dto.setLikesReceived(calculateUserLikesReceived(user.getId()));
        dto.setCommentsCount(calculateUserCommentsCount(user.getId()));
        dto.setViewsReceived(calculateUserViewsReceived(user.getId()));

        return dto;
    }

    private User convertToEntity(UserDTO userDTO) {
        User user = new User();
        user.setEmail(userDTO.getEmail());
        user.setFullName(userDTO.getFullName());
        user.setProfilePicture(userDTO.getProfilePicture());
        user.setPhone(userDTO.getPhone());
        user.setBio(userDTO.getBio());
        user.setCurrentSchool(userDTO.getUndergraduateSchool());
        user.setGpa(userDTO.getGpa());
        user.setGreCombined(userDTO.getGreScore());
        user.setToeflScore(userDTO.getToeflScore());
        user.setIeltsScore(userDTO.getIeltsScore());
        user.setTargetMajor(userDTO.getTargetMajor());

        // 直接设置GMAT分数
        if (userDTO.getGmatScore() != null) {
            user.setGmatTotal(userDTO.getGmatScore());
        }

        // 设置角色，从字符串转换为枚举类型
        if (userDTO.getRole() != null) {
            user.setRole(UserRole.valueOf(userDTO.getRole()));
        } else {
            user.setRole(UserRole.USER);
        }

        return user;
    }

    /**
     * 获取用户保存的学校ID列表
     * 
     * @param userId 用户ID
     * @return 学校ID列表
     */
    @Transactional(readOnly = true)
    public List<Long> getSavedSchools(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在 ID: " + userId));
        return user.getSavedSchools() != null ? new ArrayList<>(user.getSavedSchools()) : new ArrayList<>();
    }

    /**
     * 切换用户保存的学校状态（添加或移除）
     * 
     * @param userId   用户ID
     * @param schoolId 学校ID
     * @param saved    是否保存
     */
    @Transactional
    public void toggleSavedSchool(Long userId, Long schoolId, boolean saved) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在 ID: " + userId));

        Set<Long> savedSchools = user.getSavedSchools();
        if (savedSchools == null) {
            savedSchools = new HashSet<>();
            user.setSavedSchools(savedSchools);
        }

        boolean alreadySaved = savedSchools.contains(schoolId);

        if (saved && !alreadySaved) {
            // 添加到收藏
            savedSchools.add(schoolId);
        } else if (!saved && alreadySaved) {
            // 从收藏中移除
            savedSchools.remove(schoolId);
        }

        userRepository.save(user);
    }

    /**
     * 统计用户总数
     * 
     * @return 用户总数
     */
    public Long countTotalUsers() {
        return userRepository.count();
    }

    /**
     * 计算用户发帖数
     * 
     * @param userId 用户ID
     * @return 发帖数
     */
    private Long calculateUserPostsCount(Long userId) {
        try {
            return forumPostRepository.countByAuthorId(userId);
        } catch (Exception e) {
            System.err.println("计算用户发帖数失败: " + e.getMessage());
            return 0L;
        }
    }

    /**
     * 计算用户获赞数
     * 
     * @param userId 用户ID
     * @return 获赞数
     */
    private Long calculateUserLikesReceived(Long userId) {
        try {
            return postLikeRepository.countLikesByUserId(userId);
        } catch (Exception e) {
            System.err.println("计算用户获赞数失败: " + e.getMessage());
            return 0L;
        }
    }

    /**
     * 计算用户评论数
     * 
     * @param userId 用户ID
     * @return 评论数
     */
    private Long calculateUserCommentsCount(Long userId) {
        try {
            return commentRepository.countByAuthorId(userId);
        } catch (Exception e) {
            System.err.println("计算用户评论数失败: " + e.getMessage());
            return 0L;
        }
    }

    /**
     * 计算用户帖子浏览量
     * 
     * @param userId 用户ID
     * @return 浏览量
     */
    private Long calculateUserViewsReceived(Long userId) {
        try {
            return forumPostRepository.sumViewsByAuthorId(userId);
        } catch (Exception e) {
            System.err.println("计算用户浏览量失败: " + e.getMessage());
            return 0L;
        }
    }
}
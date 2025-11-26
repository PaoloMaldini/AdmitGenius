package com.admitgenius.controller;

import com.admitgenius.dto.UserDTO;
import com.admitgenius.model.User;
import com.admitgenius.model.UserRole; // 需要确认 UserRole 枚举是否存在且路径正确
import com.admitgenius.payload.ApiResponse; // 需要创建这个类用于通用响应
import com.admitgenius.payload.JwtAuthenticationResponse;
import com.admitgenius.payload.LoginRequest;
import com.admitgenius.payload.SignUpRequest;
import com.admitgenius.repository.UserRepository;
import com.admitgenius.security.JwtTokenProvider;
import com.admitgenius.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import org.springframework.security.core.userdetails.UserDetails;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtTokenProvider tokenProvider;

    @Autowired
    UserService userService;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            logger.info("尝试登录用户: {}", loginRequest.getEmail());

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 生成JWT令牌
            String jwt = tokenProvider.generateToken(authentication);

            // 获取用户信息并转换为DTO
            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            // 使用UserService转换为DTO，避免序列化复杂的实体关系
            UserDTO userDTO = userService.getUserById(user.getId());

            logger.info("用户登录成功: {}", user.getId());

            // 创建响应数据
            JwtAuthenticationResponse authResponse = new JwtAuthenticationResponse(jwt, userDTO);

            // 返回符合前端期望的格式
            return ResponseEntity.ok(new ApiResponse(true, "登录成功", authResponse));
        } catch (Exception e) {
            logger.error("登录失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse(false, "登录失败: " + e.getMessage()));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        logger.info("收到注册请求: {}", signUpRequest);

        try {
            if (userRepository.existsByEmail(signUpRequest.getEmail())) {
                logger.warn("注册失败: 邮箱 {} 已被注册", signUpRequest.getEmail());
                return new ResponseEntity<>(new ApiResponse(false, "邮箱已被注册!"),
                        HttpStatus.BAD_REQUEST);
            }

            logger.info("开始创建新用户，姓名: {}, 邮箱: {}", signUpRequest.getName(), signUpRequest.getEmail());

            // 创建新用户
            User user = new User();
            user.setFullName(signUpRequest.getName());
            user.setEmail(signUpRequest.getEmail());
            user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));

            // 设置额外信息（如果提供）
            if (signUpRequest.getProfilePicture() != null) {
                user.setProfilePicture(signUpRequest.getProfilePicture());
            }

            if (signUpRequest.getUndergraduateSchool() != null) {
                user.setCurrentSchool(signUpRequest.getUndergraduateSchool());
            }

            if (signUpRequest.getGpa() != null) {
                user.setGpa(signUpRequest.getGpa());
            }

            if (signUpRequest.getGreScore() != null) {
                user.setGreCombined(signUpRequest.getGreScore());
            }

            // 根据User类中的实际字段名进行设置
            if (signUpRequest.getGmatScore() != null) {
                try {
                    user.getClass().getDeclaredField("gmatTotal");
                    user.getClass().getDeclaredMethod("setGmatTotal", Integer.class)
                            .invoke(user, signUpRequest.getGmatScore());
                } catch (Exception e) {
                    logger.warn("设置GMAT分数失败: {}", e.getMessage());
                }
            }

            // 设置角色
            if (signUpRequest.getRole() != null && !signUpRequest.getRole().isEmpty()) {
                try {
                    user.setRole(UserRole.valueOf(signUpRequest.getRole().toUpperCase()));
                } catch (IllegalArgumentException e) {
                    logger.warn("无效的角色设置: {}, 使用默认USER角色", signUpRequest.getRole());
                    user.setRole(UserRole.USER);
                }
            } else {
                user.setRole(UserRole.USER);
            }

            User result = userRepository.save(user);
            logger.info("用户注册成功: {}", result.getId());

            URI location = ServletUriComponentsBuilder
                    .fromCurrentContextPath().path("/api/users/{id}")
                    .buildAndExpand(result.getId()).toUri();

            return ResponseEntity.created(location).body(new ApiResponse(true, "用户注册成功!"));
        } catch (Exception e) {
            logger.error("注册过程中发生错误: ", e);
            return new ResponseEntity<>(new ApiResponse(false, "注册失败: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
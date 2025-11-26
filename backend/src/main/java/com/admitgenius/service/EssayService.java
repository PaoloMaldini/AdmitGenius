package com.admitgenius.service;

import com.admitgenius.dto.EssayDTO;
import com.admitgenius.model.Essay;
import com.admitgenius.model.User;
import com.admitgenius.repository.EssayRepository;
import com.admitgenius.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.admitgenius.exception.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EssayService {
    @Autowired
    private EssayRepository essayRepository;

    @Autowired
    private UserRepository userRepository;

    // 文书创建
    public EssayDTO createEssay(EssayDTO essayDTO) {
        // 输入验证
        if (essayDTO == null) {
            throw new IllegalArgumentException("文书信息不能为空");
        }

        // 1. 校验userId非空并查询用户
        Long userId = essayDTO.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在，ID: " + userId));

        // 验证标题
        if (essayDTO.getTitle() == null || essayDTO.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("文书标题不能为空");
        }

        // 验证内容
        if (essayDTO.getContent() == null || essayDTO.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("文书内容不能为空");
        }

        // 处理essayType
        String essayTypeStr = essayDTO.getEssayType();
        if (essayTypeStr == null || essayTypeStr.trim().isEmpty()) {
            throw new IllegalArgumentException("文书类型不能为空");
        }
        Essay.EssayType essayType;
        try {
            essayType = Essay.EssayType.valueOf(essayTypeStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("无效的文书类型: " + essayTypeStr);
        }

        // 4. 创建并填充Essay实体（仅处理非空字段）
        Essay essay = new Essay();
        essay.setUser(user);
        essay.setTitle(essayDTO.getTitle().trim());
        essay.setEssayType(essayType);
        essay.setContent(essayDTO.getContent().trim());
        essay.setGeneratedBy(Essay.GenerationSource.STUDENT);
        essay.setCreatedAt(LocalDateTime.now());
        essay.setUpdatedAt(LocalDateTime.now());

        // 设置可选字段
        if (essayDTO.getWordLimit() != null) {
            essay.setWordLimit(essayDTO.getWordLimit());
        }
        if (essayDTO.getSchoolId() != null) {
            essay.setSchoolId(essayDTO.getSchoolId());
        }
        if (essayDTO.getPrompt() != null && !essayDTO.getPrompt().trim().isEmpty()) {
            essay.setPrompt(essayDTO.getPrompt().trim());
        }

        // 5. 保存并转换为DTO
        Essay savedEssay = essayRepository.save(essay);
        return convertToDTO(savedEssay);
    }

    /**
     * 转换为DTO
     */
    private EssayDTO convertToDTO(Essay essay) {
        EssayDTO dto = new EssayDTO();
        dto.setId(essay.getId());
        dto.setUserId(essay.getUser().getId());
        dto.setTitle(essay.getTitle());
        dto.setContent(essay.getContent());
        dto.setEssayType(essay.getEssayType().name());
        dto.setCreatedAt(essay.getCreatedAt());
        dto.setUpdatedAt(essay.getUpdatedAt());
        dto.setGeneratedBy(essay.getGeneratedBy().name());
        dto.setStatus(essay.getStatus().name());
        dto.setWordLimit(essay.getWordLimit());
        dto.setSchoolId(essay.getSchoolId());
        dto.setPrompt(essay.getPrompt());
        return dto;
    }

    // 文书更新
    public EssayDTO updateEssay(EssayDTO essayDTO) {
        Long essayId = essayDTO.getId();
        if (essayId == null) {
            throw new IllegalArgumentException("Essay ID cannot be null");
        }

        Essay essay = essayRepository.findById(essayId)
                .orElseThrow(() -> new ResourceNotFoundException("Essay not found with id: " + essayId));

        // 更新文书类型
        if (essayDTO.getEssayType() != null) {
            Essay.EssayType essayType;
            try {
                essayType = Essay.EssayType.valueOf(essayDTO.getEssayType());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid essay type: " + essayDTO.getEssayType());
            }
            essay.setEssayType(essayType);
        }

        if (essayDTO.getTitle() != null) {
            essay.setTitle(essayDTO.getTitle());
        }
        if (essayDTO.getContent() != null) {
            essay.setContent(essayDTO.getContent());
        }

        // 更新状态
        if (essayDTO.getStatus() != null) {
            Essay.Status status;
            try {
                status = Essay.Status.valueOf(essayDTO.getStatus());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid status: " + essayDTO.getStatus());
            }
            essay.setStatus(status);
        }

        // 更新可选字段
        if (essayDTO.getWordLimit() != null) {
            essay.setWordLimit(essayDTO.getWordLimit());
        }
        if (essayDTO.getSchoolId() != null) {
            essay.setSchoolId(essayDTO.getSchoolId());
        }
        if (essayDTO.getPrompt() != null) {
            essay.setPrompt(essayDTO.getPrompt());
        }

        essay.setUpdatedAt(LocalDateTime.now());
        essay.setGeneratedBy(Essay.GenerationSource.STUDENT);

        Essay savedEssay = essayRepository.save(essay);
        return convertToDTO(savedEssay);
    }

    // 文书删除
    public void deleteEssay(Long essayId) {
        Essay essay = essayRepository.findById(essayId)
                .orElseThrow(() -> new ResourceNotFoundException("Essay not found with id: " + essayId));
        essayRepository.delete(essay);

    }

    public List<EssayDTO> getUserEssays(Long userId) {
        return getUserEssays(userId, null, null, null, null, null, null);
    }

    public List<EssayDTO> getUserEssays(Long userId, Integer page, Integer size, String keyword, String type,
            String status, String sort) {
        List<Essay> essays = essayRepository.findByUserId(userId);

        // 应用关键词搜索
        if (keyword != null && !keyword.trim().isEmpty()) {
            String lowerKeyword = keyword.toLowerCase();
            essays = essays.stream()
                    .filter(essay -> essay.getTitle().toLowerCase().contains(lowerKeyword) ||
                            (essay.getContent() != null && essay.getContent().toLowerCase().contains(lowerKeyword)))
                    .collect(Collectors.toList());
        }

        // 应用文书类型筛选
        if (type != null && !type.trim().isEmpty()) {
            essays = essays.stream()
                    .filter(essay -> essay.getEssayType().name().equals(type))
                    .collect(Collectors.toList());
        }

        // 应用状态筛选
        if (status != null && !status.trim().isEmpty()) {
            String statusUpper = status.toUpperCase();
            if ("DRAFT".equals(statusUpper) || "COMPLETED".equals(statusUpper)) {
                essays = essays.stream()
                        .filter(essay -> essay.getStatus().name().equals(statusUpper))
                        .collect(Collectors.toList());
            }
        }

        // 应用排序
        if (sort != null && !sort.trim().isEmpty()) {
            switch (sort) {
                case "updatedAt_desc":
                    essays.sort((e1, e2) -> e2.getUpdatedAt().compareTo(e1.getUpdatedAt()));
                    break;
                case "createdAt_asc":
                    essays.sort((e1, e2) -> e1.getCreatedAt().compareTo(e2.getCreatedAt()));
                    break;
                case "createdAt_desc":
                    essays.sort((e1, e2) -> e2.getCreatedAt().compareTo(e1.getCreatedAt()));
                    break;
                case "title_asc":
                    essays.sort((e1, e2) -> e1.getTitle().compareToIgnoreCase(e2.getTitle()));
                    break;
                default:
                    // 默认按更新时间倒序
                    essays.sort((e1, e2) -> e2.getUpdatedAt().compareTo(e1.getUpdatedAt()));
                    break;
            }
        } else {
            // 默认排序：按更新时间倒序
            essays.sort((e1, e2) -> e2.getUpdatedAt().compareTo(e1.getUpdatedAt()));
        }

        return essays.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public EssayDTO getEssayById(Long essayId) {
        Essay essay = essayRepository.findById(essayId)
                .orElseThrow(() -> new ResourceNotFoundException("Essay not found with id: " + essayId));
        return convertToDTO(essay);
    }

}
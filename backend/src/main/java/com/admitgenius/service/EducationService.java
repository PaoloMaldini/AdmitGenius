package com.admitgenius.service;

import com.admitgenius.dto.EducationDTO;
import com.admitgenius.model.Education;
import com.admitgenius.repository.EducationRepository;
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
public class EducationService {

    private final EducationRepository educationRepository;

    /**
     * 获取用户的所有教育经历
     */
    public List<EducationDTO> getUserEducations(Long userId) {
        log.info("获取用户教育经历，用户ID: {}", userId);
        List<Education> educations = educationRepository.findByUserIdOrderByEndDateDesc(userId);
        return educations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 创建教育经历
     */
    public EducationDTO createEducation(Long userId, EducationDTO educationDTO) {
        log.info("创建教育经历，用户ID: {}, 学校: {}", userId, educationDTO.getSchool());

        Education education = convertToEntity(educationDTO);
        education.setUserId(userId);

        Education savedEducation = educationRepository.save(education);
        log.info("教育经历创建成功，ID: {}", savedEducation.getId());

        return convertToDTO(savedEducation);
    }

    /**
     * 更新教育经历
     */
    public EducationDTO updateEducation(Long userId, Long educationId, EducationDTO educationDTO) {
        log.info("更新教育经历，用户ID: {}, 教育经历ID: {}", userId, educationId);

        Education existingEducation = educationRepository.findByIdAndUserId(educationId, userId);
        if (existingEducation == null) {
            throw new RuntimeException("教育经历不存在或无权限访问");
        }

        // 更新字段
        existingEducation.setSchool(educationDTO.getSchool());
        existingEducation.setDegree(educationDTO.getDegree());
        existingEducation.setMajor(educationDTO.getMajor());
        existingEducation.setStartDate(educationDTO.getStartDate());
        existingEducation.setEndDate(educationDTO.getEndDate());
        existingEducation.setAchievement(educationDTO.getAchievement());

        Education updatedEducation = educationRepository.save(existingEducation);
        log.info("教育经历更新成功，ID: {}", updatedEducation.getId());

        return convertToDTO(updatedEducation);
    }

    /**
     * 删除教育经历
     */
    public void deleteEducation(Long userId, Long educationId) {
        log.info("删除教育经历，用户ID: {}, 教育经历ID: {}", userId, educationId);

        Education education = educationRepository.findByIdAndUserId(educationId, userId);
        if (education == null) {
            throw new RuntimeException("教育经历不存在或无权限访问");
        }

        educationRepository.deleteByIdAndUserId(educationId, userId);
        log.info("教育经历删除成功，ID: {}", educationId);
    }

    /**
     * 将实体转换为DTO
     */
    private EducationDTO convertToDTO(Education education) {
        EducationDTO dto = new EducationDTO();
        dto.setId(education.getId());
        dto.setSchool(education.getSchool());
        dto.setDegree(education.getDegree());
        dto.setMajor(education.getMajor());
        dto.setStartDate(education.getStartDate());
        dto.setEndDate(education.getEndDate());
        dto.setAchievement(education.getAchievement());
        return dto;
    }

    /**
     * 将DTO转换为实体
     */
    private Education convertToEntity(EducationDTO dto) {
        Education education = new Education();
        education.setSchool(dto.getSchool());
        education.setDegree(dto.getDegree());
        education.setMajor(dto.getMajor());
        education.setStartDate(dto.getStartDate());
        education.setEndDate(dto.getEndDate());
        education.setAchievement(dto.getAchievement());
        return education;
    }
}
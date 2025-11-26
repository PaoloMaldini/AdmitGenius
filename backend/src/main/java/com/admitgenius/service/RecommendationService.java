package com.admitgenius.service;

import com.admitgenius.dto.RecommendationRequestDTO;
import com.admitgenius.dto.RecommendationResponseDTO;
import com.admitgenius.dto.SchoolDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RecommendationService {
    /**
     * 基于学生背景生成学校推荐
     * 
     * @param request 包含学生背景和推荐偏好的请求
     * @return 推荐响应，包含推荐学校和匹配原因
     */
    RecommendationResponseDTO generateRecommendation(RecommendationRequestDTO request);

    /**
     * 获取用户的所有推荐历史
     * 
     * @param userId 用户ID
     * @return 推荐响应列表
     */
    List<RecommendationResponseDTO> getUserRecommendations(Long userId);

    /**
     * 为特定推荐项提供反馈
     * 
     * @param itemId   推荐项ID
     * @param feedback 反馈内容
     * @param applied  是否已申请
     */
    void provideFeedback(Long itemId, String feedback, boolean applied);

    /**
     * 获取所有学校列表 (分页)
     * 
     * @param pageable 分页信息
     * @return 学校DTO的分页列表
     */
    Page<SchoolDTO> getAllSchools(Pageable pageable);

    /**
     * 根据ID获取学校
     * 
     * @param id 学校ID
     * @return 学校DTO
     */
    SchoolDTO getSchoolById(Long id);

    /**
     * 为指定用户推荐学校(简化版)
     * 
     * @param userId 用户ID
     * @return 推荐学校列表
     */
    List<SchoolDTO> recommendSchools(Long userId);

    /**
     * 获取所有可用的专业列表
     * 
     * @return 专业名称列表
     */
    List<String> getAvailablePrograms();

    /**
     * 获取所有可用的国家/地区列表
     * 
     * @return 国家/地区名称列表
     */
    List<String> getAvailableCountries();

    /**
     * 按名称搜索学校 (分页)
     * 
     * @param nameQuery 搜索关键词
     * @param pageable  分页信息
     * @return 学校DTO的分页列表
     */
    Page<SchoolDTO> searchSchoolsByName(String nameQuery, Pageable pageable);

    /**
     * 按国家筛选学校 (分页)
     * 
     * @param country  国家名称
     * @param pageable 分页信息
     * @return 学校DTO的分页列表
     */
    Page<SchoolDTO> getSchoolsByCountry(String country, Pageable pageable);

    /**
     * 按专业筛选学校 (分页)
     * 
     * @param programName 专业名称
     * @param pageable    分页信息
     * @return 学校DTO的分页列表
     */
    Page<SchoolDTO> getSchoolsByProgram(String programName, Pageable pageable);
}
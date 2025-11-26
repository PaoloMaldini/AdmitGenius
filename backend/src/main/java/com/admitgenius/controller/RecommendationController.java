package com.admitgenius.controller;

import com.admitgenius.dto.RecommendationRequestDTO;
import com.admitgenius.dto.RecommendationResponseDTO;
import com.admitgenius.dto.SchoolDTO;
import com.admitgenius.dto.SchoolProgramDTO;
import com.admitgenius.service.RecommendationService;
import com.admitgenius.service.SchoolService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recommendations")
@Validated
public class RecommendationController {
    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private SchoolService schoolService;

    /**
     * 生成推荐
     * 文档 4.6.1: POST /api/recommendations/generate
     * 
     * 根据用户提供的信息生成学校或项目推荐
     * 
     * @param request 包含用户背景和偏好的请求DTO
     * @return 推荐结果
     */
    @PostMapping("/generate")
    public ResponseEntity<RecommendationResponseDTO> generateRecommendation(
            @Valid @RequestBody RecommendationRequestDTO request) {
        RecommendationResponseDTO recommendation = recommendationService.generateRecommendation(request);
        return ResponseEntity.ok(recommendation);
    }

    /**
     * 获取特定用户的推荐
     * 文档 4.6.2: GET /api/recommendations/user/{userId}
     * 
     * 获取特定用户的历史推荐结果
     * 
     * @param userId 用户ID
     * @return 用户的推荐列表
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RecommendationResponseDTO>> getUserRecommendations(@PathVariable Long userId) {
        List<RecommendationResponseDTO> recommendations = recommendationService.getUserRecommendations(userId);
        return ResponseEntity.ok(recommendations);
    }

    /**
     * 提交对推荐项目的反馈
     * 文档 4.6.3: POST /api/recommendations/feedback/{itemId}
     * 
     * 提交用户对特定推荐项的反馈
     * 
     * @param itemId   推荐项ID
     * @param feedback 包含反馈内容和是否已申请的信息
     * @return 成功响应
     */
    @PostMapping("/feedback/{itemId}")
    public ResponseEntity<?> provideFeedback(
            @PathVariable Long itemId,
            @RequestBody Map<String, Object> feedback) {

        String feedbackText = (String) feedback.get("feedback");
        boolean applied = Boolean.TRUE.equals(feedback.get("applied"));

        recommendationService.provideFeedback(itemId, feedbackText, applied);
        return ResponseEntity.ok().build();
    }

    /**
     * 获取用户的简化推荐
     * 文档 4.6.4: GET /api/recommendations/simple/{userId}
     * 
     * 获取针对用户的简化学校推荐列表
     * 
     * @param userId 用户ID
     * @return 简化的学校推荐列表
     */
    @GetMapping("/simple/{userId}")
    public ResponseEntity<List<SchoolDTO>> getSimpleRecommendations(@PathVariable Long userId) {
        List<SchoolDTO> recommendations = recommendationService.recommendSchools(userId);
        return ResponseEntity.ok(recommendations);
    }

    /**
     * 获取所有学校的列表 (分页)
     * 
     * @param pageable 分页参数 (e.g., ?page=0&size=10&sort=name,asc)
     * @return 学校DTO的分页列表
     */
    @GetMapping("/schools")
    public ResponseEntity<Page<SchoolDTO>> getAllSchools(@PageableDefault(size = 10) Pageable pageable) {
        Page<SchoolDTO> schools = recommendationService.getAllSchools(pageable);
        return ResponseEntity.ok(schools);
    }

    /**
     * 根据ID获取特定学校的详情
     * 文档 4.6.6: GET /api/recommendations/schools/{id}
     * 
     * 获取特定学校的详细信息
     * 
     * @param id 学校ID
     * @return 学校详情
     */
    @GetMapping("/schools/{id}")
    public ResponseEntity<SchoolDTO> getSchoolById(@PathVariable Long id) {
        SchoolDTO school = recommendationService.getSchoolById(id);
        return ResponseEntity.ok(school);
    }

    /**
     * 获取学校的所有专业列表
     * 
     * @param id 学校ID
     * @return 专业列表
     */
    @GetMapping("/schools/{id}/programs")
    public ResponseEntity<List<SchoolProgramDTO>> getSchoolPrograms(@PathVariable Long id) {
        List<SchoolProgramDTO> programs = schoolService.getAllProgramsBySchool(id);
        return ResponseEntity.ok(programs);
    }

    /**
     * 按名称搜索学校 (分页)
     * 
     * @param query    搜索关键词
     * @param pageable 分页参数
     * @return 学校DTO的分页列表
     */
    @GetMapping("/schools/search")
    public ResponseEntity<Page<SchoolDTO>> searchSchoolsByName(
            @RequestParam("q") String query,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<SchoolDTO> schools = recommendationService.searchSchoolsByName(query, pageable);
        return ResponseEntity.ok(schools);
    }

    /**
     * 按国家筛选学校 (分页)
     * 
     * @param country  国家名称
     * @param pageable 分页参数
     * @return 学校DTO的分页列表
     */
    @GetMapping("/schools/country/{country}")
    public ResponseEntity<Page<SchoolDTO>> getSchoolsByCountry(
            @PathVariable String country,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<SchoolDTO> schools = recommendationService.getSchoolsByCountry(country, pageable);
        return ResponseEntity.ok(schools);
    }

    /**
     * 按专业筛选学校 (分页)
     * 
     * @param program  专业名称
     * @param pageable 分页参数
     * @return 学校DTO的分页列表
     */
    @GetMapping("/schools/program/{programName}")
    public ResponseEntity<Page<SchoolDTO>> getSchoolsByProgram(
            @PathVariable String programName,
            @PageableDefault(size = 10) Pageable pageable) {
        System.out.println("收到专业筛选请求: " + programName);
        Page<SchoolDTO> schools = recommendationService.getSchoolsByProgram(programName, pageable);
        System.out.println("返回学校数量: " + schools.getTotalElements());
        return ResponseEntity.ok(schools);
    }

    /**
     * 获取所有可用专业列表
     * 
     * @return 专业名称列表
     */
    @GetMapping("/programs")
    public ResponseEntity<List<String>> getAvailablePrograms() {
        List<String> programs = recommendationService.getAvailablePrograms();
        return ResponseEntity.ok(programs);
    }

    /**
     * 获取所有可用国家/地区列表
     * 
     * @return 国家/地区名称列表
     */
    @GetMapping("/countries")
    public ResponseEntity<List<String>> getAvailableCountries() {
        List<String> countries = recommendationService.getAvailableCountries();
        return ResponseEntity.ok(countries);
    }
}
package com.admitgenius.service.impl;

import com.admitgenius.dto.RecommendationRequestDTO;
import com.admitgenius.dto.RecommendationResponseDTO;
import com.admitgenius.dto.RecommendationResponseDTO.RecommendationItemDTO;
import com.admitgenius.dto.SchoolDTO;
import com.admitgenius.model.User;
import com.admitgenius.model.School;
import com.admitgenius.model.SchoolProgram;
import com.admitgenius.model.Recommendation;
import com.admitgenius.model.RecommendationItem;
import com.admitgenius.repository.UserRepository;
import com.admitgenius.repository.SchoolRepository;
import com.admitgenius.repository.SchoolProgramRepository;
import com.admitgenius.repository.RecommendationRepository;
import com.admitgenius.repository.RecommendationItemRepository;
import com.admitgenius.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationServiceImpl implements RecommendationService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SchoolRepository schoolRepository;

    @Autowired
    private SchoolProgramRepository schoolProgramRepository;

    @Autowired
    private RecommendationRepository recommendationRepository;

    @Autowired
    private RecommendationItemRepository recommendationItemRepository;

    @Override
    @Transactional
    public RecommendationResponseDTO generateRecommendation(RecommendationRequestDTO request) {
        // 输入验证
        if (request == null) {
            throw new IllegalArgumentException("推荐请求不能为空");
        }
        if (request.getUserId() == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (request.getCount() == null || request.getCount() <= 0) {
            request.setCount(10); // 默认推荐10所学校
        }
        if (request.getRecommendationType() == null || request.getRecommendationType().trim().isEmpty()) {
            request.setRecommendationType("ACADEMIC"); // 默认学术推荐
        }

        // 1. 验证用户存在
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 2. 创建推荐记录
        Recommendation recommendation = new Recommendation();
        recommendation.setStudent(user);
        recommendation.setCreatedAt(LocalDateTime.now());
        recommendation.setInputSummary(generateInputSummary(request));

        try {
            recommendation.setRecommendationType(
                    Recommendation.RecommendationType.valueOf(request.getRecommendationType().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("无效的推荐类型: " + request.getRecommendationType());
        }

        recommendation = recommendationRepository.save(recommendation);

        // 3. 获取所有学校
        List<School> allSchools = schoolRepository.findAll();
        if (allSchools.isEmpty()) {
            // throw new RuntimeException("系统中暂无学校数据，无法生成推荐");
            // Return empty recommendation instead of throwing error if no schools
            return convertToResponseDTO(recommendation, request); // Pass request for DTO conversion context
        }

        // 4. 计算匹配分数并排序
        List<SchoolMatchResult> matchResults = calculateMatchScores(allSchools, request);

        // 5. 获取排名靠前的N所学校
        List<SchoolMatchResult> topMatches = matchResults.stream()
                .sorted(Comparator.comparing(SchoolMatchResult::getMatchScore).reversed())
                .limit(request.getCount())
                .collect(Collectors.toList());

        // 6. 创建推荐项目
        List<RecommendationItem> items = new ArrayList<>();
        int rank = 1;

        for (SchoolMatchResult result : topMatches) {
            RecommendationItem item = new RecommendationItem();
            item.setRecommendation(recommendation);
            item.setSchool(result.getSchool());

            // 如果需要匹配项目，查找最匹配的项目
            if (request.getTargetMajor() != null && !request.getTargetMajor().trim().isEmpty()) {
                Optional<SchoolProgram> bestProgram = findBestProgram(result.getSchool(), request.getTargetMajor());
                bestProgram.ifPresent(item::setProgram);
            }

            item.setMatchScore(result.getMatchScore());
            item.setRank(rank++);
            item.setMatchReason(result.getMatchReason());
            item.setIsApplied(false);

            items.add(recommendationItemRepository.save(item));
        }

        recommendation.setItems(items); // Set items back to recommendation before converting DTO
        return convertToResponseDTO(recommendation, request); // Pass request for DTO conversion context
    }

    @Override
    public List<RecommendationResponseDTO> getUserRecommendations(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        List<Recommendation> recommendations = recommendationRepository.findByStudentOrderByCreatedAtDesc(user);

        return recommendations.stream()
                .map(rec -> convertToResponseDTO(rec, null)) // Pass null or a default request for DTO conversion
                .collect(Collectors.toList());
    }

    @Override
    public void provideFeedback(Long itemId, String feedback, boolean applied) {
        RecommendationItem item = recommendationItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("推荐项不存在"));

        item.setFeedback(feedback);

        if (applied) {
            item.markAsApplied();
        }

        recommendationItemRepository.save(item);
    }

    // 计算学校匹配分数
    private List<SchoolMatchResult> calculateMatchScores(List<School> schools, RecommendationRequestDTO request) {
        List<SchoolMatchResult> results = new ArrayList<>();
        List<Integer> requestedRankingRange = request.getRankingRange();

        for (School school : schools) {
            if (school == null)
                continue;
            Integer schoolRanking = school.getRanking();

            // 1. Ranking Preference Filter
            if (requestedRankingRange != null && !requestedRankingRange.isEmpty()) {
                if (requestedRankingRange.size() == 2) { // Ensure range is valid
                    if (schoolRanking == null) {
                        continue; // Skip if school has no ranking data when range is specified
                    }
                    int minRank = requestedRankingRange.get(0);
                    int maxRank = requestedRankingRange.get(1);
                    if (schoolRanking < minRank || schoolRanking > maxRank) {
                        continue; // Skip school if its ranking is outside the preferred range
                    }
                } else {
                    // Handle invalid rankingRange size, perhaps log a warning or ignore
                    System.err.println("Warning: received invalid rankingRange size: " + requestedRankingRange.size());
                }
            }

            float score = 0.0f;
            List<String> reasons = new ArrayList<>();

            // Add reason if ranking was a factor and school is in range
            if (requestedRankingRange != null && !requestedRankingRange.isEmpty() && requestedRankingRange.size() == 2
                    &&
                    schoolRanking != null && schoolRanking >= requestedRankingRange.get(0)
                    && schoolRanking <= requestedRankingRange.get(1)) {
                reasons.add("学校排名在您的偏好范围内 (" + requestedRankingRange.get(0) + "-" + requestedRankingRange.get(1) + ")");
                // Optional: Add a base score for ranking match, e.g., score += 0.1f;
            }

            // GPA匹配 (权重：30%)
            if (request.getGpa() != null && school.getAverageGPA() != null) {
                float gpaScore = calculateGPAScore(request.getGpa(), school.getAverageGPA());
                score += gpaScore * 0.3f;

                if (gpaScore > 0.7f) {
                    reasons.add("您的GPA与该校招生要求非常匹配");
                } else if (gpaScore > 0.5f) {
                    reasons.add("您的GPA在该校招生范围内");
                }
            }

            // 标准化考试匹配 (权重：30%)
            float testScore = 0.0f;

            // GRE分数
            if (request.getGreScore() != null) {
                float greScoreResult = 0.0f;

                // GRE语文部分
                if (request.getGreVerbal() != null && school.getAverageGREVerbal() != null) {
                    float greVerbalScore = calculateGREScore(request.getGreVerbal(), school.getAverageGREVerbal());
                    greScoreResult += greVerbalScore * 0.3f;

                    if (greVerbalScore > 0.8f) {
                        reasons.add("您的GRE语文分数明显高于该校平均水平");
                    } else if (greVerbalScore > 0.6f) {
                        reasons.add("您的GRE语文分数符合该校招生要求");
                    }
                }

                // GRE数学部分
                if (request.getGreQuantitative() != null && school.getAverageGREQuant() != null) {
                    float greQuantScore = calculateGREScore(request.getGreQuantitative(), school.getAverageGREQuant());
                    greScoreResult += greQuantScore * 0.3f;

                    if (greQuantScore > 0.8f && !reasons.contains("您的GRE语文分数明显高于该校平均水平")) {
                        reasons.add("您的GRE数学分数明显高于该校平均水平");
                    } else if (greQuantScore > 0.6f && !reasons.contains("您的GRE语文分数符合该校招生要求")) {
                        reasons.add("您的GRE数学分数符合该校招生要求");
                    }
                }

                // GRE分析性写作部分
                if (request.getGreAnalytical() != null && school.getAverageGREAW() != null) {
                    float greAWScore = calculateGREAWScore(request.getGreAnalytical(), school.getAverageGREAW());
                    greScoreResult += greAWScore * 0.4f;

                    if (greAWScore > 0.8f && !reasons.contains("您的GRE语文分数明显高于该校平均水平")
                            && !reasons.contains("您的GRE数学分数明显高于该校平均水平")) {
                        reasons.add("您的GRE分析性写作分数明显高于该校平均水平");
                    } else if (greAWScore > 0.6f && !reasons.contains("您的GRE语文分数符合该校招生要求")
                            && !reasons.contains("您的GRE数学分数符合该校招生要求")) {
                        reasons.add("您的GRE分析性写作分数符合该校招生要求");
                    }
                }

                testScore = Math.max(testScore, greScoreResult);
            }

            // GMAT分数
            if (request.getGmatScore() != null && school.getAverageGMAT() != null) {
                float gmatScore = calculateGMATScore(request.getGmatScore(), school.getAverageGMAT());
                testScore = Math.max(testScore, gmatScore);

                if (gmatScore > 0.8f && !reasons.contains("您的GRE语文分数明显高于该校平均水平")
                        && !reasons.contains("您的GRE数学分数明显高于该校平均水平")
                        && !reasons.contains("您的GRE分析性写作分数明显高于该校平均水平")) {
                    reasons.add("您的GMAT分数明显高于该校平均水平");
                } else if (gmatScore > 0.6f && !reasons.contains("您的GRE语文分数符合该校招生要求")
                        && !reasons.contains("您的GRE数学分数符合该校招生要求")
                        && !reasons.contains("您的GRE分析性写作分数符合该校招生要求")) {
                    reasons.add("您的GMAT分数符合该校招生要求");
                }
            }

            score += testScore * 0.3f;

            // 地点偏好匹配 (权重：15%)
            if (request.getLocationPreferences() != null && !request.getLocationPreferences().isEmpty()
                    && school.getLocation() != null) {
                float locationScore = calculateLocationScore(school.getLocation(), request.getLocationPreferences());
                score += locationScore * 0.15f;

                if (locationScore > 0.9f) {
                    reasons.add("学校位置完全符合您的地区偏好");
                } else if (locationScore > 0.5f) {
                    reasons.add("学校位置与您的部分地区偏好相符");
                }
            }

            // 学校类型偏好匹配 (权重：15%)
            if (request.getSchoolTypePreferences() != null && !request.getSchoolTypePreferences().isEmpty()) {
                // 假设学校有一个tag或type字段
                float typeScore = 0.5f; // 默认中等匹配
                score += typeScore * 0.15f;

                // 如果有Ivy League偏好且学校是常春藤
                if (request.getSchoolTypePreferences().contains("Ivy League") &&
                        Boolean.TRUE.equals(school.getIsIvyLeague())) {
                    reasons.add("常春藤联盟院校符合您的学校类型偏好");
                }
            }

            // 专业匹配 (权重：10%)
            if (request.getTargetMajor() != null && !request.getTargetMajor().isEmpty()) {
                Optional<SchoolProgram> bestProgram = findBestProgram(school, request.getTargetMajor());
                if (bestProgram.isPresent()) {
                    score += 0.1f;
                    reasons.add("学校提供您感兴趣的 " + bestProgram.get().getName() + " 专业");
                }
            }

            // 创建匹配结果
            SchoolMatchResult result = new SchoolMatchResult();
            result.setSchool(school);
            result.setMatchScore(score);
            result.setMatchReason(String.join("；", reasons));

            results.add(result);
        }

        return results;
    }

    private float calculateGPAScore(double userGPA, double schoolGPA) {
        // 计算用户GPA与学校GPA的匹配度，返回0-1的分数
        // 基本逻辑：用户GPA >= 学校GPA时为高匹配，否则按差距减分
        if (userGPA >= schoolGPA) {
            return 1.0f;
        } else {
            float difference = (float) (schoolGPA - userGPA);
            // GPA差距每0.3减0.2分
            return Math.max(0, 1.0f - (difference / 0.3f) * 0.2f);
        }
    }

    /**
     * 计算GRE分数匹配度
     * 
     * @param userScore 用户GRE分数
     * @param schoolAvg 学校平均GRE分数
     * @return 匹配度分数(0-1)
     */
    private float calculateGREScore(int userScore, int schoolAvg) {
        // GRE各部分分数范围：130-170，计算匹配度
        if (userScore >= schoolAvg) {
            return 1.0f;
        } else {
            float difference = schoolAvg - userScore;
            // 差距每5分减0.2分
            return Math.max(0, 1.0f - (difference / 5.0f) * 0.2f);
        }
    }

    /**
     * 计算GRE分析性写作匹配度
     * 
     * @param userScore 用户GRE AW分数
     * @param schoolAvg 学校平均GRE AW分数
     * @return 匹配度分数(0-1)
     */
    private float calculateGREAWScore(double userScore, double schoolAvg) {
        // GRE AW分数范围：0.0-6.0，计算匹配度
        if (userScore >= schoolAvg) {
            return 1.0f;
        } else {
            float difference = (float) (schoolAvg - userScore);
            // 差距每0.5分减0.2分
            return Math.max(0, 1.0f - (difference / 0.5f) * 0.2f);
        }
    }

    /**
     * 计算GMAT分数匹配度
     * 
     * @param userScore 用户GMAT分数
     * @param schoolAvg 学校平均GMAT分数
     * @return 匹配度分数(0-1)
     */
    private float calculateGMATScore(int userScore, int schoolAvg) {
        // GMAT分数范围：200-800，计算匹配度
        if (userScore >= schoolAvg) {
            return 1.0f;
        } else {
            float difference = schoolAvg - userScore;
            // 差距每30分减0.2分
            return Math.max(0, 1.0f - (difference / 30.0f) * 0.2f);
        }
    }

    private float calculateLocationScore(String schoolLocation, List<String> preferredLocations) {
        if (schoolLocation == null || preferredLocations == null || preferredLocations.isEmpty()) {
            return 0.0f; // Return 0 if essential data is missing
        }
        if (preferredLocations.contains(schoolLocation)) {
            return 1.0f;
        }

        // 处理状态/地区部分匹配的情况
        for (String location : preferredLocations) {
            if (schoolLocation.contains(location) || location.contains(schoolLocation)) {
                return 0.7f;
            }
        }

        return 0.0f;
    }

    private Optional<SchoolProgram> findBestProgram(School school, String targetMajor) {
        if (school == null || targetMajor == null || targetMajor.trim().isEmpty()) {
            return Optional.empty();
        }
        // 假设学校与其专业之间有一对多关系
        List<SchoolProgram> programs = schoolProgramRepository.findBySchool(school);
        if (programs == null || programs.isEmpty()) {
            return Optional.empty();
        }

        String targetMajorLower = targetMajor.toLowerCase();

        return programs.stream()
                .filter(program -> {
                    if (program == null)
                        return false;
                    boolean nameMatch = false;
                    if (program.getName() != null) {
                        nameMatch = program.getName().toLowerCase().contains(targetMajorLower);
                    }
                    boolean keywordMatch = false;
                    if (program.getKeywords() != null) {
                        keywordMatch = program.getKeywords().stream()
                                .filter(Objects::nonNull) // Ensure keyword itself is not null
                                .anyMatch(keyword -> keyword.toLowerCase().contains(targetMajorLower));
                    }
                    return nameMatch || keywordMatch;
                })
                .findFirst();
    }

    private String generateInputSummary(RecommendationRequestDTO request) {
        StringBuilder summary = new StringBuilder();
        summary.append("GPA: ").append(request.getGpa());

        if (request.getGreScore() != null) {
            summary.append(", GRE: ").append(request.getGreScore());
        }

        if (request.getGmatScore() != null) {
            summary.append(", GMAT: ").append(request.getGmatScore());
        }

        if (request.getTargetMajor() != null) {
            summary.append(", 目标专业: ").append(request.getTargetMajor());
        }

        if (request.getLocationPreferences() != null && !request.getLocationPreferences().isEmpty()) {
            summary.append(", 地区偏好: ").append(String.join(", ", request.getLocationPreferences()));
        }

        return summary.toString();
    }

    private RecommendationItemDTO convertToItemDTO(RecommendationItem item, RecommendationRequestDTO requestContext) {
        RecommendationItemDTO dto = new RecommendationItemDTO();
        School school = item.getSchool();

        dto.setId(item.getId());
        dto.setSchoolId(school.getId());
        dto.setSchoolName(school.getName());
        dto.setSchoolLocation(school.getLocation());
        dto.setSchoolLogo(school.getImageUrl());
        dto.setSchoolRanking(school.getRanking());

        if (item.getProgram() != null) {
            SchoolProgram program = item.getProgram();
            dto.setProgramId(program.getId());
            dto.setProgramName(program.getName());
            dto.setDepartment(program.getDepartment());
            if (program.getDegreeLevel() != null) { // Check for null before calling toString()
                dto.setDegreeLevel(program.getDegreeLevel().toString());
            }

            // Check requestContext before accessing its properties
            if (requestContext != null && Boolean.TRUE.equals(requestContext.getIncludeTuitionInfo())) {
                dto.setTuitionFee(program.getTuitionFee());
                dto.setScholarshipAvailable(program.getScholarshipAvailable());
            }
        }

        dto.setMatchScore(item.getMatchScore());
        dto.setRank(item.getRank());
        dto.setMatchReason(item.getMatchReason());
        dto.setIsApplied(item.getIsApplied());
        dto.setFeedback(item.getFeedback());

        return dto;
    }

    private Map<String, Object> generateStatistics(List<RecommendationItemDTO> items) {
        Map<String, Object> stats = new HashMap<>();

        // 计算平均排名
        double avgRanking = items.stream()
                .filter(item -> item.getSchoolRanking() != null)
                .mapToInt(RecommendationItemDTO::getSchoolRanking)
                .average()
                .orElse(0);
        stats.put("avgRanking", avgRanking);

        // 计算平均学费
        double avgTuition = items.stream()
                .filter(item -> item.getTuitionFee() != null)
                .mapToDouble(item -> {
                    String fee = item.getTuitionFee();
                    // 移除货币符号和其他非数字字符
                    fee = fee.replaceAll("[^\\d.]", "");
                    try {
                        return Double.parseDouble(fee);
                    } catch (NumberFormatException e) {
                        return 0.0;
                    }
                })
                .average()
                .orElse(0);
        stats.put("avgTuition", avgTuition);

        // 地区分布
        Map<String, Integer> locationDistribution = new HashMap<>();
        items.forEach(item -> {
            locationDistribution.merge(item.getSchoolLocation(), 1, Integer::sum);
        });
        stats.put("locationDistribution", locationDistribution);

        return stats;
    }

    private RecommendationResponseDTO convertToResponseDTO(Recommendation recommendation,
            RecommendationRequestDTO requestContext) {
        RecommendationResponseDTO dto = new RecommendationResponseDTO();
        dto.setId(recommendation.getId());
        dto.setUserId(recommendation.getStudent().getId());
        dto.setCreatedAt(recommendation.getCreatedAt());
        dto.setExplanation("Generated recommendations based on your profile.");

        List<RecommendationItemDTO> itemDTOs = recommendation.getItems().stream()
                .map(item -> convertToItemDTO(item, requestContext))
                .collect(Collectors.toList());

        dto.setItems(itemDTOs);

        // if (!itemDTOs.isEmpty()) {
        // Map<String, Object> stats = generateStatistics(itemDTOs); //
        // generateStatistics needs to be defined
        // dto.setStatistics(stats);
        // }
        return dto;
    }

    // 辅助内部类，用于存储匹配结果
    private static class SchoolMatchResult {
        private School school;
        private float matchScore;
        private String matchReason;

        public School getSchool() {
            return school;
        }

        public void setSchool(School school) {
            this.school = school;
        }

        public float getMatchScore() {
            return matchScore;
        }

        public void setMatchScore(float matchScore) {
            this.matchScore = matchScore;
        }

        public String getMatchReason() {
            return matchReason;
        }

        public void setMatchReason(String matchReason) {
            this.matchReason = matchReason;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<SchoolDTO> recommendSchools(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + userId));

        List<School> allSchools = schoolRepository.findAll(); // This could be large

        // Simple logic for now: filter top N schools by some criteria or random
        // Placeholder: In a real scenario, this would involve a more complex matching
        // algorithm
        List<School> matchingSchools = allSchools.stream()
                // .filter(school -> isGoodMatch(user, school)) // Hypothetical matching logic
                .sorted(Comparator.comparing(School::getRanking, Comparator.nullsLast(Comparator.naturalOrder())))
                .limit(10) // Limit to 10 for simplicity
                .collect(Collectors.toList());
        return matchingSchools.stream().map(this::convertToSchoolDTO).collect(Collectors.toList());
    }

    private SchoolDTO convertToSchoolDTO(School school) {
        SchoolDTO dto = new SchoolDTO();
        dto.setId(school.getId());
        dto.setName(school.getName());
        dto.setLocation(school.getLocation());
        dto.setRanking(school.getRanking());
        dto.setAcceptanceRate(school.getAcceptanceRate());
        dto.setAverageGREVerbal(school.getAverageGREVerbal());
        dto.setAverageGREQuant(school.getAverageGREQuant());
        dto.setAverageGREAW(school.getAverageGREAW());
        dto.setAverageGMAT(school.getAverageGMAT());
        dto.setAverageGPA(school.getAverageGPA());
        dto.setIsIvyLeague(school.getIsIvyLeague());
        dto.setDescription(school.getDescription());
        dto.setWebsite(school.getWebsite());
        dto.setImageUrl(school.getImageUrl());
        dto.setHasScholarship(school.getHasScholarship());
        dto.setTuitionFee(school.getTuitionFee());
        dto.setSchoolType(school.getSchoolType() != null ? school.getSchoolType().name() : null);
        dto.setAdmissionRequirements(school.getAdmissionRequirements());
        dto.setTopPrograms(school.getTopPrograms());
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SchoolDTO> getAllSchools(Pageable pageable) {
        Page<School> schoolPage = schoolRepository.findAll(pageable);
        return schoolPage.map(this::convertToSchoolDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public SchoolDTO getSchoolById(Long id) {
        School school = schoolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("学校不存在: " + id)); // Consider ResourceNotFoundException
        return convertToSchoolDTO(school);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAvailablePrograms() {
        return schoolProgramRepository.findAll().stream()
                .map(SchoolProgram::getName)
                .filter(name -> name != null && !name.trim().isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAvailableCountries() {
        return schoolRepository.findAll().stream()
                .map(School::getLocation) // Assuming location primarily holds country, or needs parsing
                .filter(location -> location != null && !location.trim().isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SchoolDTO> searchSchoolsByName(String nameQuery, Pageable pageable) {
        Page<School> schoolPage = schoolRepository.findByNameContainingIgnoreCase(nameQuery, pageable);
        return schoolPage.map(this::convertToSchoolDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SchoolDTO> getSchoolsByCountry(String country, Pageable pageable) {
        // Current SchoolRepository.findByLocationContainingIgnoreCase might be too
        // broad.
        // If School entity had a dedicated 'country' field, it would be
        // SchoolRepository.findByCountryIgnoreCase(country, pageable)
        Page<School> schoolPage = schoolRepository.findByLocationContainingIgnoreCase(country, pageable);
        return schoolPage.map(this::convertToSchoolDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SchoolDTO> getSchoolsByProgram(String programName, Pageable pageable) {
        System.out.println("正在查找专业: " + programName);
        // 通过学校专业表查找学校，然后分页返回
        Page<School> schools = schoolRepository.findBySchoolProgramName(programName, pageable);
        System.out.println("找到学校数量: " + schools.getTotalElements());
        return schools.map(this::convertToSchoolDTO);
    }
}
package com.admitgenius.service;

import com.admitgenius.config.OpenAIConfig;
import com.admitgenius.dto.EssayDTO;
import com.admitgenius.dto.EssayGenerationDTO;
import com.admitgenius.model.*;
import com.admitgenius.repository.EssayRepository;
import com.admitgenius.repository.SchoolRepository;
import com.admitgenius.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class EssayGenerationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EssayRepository essayRepository;

    @Autowired
    private SchoolRepository schoolRepository;

    @Autowired
    private OpenAIConfig openaiConfig;

    private String openaiApiKey;
    private String openaiApiUrl;
    private String openaiModel = "moonshot-v1-32k"; // Kimi API 模型

    /**
     * 根据用户提交的信息自动生成文书
     */
    public EssayDTO generateEssay(EssayGenerationDTO generationDTO) {
        User user = userRepository.findById(generationDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        String schoolNameForPrompt = null;
        if (generationDTO.getSchoolId() != null) {
            Optional<School> schoolOptional = schoolRepository.findById(generationDTO.getSchoolId());
            if (schoolOptional.isPresent()) {
                schoolNameForPrompt = schoolOptional.get().getName();
            } else {
                System.err.println("Warning: School ID " + generationDTO.getSchoolId()
                        + " provided for essay generation but not found in database.");
            }
        }

        String prompt = buildPrompt(generationDTO, schoolNameForPrompt);

        String generatedContent = callOpenAIApi(prompt);

        Essay essay = new Essay();
        essay.setUser(user);
        essay.setTitle(generationDTO.getEssayTitle());
        essay.setContent(generatedContent);
        essay.setEssayType(Essay.EssayType.valueOf(generationDTO.getEssayType()));
        essay.setCreatedAt(LocalDateTime.now());
        essay.setUpdatedAt(LocalDateTime.now());
        essay.setGeneratedBy(Essay.GenerationSource.AI_MODEL);

        essay.setSchoolId(generationDTO.getSchoolId());
        essay.setWordLimit(generationDTO.getMaxWordCount());
        essay.setPrompt(generationDTO.getEssayPrompt());

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
        dto.setSchoolId(essay.getSchoolId());
        dto.setWordLimit(essay.getWordLimit());
        dto.setPrompt(essay.getPrompt());
        return dto;
    }

    /**
     * 构建AI提示语
     */
    private String buildPrompt(EssayGenerationDTO dto, String schoolName) {
        StringBuilder prompt = new StringBuilder();

        prompt.append(
                "You are a professional study abroad application essay consultant, adept at crafting personalized and persuasive application essays for applicants.");
        prompt.append(
                "Please write an application essay for the applicant targeting a specific school and program based on the following information.\n\n");

        prompt.append("## Applicant Information\n");
        if (schoolName != null && !schoolName.isEmpty()) {
            addField(prompt, "Target School", schoolName);
        } else if (dto.getSchoolId() != null) {
            // If ID was provided but name not found, could mention ID or be silent
            // addField(prompt, "Target School ID", dto.getSchoolId());
        }
        addField(prompt, "Target Program", dto.getProgramName());
        addField(prompt, "Essay Type", dto.getEssayType());
        addField(prompt, "Essay Title", dto.getEssayTitle());
        addField(prompt, "Essay Prompt/Instructions", dto.getEssayPrompt());
        if (dto.getMinWordCount() != null || dto.getMaxWordCount() != null) {
            String wordCountReq = (dto.getMinWordCount() != null ? dto.getMinWordCount() : "N/A") + " - " +
                    (dto.getMaxWordCount() != null ? dto.getMaxWordCount() : "N/A") + " words";
            addField(prompt, "Word Count Requirement", wordCountReq);
        }

        prompt.append("\n## Applicant Background\n");
        addField(prompt, "Major", dto.getMajor());
        addField(prompt, "Degree", dto.getDegree());
        addField(prompt, "Current/Graduated School", dto.getCurrentSchool());
        addField(prompt, "GPA", formatGPA(dto.getGpa(), dto.getGradeScale()));
        prompt.append("\n");

        prompt.append("## Detailed Background Information (Example - reconstruct fully)\n");
        if (dto.getGpa() != null)
            prompt.append("GPA: ").append(dto.getGpa()).append("\n");
        // ... many more fields from DTO ...

        prompt.append("\nBased on all the provided information, please generate a compelling essay.");
        prompt.append("The essay should be well-structured, articulate, and directly address the prompt if provided.");
        prompt.append("Ensure the tone is appropriate for the application.");
        prompt.append("Output only the generated essay content, without any additional explanations or headers.");

        return prompt.toString();
    }

    // ======================
    // 辅助方法集
    // ======================
    private void addField(StringBuilder prompt, String label, Object value) {
        if (value != null && !value.toString().trim().isEmpty()) {
            prompt.append(label).append(": ").append(value.toString()).append("\n");
        }
    }

    private String formatGPA(Double gpa, String scale) {
        if (gpa == null || scale == null)
            return "未提供";
        return String.format("%.2f/%s（专业前%d%%）", gpa, scale, calculatePercentage(gpa, scale));
    }

    private int calculatePercentage(Double gpa, String scale) {
        double max = Double.parseDouble(scale);
        return (int) ((gpa / max) * 100);
    }

    /**
     * 调用OpenAI API
     */
    private String callOpenAIApi(String prompt) {
        try {
            openaiApiKey = openaiConfig.getApi().getKey();
            openaiApiUrl = openaiConfig.getApi().getProxyUrl() + "/chat/completions";

            if (openaiApiKey == null || openaiApiKey.trim().isEmpty()) {
                throw new RuntimeException("OpenAI API密钥未配置");
            }

            System.out.println(
                    "调用OpenAI的API密钥: " + (openaiApiKey.length() > 10 ? openaiApiKey.substring(0, 10) + "..." : "密钥过短"));
            System.out.println("调用OpenAI的URL: " + openaiApiUrl);

            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openaiApiKey);

            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", openaiModel);
            requestBody.put("messages", new Object[] { message });
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 4000);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(openaiApiUrl, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                if (responseBody != null && responseBody.containsKey("choices")) {
                    List<Object> choicesList = (List<Object>) responseBody.get("choices");
                    if (choicesList != null && !choicesList.isEmpty()) {
                        Map<String, Object> choice = (Map<String, Object>) choicesList.get(0);
                        Map<String, Object> messageResponse = (Map<String, Object>) choice.get("message");
                        return (String) messageResponse.get("content");
                    }
                }
                throw new RuntimeException("OpenAI API响应格式异常");
            }

            throw new RuntimeException("调用OpenAI API失败: " + response.getStatusCode());

        } catch (Exception e) {
            System.err.println("调用OpenAI API出错: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("调用OpenAI API出错: " + e.getMessage(), e);
        }
    }

    /**
     * 计算文本的词数
     */
    private Integer countWords(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        String[] words = text.split("\\s+");
        return words.length;
    }

}
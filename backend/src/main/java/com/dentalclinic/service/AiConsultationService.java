package com.dentalclinic.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dentalclinic.common.BusinessException;
import com.dentalclinic.config.AiProperties;
import com.dentalclinic.entity.AiConfig;
import com.dentalclinic.entity.DoctorProfile;
import com.dentalclinic.entity.Medicine;
import com.dentalclinic.entity.SymptomRule;
import com.dentalclinic.mapper.AiConfigMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AiConsultationService {
    private final AiProperties properties;
    private final AiConfigMapper aiConfigMapper;
    private final ObjectMapper objectMapper;

    public AiConsultationService(AiProperties properties, AiConfigMapper aiConfigMapper, ObjectMapper objectMapper) {
        this.properties = properties;
        this.aiConfigMapper = aiConfigMapper;
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> consult(String symptoms, List<SymptomRule> rules,
                                       List<DoctorProfile> doctors, List<Medicine> medicines) {
        AiConfig config = activeConfig();
        validateConfig(config);
        try {
            Map<String, Object> requestBody = Map.of(
                "model", config.model,
                "temperature", config.temperature == null ? 0.2 : config.temperature,
                "max_tokens", config.maxTokens == null ? 900 : config.maxTokens,
                "messages", List.of(
                    Map.of("role", "system", "content", systemPrompt()),
                    Map.of("role", "user", "content", userPrompt(symptoms, rules, doctors, medicines))
                )
            );

            HttpRequest request = HttpRequest.newBuilder()
                .uri(chatCompletionsUri(config))
                .timeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
                .header("Authorization", "Bearer " + config.apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                .build();

            HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new BusinessException("AI API call failed: " + response.statusCode() + " " + shorten(response.body()));
            }

            JsonNode root = objectMapper.readTree(response.body());
            String content = root.path("choices").path(0).path("message").path("content").asText("");
            if (!StringUtils.hasText(content)) {
                throw new BusinessException("AI API response does not contain choices[0].message.content");
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("provider", config.providerName);
            result.put("baseUrl", config.baseUrl);
            result.put("model", config.model);
            result.put("answer", content);
            result.put("disclaimer", "AI suggestions are preliminary reference only and cannot replace a doctor's diagnosis.");
            result.put("usage", objectMapper.convertValue(root.path("usage"), Map.class));
            return result;
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException("AI API call error: " + exception.getMessage());
        }
    }

    public AiConfig activeConfig() {
        AiConfig config = aiConfigMapper.selectOne(new QueryWrapper<AiConfig>()
            .eq("enabled", true)
            .orderByDesc("updated_at")
            .last("limit 1"));
        if (config == null) {
            throw new BusinessException("Please enable one AI config row in ai_config first.");
        }
        return config;
    }

    private void validateConfig(AiConfig config) {
        if (!StringUtils.hasText(config.baseUrl)) {
            throw new BusinessException("Please configure ai_config.base_url first.");
        }
        if (!StringUtils.hasText(config.apiKey)) {
            throw new BusinessException("Please configure ai_config.api_key first.");
        }
        if (!StringUtils.hasText(config.model)) {
            throw new BusinessException("Please configure ai_config.model first.");
        }
    }

    private URI chatCompletionsUri(AiConfig config) {
        String baseUrl = config.baseUrl.trim();
        while (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        if (baseUrl.endsWith("/chat/completions")) {
            return URI.create(baseUrl);
        }
        return URI.create(baseUrl + "/chat/completions");
    }

    private String systemPrompt() {
        return """
            You are an AI dental triage assistant in a dental clinic management system.
            Answer in Simplified Chinese.
            Provide only preliminary oral-health triage, possible causes, common care suggestions, recommended department, and visit advice.
            Do not make a confirmed diagnosis, do not replace a licensed doctor, and do not promise treatment outcomes.
            If symptoms include severe pain, facial swelling, fever, persistent bleeding, trauma, swallowing difficulty, or breathing difficulty, advise urgent offline care.
            Use this format:
            1. Initial assessment
            2. Possible causes
            3. Recommended department or doctor direction
            4. Reference medicine or care suggestions
            5. When to seek care quickly
            6. Disclaimer
            """;
    }

    private String userPrompt(String symptoms, List<SymptomRule> rules,
                              List<DoctorProfile> doctors, List<Medicine> medicines) {
        return """
            Patient symptoms:
            %s

            Local symptom-rule references:
            %s

            Available doctors:
            %s

            Available medicines:
            %s

            Generate a preliminary dental consultation answer based on the patient symptoms and references.
            Medicines are only care or consultation references. Remind the patient to follow doctor's advice.
            """.formatted(
            blankToDefault(symptoms, "No specific symptoms provided."),
            toJson(rules),
            toJson(doctors),
            toJson(medicines)
        );
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception exception) {
            return String.valueOf(value);
        }
    }

    private String blankToDefault(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }

    private String shorten(String body) {
        if (body == null) {
            return "";
        }
        return body.length() <= 600 ? body : body.substring(0, 600) + "...";
    }
}

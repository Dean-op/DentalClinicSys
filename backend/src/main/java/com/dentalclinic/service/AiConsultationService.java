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
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
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
                            Map.of("role", "user", "content", userPrompt(symptoms, rules, doctors, medicines))));

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
                throw new BusinessException(
                        "AI API call failed: " + response.statusCode() + " " + shorten(response.body()));
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
            result.put("sections", extractSections(content));
            // result.put("disclaimer", "AI建议仅供就诊前参考，不能替代医生诊断。");
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
                Answer in Simplified Chinese only.
                Do not make a confirmed diagnosis, do not replace a licensed doctor, and do not promise treatment outcomes.
                If symptoms include severe pain, facial swelling, fever, persistent bleeding, trauma, swallowing difficulty, or breathing difficulty, advise urgent offline care.
                Use Markdown and strictly follow this structure:
                1. 初步评估
                2. 可能原因
                3. 建议就诊科室或医生方向
                4. 参考用药或护理建议
                5. 何时尽快就医
                6. 免责声明
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
                toJson(medicines));
    }

    private List<Map<String, String>> extractSections(String content) {
        if (!StringUtils.hasText(content)) {
            return List.of();
        }
        List<SectionDefinition> definitions = List.of(
                new SectionDefinition("初步评估", Set.of("初步评估", "Initial assessment")),
                new SectionDefinition("可能原因", Set.of("可能原因", "Possible causes")),
                new SectionDefinition("建议就诊科室或医生方向",
                        Set.of("建议就诊科室或医生方向", "建议就诊科室", "推荐科室", "Recommended department or doctor direction")),
                new SectionDefinition("参考用药或护理建议",
                        Set.of("参考用药或护理建议", "参考用药建议", "护理建议", "Reference medicine or care suggestions")),
                new SectionDefinition("何时尽快就医", Set.of("何时尽快就医", "When to seek care quickly")),
                new SectionDefinition("免责声明", Set.of("免责声明", "Disclaimer")));

        LinkedHashMap<String, StringBuilder> bucket = new LinkedHashMap<>();
        String currentTitle = null;
        for (String line : content.split("\\R")) {
            String trimmed = line.trim();
            if (!StringUtils.hasText(trimmed)) {
                if (currentTitle != null) {
                    bucket.get(currentTitle).append('\n');
                }
                continue;
            }

            SectionHit hit = detectSection(trimmed, definitions);
            if (hit != null) {
                currentTitle = hit.title();
                bucket.putIfAbsent(currentTitle, new StringBuilder());
                if (StringUtils.hasText(hit.remainder())) {
                    bucket.get(currentTitle).append(hit.remainder().trim());
                }
                continue;
            }

            if (currentTitle == null) {
                currentTitle = "AI原文";
                bucket.putIfAbsent(currentTitle, new StringBuilder());
            }
            if (bucket.get(currentTitle).length() > 0) {
                bucket.get(currentTitle).append('\n');
            }
            bucket.get(currentTitle).append(line);
        }

        List<Map<String, String>> sections = new ArrayList<>();
        for (Map.Entry<String, StringBuilder> entry : bucket.entrySet()) {
            Map<String, String> section = new LinkedHashMap<>();
            section.put("title", entry.getKey());
            section.put("content", entry.getValue().toString().trim());
            sections.add(section);
        }
        return sections;
    }

    private SectionHit detectSection(String line, List<SectionDefinition> definitions) {
        String normalized = line.replaceFirst("^#{1,6}\\s*", "").replaceFirst("^\\d+[.、]\\s*", "");
        for (SectionDefinition definition : definitions) {
            for (String alias : definition.aliases()) {
                Pattern pattern = Pattern.compile("^" + Pattern.quote(alias) + "(?:\\s*[：:\\-—]\\s*(.*))?$");
                var matcher = pattern.matcher(normalized);
                if (matcher.matches()) {
                    return new SectionHit(definition.title(), matcher.group(1) == null ? "" : matcher.group(1));
                }
            }
        }
        return null;
    }

    private record SectionDefinition(String title, Set<String> aliases) {
    }

    private record SectionHit(String title, String remainder) {
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

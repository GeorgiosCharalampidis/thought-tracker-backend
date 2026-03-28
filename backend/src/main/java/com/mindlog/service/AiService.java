package com.mindlog.service;

import com.mindlog.config.AiServiceConfig;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import org.springframework.web.client.RestTemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mindlog.dto.ChatMessage;
import com.mindlog.model.Category;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AiService {
    private static final Logger logger = LoggerFactory.getLogger(AiService.class);
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AiServiceConfig config;

    public AiService(AiServiceConfig config) {
        this.config = config;
        this.restTemplate = createRestTemplate();
    }

     private RestTemplate createRestTemplate() {
         SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
         factory.setConnectTimeout(config.getTimeout());
         factory.setReadTimeout(config.getTimeout());

         RestTemplate restTemplate = new RestTemplate(factory);

         // Configure message converters to handle UTF-8 properly
         restTemplate.getMessageConverters().forEach(converter -> {
             if (converter instanceof org.springframework.http.converter.StringHttpMessageConverter) {
                 ((org.springframework.http.converter.StringHttpMessageConverter) converter).setDefaultCharset(java.nio.charset.StandardCharsets.UTF_8);
             }
         });

         return restTemplate;
     }

    public String getNotesFromModel(String inputPrompt, String modelName) {

        String systemPrompt = "You are an AI that reads and responds to the user's daily notes. " +
                "Be direct, honest, and human. Don't use therapeutic language or talk down to them. " +
                "If something sounds concerning, say so directly. If they're being hard on themselves, call it out. " +
                "If they're making progress, acknowledge it without being overly positive. " +
                "Ask real questions that show you're actually thinking about what they wrote. " +
                "Keep it conversational and avoid corporate or self-help speak. " +
                "Don't be afraid to disagree or push back if something doesn't make sense. " +
                "Be a real conversation partner, not a therapist. " +
                "Use only standard ASCII characters - avoid smart quotes, em dashes, or other special Unicode characters.\n";

        String url = config.getUrl() + "/api/chat";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept-Charset", "UTF-8");

        Map<String, Object> payload = Map.of(
                "model", modelName,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", inputPrompt)
                )
        );

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);

        try {
            logger.info("Attempting to connect to AI service at: {}", url);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                try {
                    StringBuilder fullResponse = new StringBuilder();
                    String responseBody = response.getBody();

                    for (String jsonChunk : responseBody.split("\n")) {
                        if (jsonChunk.trim().isEmpty()) continue;
                        JsonNode jsonNode = objectMapper.readTree(jsonChunk);
                        if (jsonNode.has("message") && jsonNode.get("message").has("content")) {
                            String content = jsonNode.get("message").get("content").asText();
                            fullResponse.append(content);
                        }
                    }
                    return fullResponse.toString();
                } catch (Exception e) {
                    logger.error("Failed to parse response from model: {}", e.getMessage());
                    throw new RuntimeException("Failed to parse response from model", e);
                }
            } else {
                logger.error("Failed to get response from model: {}", response.getStatusCode());
                throw new RuntimeException("Failed to get response from model: " + response.getStatusCode());
            }
        } catch (Exception e) {
            logger.error("Error communicating with AI service: {}", e.getMessage());
            throw new RuntimeException("Error communicating with AI service", e);
        }
    }

    public String chat(String notesContext, List<ChatMessage> conversationHistory) {
        String systemPrompt = "You are a personal AI companion who knows this person well. " +
                "You know them through the thoughts they've shared over time:\n\n" + notesContext + "\n\n" +
                "This is your private background — never list it, never reference dates, never quote it back at them. " +
                "Just let it shape how you understand them, the way a real friend would. " +
                "Be direct, honest, and human. Don't use therapeutic language or talk down to them. " +
                "If something sounds concerning, say so directly. If they're being hard on themselves, call it out. " +
                "Ask real questions that show you're actually thinking about who they are. " +
                "Keep it conversational. Don't be a therapist — be a real conversation partner. " +
                "IMPORTANT: Never echo, repeat, or quote back what the user just said. " +
                "Respond to the meaning, not the words. " +
                "Use only standard ASCII characters - avoid smart quotes, em dashes, or other special Unicode characters.";

        String url = config.getUrl() + "/api/chat";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept-Charset", "UTF-8");

        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        for (ChatMessage msg : conversationHistory) {
            messages.add(Map.of("role", msg.getRole(), "content", msg.getContent()));
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", config.getModel());
        payload.put("messages", messages);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);

        try {
            logger.info("Sending chat request to AI service at: {}", url);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                StringBuilder fullResponse = new StringBuilder();
                String responseBody = response.getBody();

                for (String jsonChunk : responseBody.split("\n")) {
                    if (jsonChunk.trim().isEmpty()) continue;
                    JsonNode jsonNode = objectMapper.readTree(jsonChunk);
                    if (jsonNode.has("message") && jsonNode.get("message").has("content")) {
                        fullResponse.append(jsonNode.get("message").get("content").asText());
                    }
                }
                return fullResponse.toString();
            } else {
                throw new RuntimeException("Failed to get chat response: " + response.getStatusCode());
            }
        } catch (Exception e) {
            logger.error("Error during chat with AI service: {}", e.getMessage());
            throw new RuntimeException("Error communicating with AI service", e);
        }
    }

    public record ClassificationResult(boolean valid, String category) {}

    /**
     * Single LLM call that both validates the entry and classifies it into a category.
     * Returns null if the call fails (callers should fall back to separate validation + embedding categorization).
     */
    public ClassificationResult validateAndCategorize(String noteContent) {
        String categoryList = Category.allLabels().stream()
                .collect(Collectors.joining("\n- ", "- ", ""));

        String systemPrompt = "You are a classifier for a journaling app.\n" +
                "Respond with ONLY this JSON (no markdown, no explanation):\n" +
                "{\"valid\": true, \"category\": \"...\"}\n\n" +
                "valid: true for any genuine personal expression; false only for gibberish or meaningless text.\n" +
                "category: the category that best captures what the person is truly experiencing — read the full entry and look past surface words to the emotional or thematic core.\n\n" +
                "Categories:\n" + categoryList;

        String url = config.getUrl() + "/api/chat";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> payload = Map.of(
                "model", config.getClassificationModel(),
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", noteContent)
                )
        );

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) return null;

            StringBuilder fullResponse = new StringBuilder();
            for (String jsonChunk : response.getBody().split("\n")) {
                if (jsonChunk.trim().isEmpty()) continue;
                JsonNode jsonNode = objectMapper.readTree(jsonChunk);
                if (jsonNode.has("message") && jsonNode.get("message").has("content")) {
                    fullResponse.append(jsonNode.get("message").get("content").asText());
                }
            }

            String raw = fullResponse.toString().trim();
            // Strip markdown code fences if the model wrapped its response
            if (raw.startsWith("```")) {
                raw = raw.replaceAll("^```[a-z]*\\n?", "").replaceAll("```$", "").trim();
            }

            JsonNode result = objectMapper.readTree(raw);
            boolean valid = result.path("valid").asBoolean(true);
            String categoryRaw = result.path("category").asText("").trim();

            // Match to a known category (exact then loose)
            String matchedCategory = null;
            for (Category c : Category.values()) {
                if (c.getDisplayName().equalsIgnoreCase(categoryRaw)) {
                    matchedCategory = c.getDisplayName();
                    break;
                }
            }
            if (matchedCategory == null) {
                for (Category c : Category.values()) {
                    if (categoryRaw.toLowerCase().contains(c.getDisplayName().toLowerCase())) {
                        matchedCategory = c.getDisplayName();
                        break;
                    }
                }
            }

            return new ClassificationResult(valid, matchedCategory);
        } catch (Exception e) {
            logger.warn("validateAndCategorize failed: {}", e.getMessage());
            return null;
        }
    }

}

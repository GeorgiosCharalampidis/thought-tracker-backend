package com.mindlog.service;

import com.mindlog.config.AiServiceConfig;
import com.mindlog.util.TextValidator;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import org.springframework.web.client.RestTemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mindlog.dto.ChatMessage;
import java.util.*;

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

//    private RestTemplate createRestTemplate() {
//        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
//        factory.setConnectTimeout(config.getTimeout());
//        factory.setReadTimeout(config.getTimeout());
//        return new RestTemplate(factory);
//
//    }

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
        String systemPrompt = "You are a personal AI companion with access to this user's private journal. " +
                "Here are their journal entries:\n\n" + notesContext + "\n\n" +
                "Be direct, honest, and human. Don't use therapeutic language or talk down to them. " +
                "If something sounds concerning, say so directly. If they're being hard on themselves, call it out. " +
                "Ask real questions that show you're actually thinking about what they wrote. " +
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

    public boolean isValidThought(String thoughtText) {
        // Quick check - if AI service is not available, fall back immediately
        try {
            // Test if the service is reachable with a quick ping
            restTemplate.getForEntity(config.getUrl() + "/api/tags", String.class);
        } catch (Exception e) {
            logger.warn("AI service not available, falling back to basic validation: {}", e.getMessage());
            return TextValidator.isMeaningfulThought(thoughtText);
        }

        String systemPrompt = "You are a text validator for a personal reflection app. " +
                "Respond with ONLY 'VALID' or 'INVALID' — nothing else. " +

                "VALID: Any coherent statement, phrase, or sentence that conveys a personal experience, activity, event, thought, reflection, decision, concern, uncertainty, dilemma, judgment, self-description, feeling, goal, aspiration, desire, or wish. " +
                "Even short or simple expressions (e.g., 'tired', 'feeling bad', 'want to be rich', 'need a vacation') are VALID if they clearly express a state, thought, or desire. " +

                "INVALID: Pure greetings with no personal content, casual/social questions, test messages, gibberish, single words with no meaning (e.g., 'asdfgh'), incomplete fragments that cut off mid-thought, and dismissive responses like 'whatever'. " +

                "Examples — VALID: 'I am such a bad person', 'tired', 'feeling stressed', 'argued with my boss', 'I don’t know how to ask my boss for more flexibility', 'thinking about quitting my job'. " +
                "Examples — INVALID: 'hello', 'hey what's up', 'how are you', 'test', 'whatever', 'asdfgh', 'today I did'. " +

                "Rule of thumb: Be very permissive - approve any genuine personal expression including goals, desires, and aspirations. Only reject obvious non-personal content.";


        String url = config.getUrl() + "/api/chat";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept-Charset", "UTF-8");

        Map<String, Object> payload = Map.of(
                "model", config.getModel(),
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", thoughtText)
                )
        );

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);

        try {
            logger.info("Validating thought with AI service at URL: {}", url);
            logger.debug("Request payload: {}", payload);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            
            logger.info("AI validation response status: {}", response.getStatusCode());

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
                    
                    String aiResponse = fullResponse.toString().trim().toUpperCase();
                    return aiResponse.contains("VALID") && !aiResponse.contains("INVALID");
                } catch (Exception e) {
                    logger.error("Failed to parse validation response: {}", e.getMessage());
                    // Fallback to basic validation if AI fails
                    return TextValidator.isMeaningfulThought(thoughtText);
                }
            } else {
                logger.error("Failed to get validation response: {}", response.getStatusCode());
                // Fallback to basic validation if AI fails
                return TextValidator.isMeaningfulThought(thoughtText);
            }
        } catch (Exception e) {
            logger.error("Error during AI validation, falling back to basic validation: {}", e.getMessage());
            // Fallback to basic validation if AI service is unavailable
            return TextValidator.isMeaningfulThought(thoughtText);
        }
    }

}

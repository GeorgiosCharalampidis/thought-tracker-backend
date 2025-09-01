package com.mindlog.service;

import com.mindlog.config.AiServiceConfig;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            logger.info("Attempting to connect to Ollama service at: {}", url);
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
        } catch (ResourceAccessException e) {
            logger.error("Cannot connect to Ollama service: {}", e.getMessage());
            throw new IllegalStateException("AI service is not available. Please ensure Ollama is running and accessible.");
        } catch (HttpClientErrorException.NotFound e) {
            logger.error("Model not found: {}", e.getMessage());
            throw new IllegalStateException("AI model '" + modelName + "' not found. Please ensure the model is installed in Ollama.");
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            logger.error("HTTP error from Ollama service: {} - {}", e.getStatusCode(), e.getMessage());
            throw new IllegalStateException("AI service error: " + e.getStatusCode() + " - " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error while calling AI service: {}", e.getMessage());
            throw new IllegalStateException("Unexpected error while calling AI service: " + e.getMessage());
        }
    }

}

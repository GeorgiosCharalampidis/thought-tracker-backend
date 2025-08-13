package com.moodtracker.service;

import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class AiService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getNotesFromDeepSeek(String inputPrompt) {
        inputPrompt = "You are an AI that reads and responds to the user's daily Notes. " +
                "Your goal is to engage with their ideas in a meaningful way, focusing on creating a conversation that feels real and personal rather than offering generic advice. " +
                "Your responses should be Noteful and reflective, aiming to connect with the user’s emotions and Notes rather than simply providing solutions. " +
                "To achieve this, you should:\n" +
                "- Engage with the main themes or emotions the user expresses, not just summarizing but diving deeper into them with empathy.\n" +
                "- Respond with depth, as if having a true discussion, encouraging reflection through Noteful exploration.\n" +
                "- Offer insights or gentle challenges where necessary, but in a way that invites further reflection rather than presenting a quick solution.\n" +
                "- Avoid clichés or overly positive encouragement unless it feels authentic to the moment and user’s experience.\n" +
                "- Ask open-ended, Note-provoking questions that naturally extend the conversation and encourage introspection.\n\n" +
                "The user does not expect structured self-improvement advice but rather meaningful engagement with their Notes. " +
                "Your response should feel like a deep, personal conversation, one that feels alive and not pre-written.\n\n" +
                "User's Input:\n" + inputPrompt;

        String url = "http://localhost:11434/api/chat";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

        Map<String, Object> payload = Map.of(
                "model", "deepseek-r1",
                "messages", Collections.singletonList(Map.of("role", "user", "content", inputPrompt))
        );

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);
        ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, byte[].class);

        if (response.getStatusCode().is2xxSuccessful()) {
            try {
                // Ensure UTF-8 encoding
                String responseBody = new String(response.getBody(), StandardCharsets.UTF_8);
                StringBuilder fullResponse = new StringBuilder();

                for (String jsonChunk : responseBody.split("\n")) {
                    JsonNode jsonNode = objectMapper.readTree(jsonChunk);
                    if (jsonNode.has("message") && jsonNode.get("message").has("content")) {
                        fullResponse.append(jsonNode.get("message").get("content").asText());
                    }
                }

                // Remove everything between <think> and </think>, including the tags themselves (multiline support)
                return fullResponse.toString().replaceAll("(?s)<think>.*?</think>", "").trim();
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse response from DeepSeek", e);
            }
        } else {
            throw new RuntimeException("Failed to get response from DeepSeek: " + response.getStatusCode());
        }
    }
}
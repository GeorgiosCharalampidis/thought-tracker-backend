package com.rumino.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class EmbeddingService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String DEFAULT_MODEL = "mxbai-embed-large";

    public List<float[]> embedAll(List<String> inputs) {
        if (inputs == null || inputs.isEmpty()) {
            return Collections.emptyList();
        }
        String url = "http://localhost:11434/api/embeddings";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

        List<float[]> result = new ArrayList<>();
        for (String text : inputs) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("model", DEFAULT_MODEL);
            payload.put("prompt", text);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Embedding request failed: " + response.getStatusCode());
            }

            try {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode embNode = root.get("embedding");
                if (embNode == null || !embNode.isArray()) {
                    throw new RuntimeException("Unexpected embedding response: " + response.getBody());
                }
                float[] vec = new float[embNode.size()];
                int i = 0;
                for (JsonNode v : embNode) {
                    vec[i++] = (float) v.asDouble();
                }
                result.add(vec);
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse embeddings", e);
            }
        }
        return result;
    }
}



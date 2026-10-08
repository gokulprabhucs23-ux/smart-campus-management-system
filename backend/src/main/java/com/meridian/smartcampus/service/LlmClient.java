package com.meridian.smartcampus.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

@Service
public class LlmClient {

    private static final Logger log = LoggerFactory.getLogger(LlmClient.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient;

    private String apiKey;
    private String provider = "gemini"; // "gemini", "openai", or "grounded_campus_engine"
    private String model = "gemini-1.5-flash"; // "gemini-1.5-flash", "gemini-2.0-flash", "gpt-4o-mini"
    private String customEndpoint;

    public LlmClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();

        // Check environment variables
        String envGeminiKey = System.getenv("GEMINI_API_KEY");
        String envOpenAiKey = System.getenv("OPENAI_API_KEY");

        if (envGeminiKey != null && !envGeminiKey.trim().isEmpty()) {
            this.apiKey = envGeminiKey.trim();
            this.provider = "gemini";
            this.model = "gemini-1.5-flash";
            log.info("LlmClient initialized with GEMINI_API_KEY from environment.");
        } else if (envOpenAiKey != null && !envOpenAiKey.trim().isEmpty()) {
            this.apiKey = envOpenAiKey.trim();
            this.provider = "openai";
            this.model = "gpt-4o-mini";
            log.info("LlmClient initialized with OPENAI_API_KEY from environment.");
        } else {
            log.info("No external LLM API key detected in environment. Using Campus Grounded Engine by default.");
        }
    }

    public synchronized boolean isConfigured() {
        return apiKey != null && !apiKey.trim().isEmpty() && !"grounded_campus_engine".equalsIgnoreCase(provider);
    }

    public synchronized String getProvider() {
        return provider;
    }

    public synchronized void setProvider(String provider) {
        this.provider = provider;
    }

    public synchronized String getModel() {
        return model;
    }

    public synchronized void setModel(String model) {
        this.model = model;
    }

    public synchronized String getApiKey() {
        return apiKey;
    }

    public synchronized void setApiKey(String apiKey) {
        this.apiKey = apiKey != null ? apiKey.trim() : null;
    }

    public synchronized String getCustomEndpoint() {
        return customEndpoint;
    }

    public synchronized void setCustomEndpoint(String customEndpoint) {
        this.customEndpoint = customEndpoint;
    }

    public synchronized String getMaskedApiKey() {
        if (apiKey == null || apiKey.length() < 8) return "";
        return apiKey.substring(0, 4) + "..." + apiKey.substring(apiKey.length() - 4);
    }

    /**
     * Test connection to configured LLM
     */
    public boolean testConnection() {
        if (!isConfigured()) return false;
        try {
            String testPrompt = "Respond with one word: 'CONNECTED'";
            String result = generate("You are a test assistant.", Collections.emptyList(), testPrompt);
            return result != null && !result.trim().isEmpty();
        } catch (Exception e) {
            log.warn("LLM connection test failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Generate completion using configured LLM provider
     */
    public String generate(String systemPrompt, List<Map<String, String>> history, String userMessage) throws Exception {
        if (!isConfigured()) {
            throw new IllegalStateException("LLM client is not configured with an API key.");
        }

        if ("gemini".equalsIgnoreCase(provider)) {
            return generateGemini(systemPrompt, history, userMessage);
        } else if ("openai".equalsIgnoreCase(provider)) {
            return generateOpenAi(systemPrompt, history, userMessage);
        } else {
            throw new IllegalArgumentException("Unsupported LLM provider: " + provider);
        }
    }

    private String generateGemini(String systemPrompt, List<Map<String, String>> history, String userMessage) throws Exception {
        String effectiveModel = this.model != null && !this.model.isEmpty() ? this.model : "gemini-1.5-flash";
        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/" + effectiveModel + ":generateContent?key=" + apiKey;

        Map<String, Object> payload = new LinkedHashMap<>();

        // 1. System instruction
        if (systemPrompt != null && !systemPrompt.trim().isEmpty()) {
            Map<String, Object> systemInstruction = new HashMap<>();
            systemInstruction.put("parts", List.of(Map.of("text", systemPrompt)));
            payload.put("system_instruction", systemInstruction);
        }

        // 2. Multi-turn conversation contents
        List<Map<String, Object>> contents = new ArrayList<>();
        if (history != null) {
            for (Map<String, String> turn : history) {
                String from = turn.get("from");
                String text = turn.get("text");
                if (text == null || text.trim().isEmpty()) continue;

                String role = ("you".equalsIgnoreCase(from) || "user".equalsIgnoreCase(from)) ? "user" : "model";
                contents.add(Map.of("role", role, "parts", List.of(Map.of("text", text))));
            }
        }
        // Current user message
        contents.add(Map.of("role", "user", "parts", List.of(Map.of("text", userMessage))));
        payload.put("contents", contents);

        // 3. Generation configuration
        Map<String, Object> genConfig = new HashMap<>();
        genConfig.put("temperature", 0.4);
        genConfig.put("maxOutputTokens", 2048);
        payload.put("generationConfig", genConfig);

        String jsonBody = objectMapper.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            log.error("Gemini API call failed with HTTP {}: {}", response.statusCode(), response.body());
            throw new RuntimeException("Gemini API Error (" + response.statusCode() + "): " + response.body());
        }

        JsonNode root = objectMapper.readTree(response.body());
        JsonNode candidates = root.path("candidates");
        if (candidates.isArray() && candidates.size() > 0) {
            JsonNode textNode = candidates.get(0).path("content").path("parts").get(0).path("text");
            if (!textNode.isMissingNode()) {
                return textNode.asText();
            }
        }

        throw new RuntimeException("No text candidates found in Gemini response: " + response.body());
    }

    private String generateOpenAi(String systemPrompt, List<Map<String, String>> history, String userMessage) throws Exception {
        String effectiveModel = this.model != null && !this.model.isEmpty() ? this.model : "gpt-4o-mini";
        String endpoint = customEndpoint != null && !customEndpoint.isEmpty()
                ? customEndpoint
                : "https://api.openai.com/v1/chat/completions";

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", effectiveModel);

        List<Map<String, String>> messages = new ArrayList<>();
        if (systemPrompt != null && !systemPrompt.trim().isEmpty()) {
            messages.add(Map.of("role", "system", "content", systemPrompt));
        }

        if (history != null) {
            for (Map<String, String> turn : history) {
                String from = turn.get("from");
                String text = turn.get("text");
                if (text == null || text.trim().isEmpty()) continue;

                String role = ("you".equalsIgnoreCase(from) || "user".equalsIgnoreCase(from)) ? "user" : "assistant";
                messages.add(Map.of("role", role, "content", text));
            }
        }
        messages.add(Map.of("role", "user", "content", userMessage));
        payload.put("messages", messages);
        payload.put("temperature", 0.4);

        String jsonBody = objectMapper.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            log.error("OpenAI API call failed with HTTP {}: {}", response.statusCode(), response.body());
            throw new RuntimeException("OpenAI API Error (" + response.statusCode() + "): " + response.body());
        }

        JsonNode root = objectMapper.readTree(response.body());
        JsonNode choices = root.path("choices");
        if (choices.isArray() && choices.size() > 0) {
            JsonNode messageContent = choices.get(0).path("message").path("content");
            if (!messageContent.isMissingNode()) {
                return messageContent.asText();
            }
        }

        throw new RuntimeException("No choice content found in OpenAI response: " + response.body());
    }
}

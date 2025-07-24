package com.example.backend.GenAi;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
public class AiServiceClient implements AIServiceClient1{

    private final WebClient webClient;
    private final String apiKey;

    public AiServiceClient(
            WebClient.Builder webClientBuilder,
            @Value("${ai.service.url}") String serviceUrl,
            @Value("${ai.service.key}") String apiKey) {
        this.webClient = webClientBuilder
                .baseUrl(serviceUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
        this.apiKey = apiKey;
    }

    public Mono<String> getRecommendation(String cloudProvider, String issueType, String resourceDetails) {
        // Construct AI prompt
        String prompt = String.format(
            "As a cloud sustainability expert, provide a recommendation for a %s resource with issue '%s'. "
            + "Resource details: %s. "
            + "Response format: {\"action\":\"...\",\"explanation\":\"...\"}",
            cloudProvider, issueType, resourceDetails
        );

        // AI request payload
        Map<String, Object> requestBody = Map.of(
            "model", "gpt-4-turbo",
            "messages", List.of(Map.of("role", "user", "content", prompt)),
            "max_tokens", 200,
            "temperature", 0.2
        );

        return webClient.post()
                .uri("/chat/completions")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    // Parse AI response
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                });
    }
}
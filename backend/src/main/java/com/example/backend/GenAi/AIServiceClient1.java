package com.example.backend.GenAi;

import reactor.core.publisher.Mono;

public interface AIServiceClient1 {
     Mono<String> getRecommendation(String cloudType, String issueType, String resourceDescription);
}

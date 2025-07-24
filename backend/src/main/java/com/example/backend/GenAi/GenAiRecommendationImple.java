package com.example.backend.GenAi;

import com.example.backend.Model.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GenAiRecommendationImple implements GenAiRecommendationService {

    private final AIServiceClient1 aiClient;
    
    // Azure/GCP default recommendations
    private static final Map<String, String[]> AZURE_DEFAULT_RECOMMENDATIONS = Map.of(
        "idle", new String[]{"decommission", "This resource is mostly idle and can be safely decommissioned to save costs and emissions."},
        "carbon-heavy", new String[]{"optimize-or-migrate", 
                "This resource generates high carbon footprint. Consider migrating to greener Azure regions like Sweden Central, Norway East, or US West 3 (Arizona)."},
        "overprovisioned", new String[]{"right-size", 
                "Resource appears oversized for its current workload; right-sizing can reduce consumption and emissions."}
    );
    
    // AWS default recommendations
    private static final Map<String, String[]> AWS_DEFAULT_RECOMMENDATIONS = Map.of(
        "idle", new String[]{"decommission", "This AWS resource is idle and should be decommissioned."},
        "carbon-heavy", new String[]{"optimize-or-migrate", 
                "High carbon footprint AWS resource. Consider migrating to AWS Oregon (us-west-2) or Ireland (eu-west-1)."},
        "overprovisioned", new String[]{"right-size", 
                "AWS resource appears oversized; right-sizing can save costs."}
    );

    public GenAiRecommendationImple(AIServiceClient1 aiClient) {
        this.aiClient = aiClient;
    }

    @Override
    public List<Rcommendation> recommed(List<ResourceFinding> findings) {
        List<Rcommendation> rec = new ArrayList<>();
        List<Mono<Void>> asyncOperations = new ArrayList<>();
        
        for(ResourceFinding finding : findings) {
            Mono<Void> asyncOp = getAzureGcpRecommendation(finding)
                .doOnNext(recommendation -> {
                    synchronized (rec) {
                        rec.add(new Rcommendation(
                            finding,
                            recommendation[0],
                            recommendation[1]
                        ));
                    }
                })
                .then();
            
            asyncOperations.add(asyncOp);
        }
        
        Mono.when(asyncOperations).block();
        return rec;
    }

    @Override
    public List<Rcommendationaws> recommedaws(List<ResourceFindingaws> findings) {
        List<Rcommendationaws> rec = new ArrayList<>();
        List<Mono<Void>> asyncOperations = new ArrayList<>();
        
        for(ResourceFindingaws finding : findings) {
            Mono<Void> asyncOp = getAwsRecommendation(finding)
                .doOnNext(recommendation -> {
                    synchronized (rec) {
                        rec.add(new Rcommendationaws(
                            finding,
                            recommendation[0],
                            recommendation[1]
                        ));
                    }
                })
                .then();
            
            asyncOperations.add(asyncOp);
        }
        
        Mono.when(asyncOperations).block();
        return rec;
    }
    private String createResourceDescription(Object finding) {
    if (finding instanceof ResourceFinding) {
        CloudResource resource = ((ResourceFinding) finding).getResource();
        return String.format(
            "Type: %s, Region: %s, Usage: %.2f%%, Carbon: %.2f",
            resource.getType(),
            resource.getRegion(),
            resource.getUsage(),
            resource.getCarbonfootprint()
        );
    } else if (finding instanceof ResourceFindingaws) {
        CloudResourcer resource = ((ResourceFindingaws) finding).getResource();
        return String.format(
            "Type: %s, Region: %s, Usage: %.2f%%, Carbon: %.2f, LastScanned: %s",
            resource.getType(),
            resource.getRegion(),
            resource.getUsage(),
            resource.getCarbonfootprint(),
            resource.getScanTimestamp()
        );
    }
    throw new IllegalArgumentException("Unsupported resource type");
}

private Mono<String[]> getAzureGcpRecommendation(ResourceFinding finding) {
    return aiClient.getRecommendation(
            "azure-gcp", 
            finding.getIssueType(),
            createResourceDescription(finding)  // Pass the finding object
        )
        .map(this::parseAIResponse)
        .defaultIfEmpty(getDefaultAzureRecommendation(finding))
        .onErrorResume(e -> Mono.just(getDefaultAzureRecommendation(finding)));
}


    private Mono<String[]> getAwsRecommendation(ResourceFindingaws finding) {
        // First try AI recommendation
        return aiClient.getRecommendation(
                "aws", 
                finding.getIssueType(),
                createResourceDescription(finding.getResource()))
            .map(this::parseAIResponse)
            .defaultIfEmpty(getDefaultAwsRecommendation(finding))
            .onErrorResume(e -> Mono.just(getDefaultAwsRecommendation(finding)));
    }

    private String[] getDefaultAzureRecommendation(ResourceFinding finding) {
        return AZURE_DEFAULT_RECOMMENDATIONS.getOrDefault(
            finding.getIssueType().toLowerCase(),
            new String[]{"none", "No specific action recommended."}
        );
    }

    private String[] getDefaultAwsRecommendation(ResourceFindingaws finding) {
        return AWS_DEFAULT_RECOMMENDATIONS.getOrDefault(
            finding.getIssueType().toLowerCase(),
            new String[]{"none", "No AWS-specific action recommended."}
        );
    }

    private String createResourceDescription(CloudResourcer resource) {
        return String.format(
            "Type: %s, Region: %s, Usage: %.2f%%, Carbon: %.2f",
            resource.getType(),
            resource.getRegion(),
            resource.getUsage(),
            resource.getCarbonfootprint()
        );
    }

    private String[] parseAIResponse(String aiResponse) {
        try {
            // Simple parsing - adjust based on your AI response format
            String action = aiResponse.split("\"action\":\"")[1].split("\"")[0];
            String explanation = aiResponse.split("\"explanation\":\"")[1].split("\"")[0];
            return new String[]{action, explanation};
        } catch (Exception e) {
            return new String[]{"analyze", "AI recommendation could not be parsed"};
        }
    }
}
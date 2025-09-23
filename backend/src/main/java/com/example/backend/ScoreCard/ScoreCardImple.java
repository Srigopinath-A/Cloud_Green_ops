package com.example.backend.ScoreCard;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.DayOfWeek;
import com.example.backend.Analyzer.ResourceAnalyzerService;
import com.example.backend.GenAi.GenAiRecommendationService;
import com.example.backend.Model.CloudResource;
import com.example.backend.Model.CloudResourcer;
import com.example.backend.Model.Rcommendation;
import com.example.backend.Model.Rcommendationaws;
import com.example.backend.Model.ResourceFinding;
import com.example.backend.Model.ResourceFindingaws;
import com.example.backend.Model.Scorecard;
import com.example.backend.Model.Scorecardaws;
import com.example.backend.Repo.CloudResourceRepository;
import com.example.backend.Service.CloudScannerService;

@Service
public class ScoreCardImple implements ScorecardService {
    private static final Logger logger = LoggerFactory.getLogger(ScoreCardImple.class);

    private final CloudScannerService cloudscanner;
    private final ResourceAnalyzerService analyzerService;
    private final GenAiRecommendationService genAiRecommendationService;
    private final CloudResourceRepository resourceRepository;

    @Autowired
    public ScoreCardImple(CloudScannerService cloudscanner,
                        ResourceAnalyzerService analyzerService,
                        GenAiRecommendationService genAiRecommendationService,
                        CloudResourceRepository resourceRepository) {
        this.cloudscanner = cloudscanner;
        this.analyzerService = analyzerService;
        this.genAiRecommendationService = genAiRecommendationService;
        this.resourceRepository = resourceRepository;
    }

    @Override
    public Scorecard generateWeeklyScorecard() {
        try {
            logger.info("Starting to generate weekly scorecard...");
            
            // Initialize empty lists for each cloud provider
            List<CloudResource> allResources = new ArrayList<>();
            
            // Scan each cloud provider separately and handle results properly
            try {
                List<CloudResource> awsResources = safeCastToCloudResourceList(cloudscanner.scanAws());
                allResources.addAll(awsResources);
            } catch (Exception e) {
                logger.error("Failed to scan AWS resources: {}", e.getMessage());
            }
            
          
            allResources.addAll(cloudscanner.scanGcp());

            logger.info("Total resources scanned: {}", allResources.size());
            
            List<ResourceFinding> findings = analyzerService.analyze(allResources);
            logger.info("Total findings identified: {}", findings.size());
            
            List<Rcommendation> recommendations = genAiRecommendationService.recommed(findings);
            logger.info("Generated recommendations: {}", recommendations.size());

            double score = calculateScore(findings);
            String week = LocalDate.now().toString();
            
            logger.info("Weekly scorecard generated successfully with score: {}", score);
            return new Scorecard(score, recommendations, week);
        } catch (Exception e) {
            logger.error("Error generating weekly scorecard: {}", e.getMessage(), e);
            throw new RuntimeException("Error generating weekly scorecard", e);
        }
    }

    @Override
    public Scorecardaws generateAWSScorecard(LocalDate forDate) {
        try {
            logger.info("Generating AWS scorecard for the week of {}", forDate);

            Instant startOfWeek = forDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                                       .atStartOfDay()
                                       .toInstant(ZoneOffset.UTC);
            Instant endOfWeek = forDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                                     .plusDays(1)
                                     .atStartOfDay()
                                     .toInstant(ZoneOffset.UTC);

            List<CloudResourcer> weeklySnapshots = resourceRepository.findByScanTimestampBetween(startOfWeek, endOfWeek);
            logger.info("Found {} AWS resource snapshots for the week", weeklySnapshots.size());

            if (weeklySnapshots.isEmpty()) {
                return new Scorecardaws(100.0, List.of(), "Week of " + startOfWeek.toString().substring(0, 10));
            }



            List<CloudResourcer> averagedResources = averageResourcesOverWeek(weeklySnapshots);
            List<ResourceFindingaws> findings = analyzerService.analyzeaws(averagedResources);
            List<Rcommendationaws> recommendations = genAiRecommendationService.recommedaws(findings);

            double score = calculateScoreForAWS(findings);
            String week = "Week of " + startOfWeek.toString().substring(0, 10);
            
            logger.info("AWS scorecard generated successfully with score: {}", score);
            return new Scorecardaws(score, recommendations, week);
        } catch (Exception e) {
            logger.error("Error generating AWS scorecard: {}", e.getMessage(), e);
            throw new RuntimeException("Error generating AWS scorecard", e);
        }
    }

    @Override
    public Scorecard generateAzureWeeklyScorecard() {
        try {
            logger.info("Starting to generate weekly scorecard...");
            
            // Initialize empty lists for each cloud provider
            List<CloudResource> allResources = new ArrayList<>();
            
            // Scan each cloud provider separately and handle results properly
            try {
                List<CloudResource> awsResources = safeCastToCloudResourceList(cloudscanner.scanAws());
                allResources.addAll(awsResources);
            } catch (Exception e) {
                logger.error("Failed to scan AWS resources: {}", e.getMessage());
            }
            
            allResources.addAll(cloudscanner.scanAzure());

            logger.info("Total resources scanned: {}", allResources.size());
            
            List<ResourceFinding> findings = analyzerService.analyze(allResources);
            logger.info("Total findings identified: {}", findings.size());
            
            List<Rcommendation> recommendations = genAiRecommendationService.recommed(findings);
            logger.info("Generated recommendations: {}", recommendations.size());

            double score = calculateScore(findings);
            String week = LocalDate.now().toString();
            
            logger.info("Weekly scorecard generated successfully with score: {}", score);
            return new Scorecard(score, recommendations, week);
        } catch (Exception e) {
            logger.error("Error generating weekly scorecard: {}", e.getMessage(), e);
            throw new RuntimeException("Error generating weekly scorecard", e);
        }
    }

    // Helper Methods
    private List<CloudResource> safeCastToCloudResourceList(Object scanResult) {
        if (scanResult instanceof List) {
            try {
                return ((List<?>) scanResult).stream()
                    .filter(CloudResource.class::isInstance)
                    .map(CloudResource.class::cast)
                    .collect(Collectors.toList());
            } catch (ClassCastException e) {
                logger.warn("Failed to cast scan result to CloudResource list", e);
            }
        }
        return new ArrayList<>();
    }

    private List<CloudResourcer> averageResourcesOverWeek(List<CloudResourcer> snapshots) {
        return snapshots.stream()
            .collect(Collectors.groupingBy(CloudResourcer::getInstanceId))
            .values().stream()
            .map(this::calculateAverageResource)
            .collect(Collectors.toList());
    }

    private CloudResourcer calculateAverageResource(List<CloudResourcer> resources) {
        CloudResourcer first = resources.get(0);
        double avgUsage = resources.stream().mapToDouble(CloudResourcer::getUsage).average().orElse(0.0);
        double avgCarbon = resources.stream().mapToDouble(CloudResourcer::getCarbonfootprint).average().orElse(0.0);
        
        return new CloudResourcer(
            null, 
            first.getInstanceId(), 
            first.getType(), 
            first.getProvider(), 
            first.getRegion(), 
            avgUsage, 
            avgCarbon, 
            null
        );
    }

    private double calculateScore(List<ResourceFinding> findings) {
        if (findings.isEmpty()) return 100.0;
        return Math.max(0, 100.0 - (findings.size() * 5.0));
    }

    private double calculateScoreForAWS(List<ResourceFindingaws> findings) {
        if (findings.isEmpty()) return 100.0;
        return Math.max(0, 100.0 - (findings.size() * 5.0));
    }

    private double calculateScoreForAzure(List<ResourceFinding> findings) {
        if (findings.isEmpty()) return 100.0;
        return Math.max(0, 100.0 - (findings.size() * 7.0));
    }
}
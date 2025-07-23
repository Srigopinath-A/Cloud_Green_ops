package com.example.backend.ScoreCard;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;
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
public class ScoreCardImple implements ScorecardService{
    private static final Logger logger = LoggerFactory.getLogger(ScoreCardImple.class);

   // It's generally good practice to make all injected dependencies final if possible,
    // and inject them via the constructor. This promotes immutability and makes
    // dependencies explicit.
    private final CloudScannerService cloudscanner;
    private final ResourceAnalyzerService analyzerService;
    private final GenAiRecommendationService genAiRecommendationService;
    private final CloudResourceRepository resourceRepository; // This is the field that caused the error

    // Constructor for dependency injection
    // Spring will automatically call this constructor and inject the required beans.
    @Autowired // @Autowired is optional here if there's only one constructor, but good for clarity sometimes
    public ScoreCardImple(CloudScannerService cloudscanner,
                          ResourceAnalyzerService analyzerService,
                          GenAiRecommendationService genAiRecommendationService,
                          CloudResourceRepository resourceRepository) {
        this.cloudscanner = cloudscanner;
        this.analyzerService = analyzerService;
        this.genAiRecommendationService = genAiRecommendationService;
        this.resourceRepository = resourceRepository; // Initialize the final field here
    }
    @Override
    public Scorecard generateWeeklyScorecard() {
        try {
            System.out.println("Starting to generate weekly scorecard...");
            List<CloudResource> allRes = new ArrayList<>();
            //allRes.addAll(cloudscanner.scanAws());
            //allRes.addAll(cloudscanner.scanAzure());
            allRes.addAll(cloudscanner.scanGcp());
    
            System.out.println("Resources scanned: " + allRes.size());
            List<ResourceFinding> findings = analyzerService.analyze(allRes);
            System.out.println("Findings: " + findings.size());
            List<Rcommendation> recs = genAiRecommendationService.recommed(findings);
            System.out.println("Recommendations: " + recs.size());
    
            double score = 100.0;
            if (!findings.isEmpty()) {
                score = 100.0 - findings.size() * 5.0;
            }
            String week = LocalDate.now().toString();
            System.out.println("Scorecard generated successfully.");
            return new Scorecard(score, recs, week);
        } catch (Exception e) {
            System.err.println("Error generating weekly scorecard: " + e.getMessage());
            throw new RuntimeException("Error generating weekly scorecard", e);
        }
    }


    
     @Override
    public Scorecardaws generateAWSScorecard(LocalDate forDate) {
        logger.info("Generating weekly scorecard for the week of {}", forDate);

        // 1. Define the time range for the week
        Instant startOfWeek = forDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant endOfWeek = forDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

        // 2. Fetch the historical data for that week from MongoDB
        List<CloudResourcer> weeklySnapshots = resourceRepository.findByScanTimestampBetween(startOfWeek, endOfWeek);
        logger.info("Found {} resource snapshots for the week between {} and {}", weeklySnapshots.size(), startOfWeek, endOfWeek);

        if (weeklySnapshots.isEmpty()) {
            return new Scorecardaws(100.0, List.of(), "Week of " + startOfWeek.toString().substring(0, 10)); // Perfect score if no resources
        }

        // 3. To analyze, we need the *average* state of each resource for the week
        List<CloudResourcer> averagedResources = averageResourcesOverWeek(weeklySnapshots);
        
        // 4. Analyze the averaged data
        List<ResourceFindingaws> findings = analyzerService.analyzeaws(averagedResources);
        logger.info("Analysis complete. Found {} issues.", findings.size());

        // 5. Generate recommendations based on findings
        List<Rcommendationaws> recs = genAiRecommendationService.recommedaws(findings);
        logger.info("Generated {} recommendations.", recs.size());

        // 6. Calculate the score
        double score = 100.0 - (findings.size() * 5.0); // Keep your logic
        String week = "Week of " + startOfWeek.toString().substring(0, 10);
        
        logger.info("Scorecard generated successfully with score: {}", score);
        return new Scorecardaws(score, recs, week);
    }
    
    // Helper method to process historical data
    private List<CloudResourcer> averageResourcesOverWeek(List<CloudResourcer> snapshots) {
        // Group snapshots by the unique instance ID
        Map<String, List<CloudResourcer>> snapshotsByInstanceId = snapshots.stream()
                .collect(Collectors.groupingByConcurrent(CloudResourcer::getInstanceId));
        
        // For each instance, calculate its average state over the week
        return snapshotsByInstanceId.values().stream()
                .map(instanceSnapshots -> {
                    CloudResourcer first = instanceSnapshots.get(0);
                    double avgUsage = instanceSnapshots.stream().mapToDouble(CloudResourcer::getUsage).average().orElse(0.0);
                    double avgCarbon = instanceSnapshots.stream().mapToDouble(CloudResourcer::getCarbonfootprint).average().orElse(0.0);
                   
                    // Create a single representative CloudResource object for analysis
                    return new CloudResourcer(null, first.getInstanceId(), first.getType(), first.getProvider(), first.getRegion(), avgUsage, avgCarbon, null);
                }).collect(Collectors.toList());
    }

    @Override
    public Scorecard generateAzureWeeklyScorecard() {
        try {
            logger.info("Starting to generate Azure-specific weekly scorecard...");
            List<CloudResource> azureResources = cloudscanner.scanAzure();
            
            logger.info("Azure resources scanned: {}", azureResources.size());
            
            // Filter resources to analyze only those from Azure
            // (scanAzure already returns only Azure, but good practice if mixed source)
            List<CloudResource> filteredAzureResources = azureResources.stream()
                .filter(res -> "Azure".equalsIgnoreCase(res.getProvider()))
                .collect(Collectors.toList());

            logger.info("Filtered Azure resources for analysis: {}", filteredAzureResources.size());

            List<ResourceFinding> azureFindings = analyzerService.analyze(filteredAzureResources);
            logger.info("Azure specific findings: {}", azureFindings.size());
            
            List<Rcommendation> azureRecs = genAiRecommendationService.recommed(azureFindings);
            logger.info("Azure specific recommendations: {}", azureRecs.size());
    
            double score = 100.0;
            if (!azureFindings.isEmpty()) {
                // Score deduction. You might want a different scoring logic for specific clouds
                score = Math.max(0, 100.0 - azureFindings.size() * 7.0); // Slightly more punitive for Azure specific (example)
            } else {
                score = 100.0;
            }

            String week = LocalDate.now().toString();
            logger.info("Azure-specific scorecard generated successfully for week {}. Score: {}", week, score);
            return new Scorecard(score, azureRecs, week);
        } catch (Exception e) {
            logger.error("Error generating Azure weekly scorecard: " + e.getMessage(), e);
            throw new RuntimeException("Error generating Azure weekly scorecard", e);
        }
    }
}


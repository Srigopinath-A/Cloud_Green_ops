package com.example.backend.ScoreCard;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.backend.Analyzer.ResourceAnalyzerService;
import com.example.backend.GenAi.GenAiRecommendationService;
import com.example.backend.Model.CloudResource;
import com.example.backend.Model.Rcommendation;
import com.example.backend.Model.ResourceFinding;
import com.example.backend.Model.Scorecard;
import com.example.backend.Service.CloudScannerService;

@Service
public class ScoreCardImple implements ScorecardService{

    private final CloudScannerService cloudscanner;
    private final ResourceAnalyzerService analyzerService;
    private final GenAiRecommendationService genAiRecommendationService;

    public ScoreCardImple(CloudScannerService cloudscanner, ResourceAnalyzerService analyzerService, GenAiRecommendationService gRecommendationService) {
        this.cloudscanner = cloudscanner;
        this.analyzerService = analyzerService;
        this.genAiRecommendationService = gRecommendationService;
    }

    @Override
    public Scorecard generateWeeklyScorecard() {
        try {
            System.out.println("Starting to generate weekly scorecard...");
            List<CloudResource> allRes = new ArrayList<>();
            allRes.addAll(cloudscanner.scanAws());
            allRes.addAll(cloudscanner.scanAzure());
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
}


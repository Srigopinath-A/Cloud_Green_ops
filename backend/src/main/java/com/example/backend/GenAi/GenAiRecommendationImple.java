package com.example.backend.GenAi;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.backend.Model.Rcommendation;
import com.example.backend.Model.Rcommendationaws;
import com.example.backend.Model.ResourceFinding;
import com.example.backend.Model.ResourceFindingaws;

@Service
public class GenAiRecommendationImple implements GenAiRecommendationService {

    @Override
    public List<Rcommendation> recommed(List<ResourceFinding> findings) {
        List<Rcommendation> rec = new ArrayList<>();
        for(ResourceFinding finding : findings){
            String actions;
            String aiExp;
            switch (finding.getIssueType()) {
                case "idle" -> {
                    actions = "decommission";
                    aiExp = "This resource is mostly idle and can be safely decommissioned to save costs and emissions.";
                }
                case "carbon-heavy" -> {
                    actions = "decommission";
                    aiExp = "This resource is mostly idle and can be safely decommissioned to save costs and emissions.";
                }
                case "overprovisioned" -> {
                    actions = "right-size";
                    aiExp = "Resource appears oversized; right-sizing can save costs and emissions.";
                }
                default -> {
                    actions = "none";
                    aiExp = "No action required.";
                }
        }
        rec.add(new Rcommendation(finding,actions,aiExp));
    } 
    return rec;
    }

    @Override
    public List<Rcommendationaws> recommedaws(List<ResourceFindingaws> findings) {
        List<Rcommendationaws> rec = new ArrayList<>();
        for(ResourceFindingaws finding : findings){
            String actions;
            String aiExp;
            switch (finding.getIssueType()) {
                case "idle" -> {
                    actions = "decommission";
                    aiExp = "This resource is mostly idle and can be safely decommissioned to save costs and emissions.";
                }
                case "carbon-heavy" -> {
                    actions = "decommission";
                    aiExp = "This resource is mostly idle and can be safely decommissioned to save costs and emissions.";
                }
                case "overprovisioned" -> {
                    actions = "right-size";
                    aiExp = "Resource appears oversized; right-sizing can save costs and emissions.";
                }
                default -> {
                    actions = "none";
                    aiExp = "No action required.";
                }
        }
        rec.add(new Rcommendationaws(finding,actions,aiExp));
    } 
    return rec;
    }


    
}


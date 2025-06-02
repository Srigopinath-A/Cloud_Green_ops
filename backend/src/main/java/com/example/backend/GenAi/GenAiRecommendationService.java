package com.example.backend.GenAi;

import java.util.List;

import com.example.backend.Model.Rcommendation;
import com.example.backend.Model.ResourceFinding;

public interface GenAiRecommendationService {
    List<Rcommendation> recommed (List<ResourceFinding> findings);
}

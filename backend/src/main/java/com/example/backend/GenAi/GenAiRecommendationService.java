package com.example.backend.GenAi;

import java.util.List;

import com.example.backend.Model.Rcommendation;
import com.example.backend.Model.Rcommendationaws;
import com.example.backend.Model.ResourceFinding;
import com.example.backend.Model.ResourceFindingaws;

public interface GenAiRecommendationService {
    List<Rcommendation> recommed (List<ResourceFinding> findings);
    List<Rcommendationaws> recommedaws (List<ResourceFindingaws> findings);
}

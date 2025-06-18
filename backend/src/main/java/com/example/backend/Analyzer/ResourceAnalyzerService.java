package com.example.backend.Analyzer;

import java.util.List;

import com.example.backend.Model.CloudResource;
import com.example.backend.Model.CloudResourcer;
import com.example.backend.Model.ResourceFinding;
import com.example.backend.Model.ResourceFindingaws;

public interface ResourceAnalyzerService {
    List<ResourceFinding> analyze (List<CloudResource> resources);
    List<ResourceFindingaws> analyzeaws (List<CloudResourcer> resources);
}

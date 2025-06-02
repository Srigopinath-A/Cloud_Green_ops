package com.example.backend.Analyzer;

import java.util.List;

import com.example.backend.Model.CloudResource;
import com.example.backend.Model.ResourceFinding;

public interface ResourceAnalyzerService {
    List<ResourceFinding> analyze (List<CloudResource> resources);
}

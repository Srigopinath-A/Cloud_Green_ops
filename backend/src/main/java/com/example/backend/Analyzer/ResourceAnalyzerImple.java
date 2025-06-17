package com.example.backend.Analyzer;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.backend.Model.CloudResource;
import com.example.backend.Model.CloudResourcer;
import com.example.backend.Model.ResourceFinding;
import com.example.backend.Model.ResourceFindingaws;

@Service
public class ResourceAnalyzerImple implements ResourceAnalyzerService{

    @Override
    public List<ResourceFinding> analyze(List<CloudResource> resources) {
        List<ResourceFinding> findings = new ArrayList<>();
        for (CloudResource res : resources) {
            if (res.getUsage() < 15.0) {
                findings.add(new ResourceFinding(res, "idle", "Resource is idle (<15% usage)"));
            } else if (res.getUsage() > 75.0 && res.getCarbonfootprint() > 60.0) {
                findings.add(new ResourceFinding(res, "carbon-heavy", "High usage in high-carbon region"));
            } else if (res.getUsage() > 70.0) {
                findings.add(new ResourceFinding(res, "overprovisioned", "Resource might be oversized"));
            }
        }
        return findings;
    }

    @Override
    public List<ResourceFindingaws> analyzeaws(List<CloudResourcer> resources) {
        List<ResourceFindingaws> findings = new ArrayList<>();
        for (CloudResourcer res : resources) {
            if (res.getUsage() < 15.0) {
                findings.add(new ResourceFindingaws(res, "idle", "Resource is idle (<15% usage)"));
            } else if (res.getUsage() > 75.0 && res.getCarbonfootprint() > 60.0) {
                findings.add(new ResourceFindingaws(res, "carbon-heavy", "High usage in high-carbon region"));
            } else if (res.getUsage() > 70.0) {
                findings.add(new ResourceFindingaws(res, "overprovisioned", "Resource might be oversized"));
            }
        }
        return findings;
    }

}

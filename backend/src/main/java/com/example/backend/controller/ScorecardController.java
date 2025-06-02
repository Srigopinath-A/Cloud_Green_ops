package com.example.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.Model.Scorecard;
import com.example.backend.ScoreCard.ScorecardService;

@RestController
public class ScorecardController {

    @Autowired
    private ScorecardService scorecardService;

    @GetMapping("/scorecard") // Use lowercase for consistency
    public Scorecard getWeeklyScorecard() {
        return scorecardService.generateWeeklyScorecard();
    }
}

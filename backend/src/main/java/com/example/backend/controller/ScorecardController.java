package com.example.backend.controller;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.Model.Scorecard;
import com.example.backend.Model.Scorecardaws;
import com.example.backend.ScoreCard.ScorecardService;

@RestController
@CrossOrigin("http://localhost:5173/")
public class ScorecardController {

    @Autowired
    private ScorecardService scorecardService;

    @GetMapping("/scorecard/gcp") // Use lowercase for consistency
    public Scorecard getWeeklyScorecard() {
        return scorecardService.generateWeeklyScorecard();
    }

    @GetMapping("/scorecard/aws")
    public ResponseEntity<Scorecardaws> getWeeklyScorecard(
            // Make the date parameter optional. If not provided, it will use the current date.
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        // If no date is specified, default to today
        LocalDate targetDate = Optional.ofNullable(date).orElse(LocalDate.now());

        Scorecardaws scorecard = scorecardService.generateAWSScorecard(targetDate);
        return ResponseEntity.ok(scorecard);
    }

    @GetMapping("/scorecard/azure") // New endpoint for Azure-specific scorecard
    public Scorecard getWeeklyAzureScorecard() {
        return scorecardService.generateAzureWeeklyScorecard();
    }
}

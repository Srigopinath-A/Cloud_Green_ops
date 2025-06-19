package com.example.backend.ScoreCard;

import com.example.backend.Model.Scorecard;
import com.example.backend.Model.Scorecardaws;
import com.example.backend.Model.Scorecard;
import java.time.LocalDate;

public interface ScorecardService {
    Scorecard generateWeeklyScorecard();
    Scorecardaws generateAWSScorecard(LocalDate forDate);
    Scorecard  generateAzureWeeklyScorecard();
}

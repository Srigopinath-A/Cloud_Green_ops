package com.example.backend.Model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Scorecardaws {
    private double sustainabilityScore;
    private List<Rcommendationaws> remediationPlan;
    private String week;
}

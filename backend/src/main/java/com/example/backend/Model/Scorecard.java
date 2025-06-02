package com.example.backend.Model;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "Scorecard")
@Getter
@Setter
public class Scorecard {
    private double sustainabilityScore;
    private List<Rcommendation> remediationPlan;
    private String week;
}

package com.example.backend.Model;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "Recommendation")
@Getter
@Setter
public class Rcommendation {
    private ResourceFinding finding;
    private String actions;
    private String aiExplanation;
}

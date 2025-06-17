package com.example.backend.Model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Rcommendationaws {
    private ResourceFindingaws finding;
    private String actions;
    private String aiExplanation;
}

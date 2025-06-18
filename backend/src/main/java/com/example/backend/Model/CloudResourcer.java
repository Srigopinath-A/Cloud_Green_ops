package com.example.backend.Model;

import java.time.Instant;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document (collection = "AWS")
public class CloudResourcer {
    @MongoId // <-- Let MongoDB manage the DB ID
    private String dbId; 

    @SerializedName("id")
    private String instanceId; // Renamed for clarity
    
    // ... other fields are fine ...
    private String type;
    private String provider;
    private String region;
    private double usage;
    private double carbonfootprint;

    // --- THIS IS THE CRUCIAL ADDITION ---
    private Instant scanTimestamp;
}

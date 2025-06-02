package com.example.backend.Model;



import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "CloudResource")
@Getter
@Setter
public class CloudResource {
    private String id;
    private String type;
    private String provider;
    private String region;
    private double usage;
    private double carbonfootprint;
}

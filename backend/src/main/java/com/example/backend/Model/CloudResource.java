package com.example.backend.Model;



import org.springframework.data.mongodb.core.mapping.Document;

import com.google.gson.annotations.SerializedName;

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
    @SerializedName("id")
    private String id;
    
    @SerializedName("type")
    private String type;
    
    @SerializedName("provider")
    private String provider;
    
    @SerializedName("region")
    private String region;
    
    @SerializedName("usage")
    private double usage;
    
    @SerializedName("carbonfootprint")
    private double carbonfootprint;
}

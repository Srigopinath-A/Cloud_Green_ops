package com.example.backend.Model;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "ResourceFinding")
@Setter
@Getter
public class ResourceFinding {
    private CloudResource resource;
    private String IssueType; //overprovisied,idel, carbon heavy resouce 
    private String details;
}

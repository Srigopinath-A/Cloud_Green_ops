package com.example.backend.Model;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "ResourceFindingaws")
public class ResourceFindingaws {
    private CloudResourcer resource;
    private String IssueType; //overprovisied,idel, carbon heavy resouce 
    private String details;
}

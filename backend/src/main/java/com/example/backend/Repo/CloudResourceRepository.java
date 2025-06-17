package com.example.backend.Repo;

import java.time.Instant;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.backend.Model.CloudResource;
import com.example.backend.Model.CloudResourcer;

public interface CloudResourceRepository extends MongoRepository<CloudResourcer, String>{

    /**
     * Finds all resource snapshots recorded within a given time range.
     * This is the key method for generating weekly reports.
     * Spring Data MongoDB will automatically create the query from this method name.
     */
    List<CloudResourcer> findByScanTimestampBetween(Instant start, Instant end);
}

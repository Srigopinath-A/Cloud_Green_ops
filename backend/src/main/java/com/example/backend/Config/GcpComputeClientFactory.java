package com.example.backend.Config;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.google.api.client.util.Value;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.InstancesSettings;

@Component
public class GcpComputeClientFactory {

    @Value("${cloud.gcp.serviceAccountKey}")
    private String serviceAccountKey;

    public GcpComputeClientFactory(Environment env) {
        this.serviceAccountKey = env.getProperty("cloud.gcp.serviceAccountKey");
    }

    public InstancesClient createInstancesClient() throws IOException {
        // Load service account credentials and apply the required scope
        GoogleCredentials credentials = ServiceAccountCredentials.fromStream(
            new ByteArrayInputStream(serviceAccountKey.getBytes(StandardCharsets.UTF_8)))
            .createScoped(Collections.singletonList("https://www.googleapis.com/auth/compute")); // Ensure scope is applied

        // Configure InstancesClient with the scoped credentials
        InstancesSettings settings = InstancesSettings.newBuilder()
            .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
            .build();

        return InstancesClient.create(settings);
    }
}
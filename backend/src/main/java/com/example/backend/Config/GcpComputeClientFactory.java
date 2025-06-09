package com.example.backend.Config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.InstancesSettings;

@Component
public class GcpComputeClientFactory {

    @Value("${cloud.gcp.serviceAccountKey}")
    private String serviceAccountKeyFile;

    public InstancesClient createInstancesClient() throws IOException {
        GoogleCredentials credentials = GoogleCredentials.fromStream(
            new FileInputStream(serviceAccountKeyFile))
            .createScoped("https://www.googleapis.com/auth/cloud-platform");

        return InstancesClient.create(InstancesSettings.newBuilder()
            .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
            .build());
    }
}
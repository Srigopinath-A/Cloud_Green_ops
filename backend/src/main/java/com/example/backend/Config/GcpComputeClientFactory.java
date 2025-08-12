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
import com.google.cloud.compute.v1.RegionsClient;
import com.google.cloud.compute.v1.RegionsSettings;
import com.google.cloud.compute.v1.ZonesClient;
import com.google.cloud.compute.v1.ZonesSettings;
import com.google.cloud.monitoring.v3.MetricServiceClient;
import com.google.cloud.monitoring.v3.MetricServiceSettings;

@Component
public class GcpComputeClientFactory {
    @Value("${cloud.gcp.serviceAccountKey}")
    private String serviceAccountKeyFile;

    private GoogleCredentials getCredentials() throws IOException {
        return GoogleCredentials.fromStream(
            new FileInputStream(serviceAccountKeyFile))
            .createScoped("https://www.googleapis.com/auth/cloud-platform");
    }

    public InstancesClient createInstancesClient() throws IOException {
        return InstancesClient.create(
            InstancesSettings.newBuilder()
                .setCredentialsProvider(FixedCredentialsProvider.create(getCredentials()))
                .build());
    }

    public RegionsClient createRegionsClient() throws IOException {
        return RegionsClient.create(
            RegionsSettings.newBuilder()
                .setCredentialsProvider(FixedCredentialsProvider.create(getCredentials()))
                .build());
    }

    public ZonesClient createZonesClient() throws IOException {
        return ZonesClient.create(
            ZonesSettings.newBuilder()
                .setCredentialsProvider(FixedCredentialsProvider.create(getCredentials()))
                .build());
    }

    public MetricServiceClient createMetricServiceClient() throws IOException {
        return MetricServiceClient.create(
            MetricServiceSettings.newBuilder()
                .setCredentialsProvider(FixedCredentialsProvider.create(getCredentials()))
                .build());
    }
}
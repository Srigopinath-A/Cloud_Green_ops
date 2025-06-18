package com.example.backend.Service;

import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.example.backend.Config.GcpComputeClientFactory;
import com.example.backend.Model.CloudResource;
import com.example.backend.Model.CloudResourcer;
import com.example.backend.Repo.CloudResourceRepository;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.compute.v1.*;
import com.google.cloud.monitoring.v3.MetricServiceClient;
import com.google.cloud.monitoring.v3.MetricServiceSettings;
import com.google.monitoring.v3.*;
import com.google.protobuf.util.Timestamps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.Reservation;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.*;

@Service
public class CloudScannerServiceImpl implements CloudScannerService {

    private static final Logger logger = LoggerFactory.getLogger(CloudScannerServiceImpl.class);
    private static final Map<String, Double> CARBON_INTENSITY_DATA = Map.of(
        "us-central1", 0.4, 
        "europe-west1", 0.2,
        "asia-southeast1", 0.3
    );
    private static final Map<String, Double> POWER_USAGE_DATA = Map.of(
        "e2-medium", 0.07,
        "e2-small", 0.03,
        "n1-standard-1", 0.12
    );


   
    // --- REFACTOR: Use final for injected dependencies ---
    @Autowired
    private final GcpComputeClientFactory gcpComputeClientFactory;
    @Autowired
    private final CloudResourceRepository resourceRepository;


    // --- REFACTOR: Use a single constructor for all dependencies (Spring best practice) ---
    public CloudScannerServiceImpl(CloudResourceRepository resourceRepository, GcpComputeClientFactory gcpComputeClientFactory) {
        this.resourceRepository = resourceRepository;
        this.gcpComputeClientFactory = gcpComputeClientFactory;
    }

    // This method will run automatically at 2 AM every day.
    @Scheduled(cron = "0 0 2 * * ?")
    public void scheduledAwsScan() {
        logger.info("Starting scheduled daily scan of AWS resources...");
        List<CloudResourcer> scannedResources = scanAws();
        
        if (!scannedResources.isEmpty()) {
            // Save all the newly scanned resources to the database
            resourceRepository.saveAll(scannedResources);
            logger.info("Successfully saved {} AWS resource snapshots to the database.", scannedResources.size());
        } else {
            logger.warn("Scheduled AWS scan completed, but no resources were found or saved.");
        }
    }

    @Override
    // FIX: The return type must be List<CloudResource>, not CloudResourcer
    public List<CloudResourcer> scanAws() {
        logger.info("Executing live AWS scan...");
        // FIX: The list must hold CloudResource objects
        List<CloudResourcer> result = new ArrayList<>();
        String accessKey = System.getenv("AWS_ACCESS_KEY");
        String secretKey = System.getenv("AWS_SECRET_KEY");
        String region = System.getenv("AWS_REGION");

        if (accessKey == null || secretKey == null || region == null) {
            logger.error("AWS credentials not configured. Skipping scan.");
            return result;
        }

        try (Ec2Client ec2 = Ec2Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)
                ))
                .build()) {

            DescribeInstancesResponse response = ec2.describeInstances();
            for (Reservation reservation : response.reservations()) {
                for (Instance instance : reservation.instances()) {
                    // FIX: The class name is CloudResource, not CloudResourcer
                    CloudResourcer resource = new CloudResourcer();
                    resource.setInstanceId(instance.instanceId());
                    resource.setType("ec2");
                    resource.setProvider("AWS");
                    resource.setRegion(instance.placement().availabilityZone());
                    resource.setUsage(getAwsCpuUsage(instance.instanceId(), region));
                    resource.setCarbonfootprint(0); // Placeholder for carbon
                    resource.setScanTimestamp(Instant.now()); // Set the timestamp!
                    result.addAll((Collection<? extends CloudResourcer>) resource);
                }
            }
        } catch (Exception e) {
            logger.error("Error during live AWS scan", e);
        }
        logger.info("Live AWS scan finished, found {} resources.", result.size());
        return result;
    }
    
    // Mock method for AWS CPU usage
    private double getAwsCpuUsage(String instanceId, String region) {
        // Implement actual AWS CloudWatch API call here
        return Math.random() * 100; // Placeholder
    }
    
    // As you don't use GCP now, you can keep it simple
    // @Override
    // public List<CloudResource> scanGcp() throws Exception {
    //     logger.warn("GCP scan is not implemented in this version.");
    //     return new ArrayList<>();
    // }

    // @Override
    // public List<CloudResource> scanAzure() {
    //     List<CloudResource> results = new ArrayList<>();
    //     try {
    //         AzureResourceManager azure = AzureResourceManager
    //             .authenticate(
    //                 new DefaultAzureCredentialBuilder().build(),
    //                 new AzureProfile(AzureEnvironment.AZURE)
    //             )
    //             .withDefaultSubscription();
            
    //         azure.virtualMachines().list().forEach(vm -> {
    //             results.add(new CloudResource(
    //                 vm.id(),
    //                 "vm",
    //                 "Azure",
    //                 vm.regionName(),
    //                 getAzureCpuUsage(vm.id()),
    //                 0  // Placeholder for carbon
    //             ));
    //         });
    //     } catch (Exception e) {
    //         logger.error("Error scanning Azure resources", e);
    //     }
    //     return results;
    // }

    private double getAzureCpuUsage(String vmId) {
        // Implement actual Azure Monitor API call here
        return Math.random() * 100; // Placeholder
    }

    @Override
    public List<CloudResource> scanGcp() throws IOException {
        List<CloudResource> results = new ArrayList<>();
        String projectId = getGcpProjectId();
        if (projectId == null) return results;

        try (InstancesClient instancesClient = gcpComputeClientFactory.createInstancesClient();
             RegionsClient regionsClient = createRegionsClient();
             ZonesClient zonesClient = createZonesClient();
             MetricServiceClient metricClient = createMetricClient()) {
            
            List<String> regions = getAllRegions(regionsClient, projectId);
            for (String region : regions) {
                List<String> zones = getAllZonesForRegion(zonesClient, region, projectId);
                for (String zone : zones) {
                    for (com.google.cloud.compute.v1.Instance instance : 
                         instancesClient.list(projectId, zone).iterateAll()) {
                        try {
                            CloudResource resource = createCloudResource(
                                instance, 
                                region,
                                metricClient,
                                projectId
                            );
                            results.add(resource);
                        } catch (Exception e) {
                            logger.error("Error processing instance {}: {}", instance.getName(), e.getMessage());
                        }
                    }
                }
            }
        }
        return results;
    }

    private String getGcpProjectId() {
        String projectId = "custom-healer-458206-t8";
        if (projectId == null || projectId.isEmpty()) {
            logger.error("GCP_PROJECT_ID environment variable not set");
            return null;
        }
        return projectId;
    }

    private RegionsClient createRegionsClient() throws IOException {
        return RegionsClient.create();
    }

    private ZonesClient createZonesClient() throws IOException {
        return ZonesClient.create();
    }

    private MetricServiceClient createMetricClient() throws IOException {
        return MetricServiceClient.create();
    }

    private CloudResource createCloudResource(
        com.google.cloud.compute.v1.Instance instance, 
        String region,
        MetricServiceClient metricClient,
        String projectId
    ) throws Exception {
        return new CloudResource(
            instance.getName(),  // Use name instead of ID
            "compute",
            "GCP",
            region,
            getOverallCpuUsage(metricClient, instance.getName(), projectId),
            calculateCarbonFootprint(instance, region)
        );
    }

    private double getOverallCpuUsage(
        MetricServiceClient metricClient, 
        String instanceName, 
        String projectId
    ) throws Exception {
        ProjectName projectName = ProjectName.of(projectId);
        long now = System.currentTimeMillis();
        
        TimeInterval interval = TimeInterval.newBuilder()
            .setStartTime(Timestamps.fromMillis(now - 300_000))  // 5 minutes ago
            .setEndTime(Timestamps.fromMillis(now))
            .build();

        Aggregation aggregation = Aggregation.newBuilder()
            .setAlignmentPeriod(com.google.protobuf.Duration.newBuilder().setSeconds(300).build())
            .setPerSeriesAligner(Aggregation.Aligner.ALIGN_MEAN)
            .build();

        ListTimeSeriesRequest request = ListTimeSeriesRequest.newBuilder()
            .setName(projectName.toString())
            .setFilter(String.format(
                "metric.type=\"compute.googleapis.com/instance/cpu/utilization\" " +
                "AND resource.labels.instance_id=\"%s\"", 
                instanceName))
            .setInterval(interval)
            .setAggregation(aggregation)
            .build();

        double total = 0;
        int count = 0;
        
        for (TimeSeries ts : metricClient.listTimeSeries(request).iterateAll()) {
            for (Point point : ts.getPointsList()) {
                total += point.getValue().getDoubleValue();
                count++;
            }
        }
        
        return count > 0 ? total / count : 0.0;
    }

    private double calculateCarbonFootprint(
        com.google.cloud.compute.v1.Instance instance, 
        String region
    ) {
        try {
            String machineType = extractMachineType(instance.getMachineType());
            double carbonIntensity = CARBON_INTENSITY_DATA.getOrDefault(region, 0.5);
            double powerUsage = POWER_USAGE_DATA.getOrDefault(machineType, 0.1);
            return carbonIntensity * powerUsage;
        } catch (Exception e) {
            logger.error("Error calculating carbon footprint: {}", e.getMessage());
            return 0.0;
        }
    }

    private String extractMachineType(String machineTypeUrl) {
        String[] parts = machineTypeUrl.split("/");
        return parts[parts.length - 1];
    }

    private List<String> getAllRegions(RegionsClient client, String projectId) {
        List<String> regions = new ArrayList<>();
        for (com.google.cloud.compute.v1.Region region : client.list(projectId).iterateAll()) {
            regions.add(region.getName().split("/")[region.getName().split("/").length - 1]);
        }
        return regions;
    }

    private List<String> getAllZonesForRegion(ZonesClient client, String region, String projectId) {
        List<String> zones = new ArrayList<>();
        for (Zone zone : client.list(projectId).iterateAll()) {
            String zoneName = zone.getName().split("/")[zone.getName().split("/").length - 1];
            if (zoneName.startsWith(region)) {
                zones.add(zoneName);
            }
        }
        return zones;
    }
}
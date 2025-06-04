package com.example.backend.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.management.AzureEnvironment;
import com.example.backend.Config.GcpComputeClientFactory;
import com.example.backend.Model.CloudResource;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.Region;
import com.google.cloud.compute.v1.RegionsClient;
import com.google.cloud.compute.v1.Zone;
import com.google.cloud.compute.v1.ZonesClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.protobuf.util.Timestamps;
import com.google.cloud.monitoring.v3.MetricServiceClient;
import com.google.cloud.monitoring.v3.MetricServiceClient.ListTimeSeriesPagedResponse;
import com.google.monitoring.v3.ProjectName;
import com.google.monitoring.v3.TimeInterval;
import com.google.monitoring.v3.Aggregation;
import com.google.monitoring.v3.Aggregation.Aligner;
import com.google.monitoring.v3.Aggregation.Reducer;
import com.google.monitoring.v3.ListTimeSeriesRequest;
import com.google.monitoring.v3.TimeSeries;
import com.google.monitoring.v3.Point;

import java.io.IOException;
import java.io.StringReader;

import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.Reservation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CloudScannerServiceImpl implements CloudScannerService {

    private static final Logger logger = LoggerFactory.getLogger(CloudScannerServiceImpl.class);

    @Autowired
    private GcpComputeClientFactory gcpComputeClientFactory;

    // @Override
    // public List<CloudResource> scanAws() {
    //     List<CloudResource> result = new ArrayList<>();
    //     try (Ec2Client ec2 = Ec2Client.create()){
    //         DescribeInstancesResponse response = ec2.describeInstances();
    //         for(Reservation reservation : response.reservations()){
    //             for(Instance instance: reservation.instances() ){
    //                 result.add( new CloudResource(
    //                 instance.instanceId(),
    //                 "ec2",
    //                 "AWS",
    //                 instance.placement().availabilityZone(),
    //                 0,
    //                 0
    //                 ));
    //             }
    //         }   
    //     }
    //     return result;
    // }

//     @Override
//     public List<CloudResource> scanAzure() {
//         List<CloudResource> results = new ArrayList<>();
//     AzureResourceManager azure = AzureResourceManager
//         .authenticate(new DefaultAzureCredentialBuilder().build(), new AzureProfile(AzureEnvironment.AZURE))
//         .withDefaultSubscription();
//     azure.virtualMachines().list().forEach(vm -> {
//         results.add(new CloudResource(
//             vm.id(),
//             "vm",
//             "Azure",
//             vm.regionName(),
//             0, // Needs metrics API for usage
//             0  // Carbon data mapping
//         ));
//     });
//     return results;
// }

@Override
public List<CloudResource> scanGcp() throws IOException {
    List<CloudResource> results = new ArrayList<>();

    // Get all available regions for the project
    List<String> regions = getAllRegions();

    for (String region : regions) {
        try (InstancesClient instancesClient = gcpComputeClientFactory.createInstancesClient()) {
            // Get all zones for the current region
            List<String> zones = getAllZonesForRegion(region);

            for (String zone : zones) {
                // List all instances in the specified project and zone
                for (com.google.cloud.compute.v1.Instance instance : instancesClient.list("custom-healer-458206-t8", zone).iterateAll()) {

                    try {
                        // Extract region from zone (remove the last part after '-')
                        //String zone = instance.getZone(); //No need to get zone from instance, we already have it

                        // Create CloudResource with actual data
                        CloudResource resource = new CloudResource(
                                String.valueOf(instance.getId()),
                                "compute",
                                "GCP",
                                region,
                                getOverallCpuUsage(String.valueOf(instance.getId())),  // Convert instance ID to String
                                calculateCarbonFootprint(instance, region)  // Implement this
                        );

                        results.add(resource);

                    } catch (Exception e) {
                        // Proper error logging
                        logger.error("Error processing instance: {}", instance.getId(), e);
                    }
                }
            }
        }
    }
    return results;
}


public double getOverallCpuUsage(String instanceId) throws Exception {
    double totalCpuUsage = 0.0;

    try (MetricServiceClient metricServiceClient = MetricServiceClient.create()) {
        // Define the project name
        ProjectName projectName = ProjectName.of("custom-healer-458206-t8");

        // Define the time interval (last 5 minutes)
        long now = System.currentTimeMillis();
        TimeInterval timeInterval = TimeInterval.newBuilder()
                .setStartTime(Timestamps.fromMillis(now - 5 * 60 * 1000))
                .setEndTime(Timestamps.fromMillis(now))
                .build();

        // Define the metric type for CPU usage
        String metricType = "compute.googleapis.com/instance/cpu/utilization";

        // Create the request
        ListTimeSeriesRequest request = ListTimeSeriesRequest.newBuilder()
                .setName(projectName.toString())
                .setFilter("metric.type = \"" + metricType + "\" AND resource.labels.instance_id = \"" + instanceId + "\"")
                .setInterval(timeInterval)
                .setView(ListTimeSeriesRequest.TimeSeriesView.FULL)
                .build();

        // Query the time series data
        ListTimeSeriesPagedResponse response = metricServiceClient.listTimeSeries(request);

        // Process the response
        for (TimeSeries timeSeries : response.iterateAll()) {
            List<Point> points = timeSeries.getPointsList();
            for (Point point : points) {
                totalCpuUsage += point.getValue().getDoubleValue();
            }
        }
    }

    return totalCpuUsage;
}

private double calculateCarbonFootprint(com.google.cloud.compute.v1.Instance instance, String region) {
    // Get the instance type
    String instanceType = instance.getMachineType();
    String[] parts = instanceType.split("/");
    String machineType = parts[parts.length - 1];

    // Get the carbon intensity for the region
    double carbonIntensity = getCarbonIntensityForRegion(region);

    // Get the power usage for the instance type
    double powerUsage = getPowerUsageForInstanceType(machineType);

    // Calculate the carbon footprint
    double carbonFootprint = carbonIntensity * powerUsage;

    return carbonFootprint;
}

private double getCarbonIntensityForRegion(String region) {
    // Load carbon intensity data from a file or database
    // Example: {"us-central1": 0.4, "europe-west1": 0.2, ...}
    Map<String, Double> carbonIntensityData = loadCarbonIntensityData();

    // Get the carbon intensity for the region
    Double carbonIntensity = carbonIntensityData.get(region);
    if (carbonIntensity == null) {
        // Handle the case where the region is not found
        carbonIntensity = 0.5; // Default value
    }

    return carbonIntensity;
}

private double getPowerUsageForInstanceType(String machineType) {
    // Load power usage data from a file or database
    // Example: {"e2-medium": 0.07, "e2-small": 0.03, ...}
    Map<String, Double> powerUsageData = loadPowerUsageData();

    // Get the power usage for the instance type
    Double powerUsage = powerUsageData.get(machineType);
    if (powerUsage == null) {
        // Handle the case where the machine type is not found
        powerUsage = 0.1; // Default value
    }

    return powerUsage;
}

private Map<String, Double> loadCarbonIntensityData() {
    // Load carbon intensity data from a file or database
    // This is just an example, you'll need to implement this
    Map<String, Double> carbonIntensityData = new HashMap<>();
    carbonIntensityData.put("us-central1", 0.4);
    carbonIntensityData.put("europe-west1", 0.2);
    return carbonIntensityData;
}

private Map<String, Double> loadPowerUsageData() {
    // Load power usage data from a file or database
    // This is just an example, you'll need to implement this
    Map<String, Double> powerUsageData = new HashMap<>();
    powerUsageData.put("e2-medium", 0.07);
    powerUsageData.put("e2-small", 0.03);
    return powerUsageData;
}

// Helper method to get all available regions
private List<String> getAllRegions() throws IOException {
    List<String> regions = new ArrayList<>();
    try (RegionsClient regionsClient = RegionsClient.create()) {
        for (Region region : regionsClient.list("custom-healer-458206-t8").iterateAll()) {
            regions.add(region.getName());
        }
    }
    return regions;
}

// Helper method to get all zones for a given region
private List<String> getAllZonesForRegion(String region) throws IOException {
    List<String> zones = new ArrayList<>();
    try (ZonesClient zonesClient = ZonesClient.create()) {
        for (Zone zone : zonesClient.list("custom-healer-458206-t8").iterateAll()) {
            if (zone.getName().startsWith(region)) {
                zones.add(zone.getName());
            }
        }
    }
    return zones;
}

    
}

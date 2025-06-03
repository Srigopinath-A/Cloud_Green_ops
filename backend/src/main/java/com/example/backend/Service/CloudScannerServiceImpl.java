package com.example.backend.Service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.management.AzureEnvironment;
import com.example.backend.Config.GcpComputeClientFactory;
import com.example.backend.Model.CloudResource;
import com.google.cloud.compute.v1.InstancesClient;
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
    
    try (InstancesClient instancesClient = gcpComputeClientFactory.createInstancesClient()) {
        // List all instances in the specified project and zone
        for (com.google.cloud.compute.v1.Instance instance : 
             instancesClient.list("custom-healer-458206-t8", "us-central1-c").iterateAll()) {
            
            try {
                // Extract region from zone (remove the last part after '-')
                String zone = instance.getZone();
                String region = zone.contains("-") 
                    ? zone.substring(0, zone.lastIndexOf("-")) 
                    : zone;

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
            } finally {
                // Add any cleanup code if necessary
            }
        }
        
public double getOverallCpuUsage(String projectId) throws Exception {
    double totalCpuUsage = 0.0;

    try (MetricServiceClient metricServiceClient = MetricServiceClient.create()) {
        // Define the project name
        ProjectName projectName = ProjectName.of(projectId);

        // Define the time interval (last 5 minutes)
        long now = System.currentTimeMillis();
        TimeInterval timeInterval = TimeInterval.newBuilder()
            .setStartTime(Timestamps.fromMillis(now - 5 * 60 * 1000)) // 5 minutes ago
            .setEndTime(Timestamps.fromMillis(now)) // Now
            .build();

        // Define the metric type for CPU usage
        String metricType = "compute.googleapis.com/instance/cpu/utilization";

        // Define the aggregation (average CPU usage across instances)
        Aggregation aggregation = Aggregation.newBuilder()
            .setAlignmentPeriod(com.google.protobuf.Duration.newBuilder().setSeconds(300).build()) // 5 minutes
            .setPerSeriesAligner(Aligner.ALIGN_MEAN)
            .setCrossSeriesReducer(Reducer.REDUCE_SUM)
            .build();

        // Create the request
        ListTimeSeriesRequest request = ListTimeSeriesRequest.newBuilder()
            .setName(projectName.toString())
            .setFilter("metric.type=\"" + metricType + "\"")
            .setInterval(timeInterval)
            .setAggregation(aggregation)
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
    // Calculate based on instance type and region
    return 0.0; // Placeholder
}

    
}

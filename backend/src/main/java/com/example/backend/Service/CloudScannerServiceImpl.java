package com.example.backend.Service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.management.AzureEnvironment;
import com.example.backend.Model.CloudResource;
import com.google.cloud.compute.v1.InstancesClient;
import java.io.IOException;

import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.Reservation;

@Service
public class CloudScannerServiceImpl implements CloudScannerService {

    @Override
    public List<CloudResource> scanAws() {
        List<CloudResource> result = new ArrayList<>();
        try (Ec2Client ec2 = Ec2Client.create()){
            DescribeInstancesResponse response = ec2.describeInstances();
            for(Reservation reservation : response.reservations()){
                for(Instance instance: reservation.instances() ){
                    result.add( new CloudResource(
                    instance.instanceId(),
                    "ec2",
                    "AWS",
                    instance.placement().availabilityZone(),
                    0,
                    0
                    ));
                }
            }   
        }
        return result;
    }

    @Override
    public List<CloudResource> scanAzure() {
        List<CloudResource> results = new ArrayList<>();
    AzureResourceManager azure = AzureResourceManager
        .authenticate(new DefaultAzureCredentialBuilder().build(), new AzureProfile(AzureEnvironment.AZURE))
        .withDefaultSubscription();
    azure.virtualMachines().list().forEach(vm -> {
        results.add(new CloudResource(
            vm.id(),
            "vm",
            "Azure",
            vm.regionName(),
            0, // Needs metrics API for usage
            0  // Carbon data mapping
        ));
    });
    return results;
}

    @Override
    public List<CloudResource> scanGcp() throws IOException {
        List<CloudResource> results = new ArrayList<>();
    try (InstancesClient instancesClient = InstancesClient.create()) {
        // List all instances in all zones of your project
        // You'll need to loop through all zones; this is a simple example
        for (com.google.cloud.compute.v1.Instance instance : instancesClient.list("custom-healer-458206-t8", "us-central1-c").iterateAll()) {
            results.add(new CloudResource(
                String.valueOf(instance.getId()),
                "compute",
                "GCP",
                instance.getZone(),
                0, // Usage needs Stackdriver Monitoring
                0  // Carbon data mapping
            ));
        }
    }
    return results;
    }
    
}

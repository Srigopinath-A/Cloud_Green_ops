package com.example.backend.Config;

import org.springframework.stereotype.Component;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.Ec2ClientBuilder;

@Component
public class AwsEc2ClientFactory {
    private String accesskey;
    private String secertkey;

    private String region;

    public Ec2Client createEc2Client(){
        AwsBasicCredentials awscred = AwsBasicCredentials.create(accesskey, secertkey);
        Ec2ClientBuilder builder = Ec2Client.builder()
        .region(Region.of(region))
        .credentialsProvider(StaticCredentialsProvider.create(awscred));
        return builder.build();
    }
}

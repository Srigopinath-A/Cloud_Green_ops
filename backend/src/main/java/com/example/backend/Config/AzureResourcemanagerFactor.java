package com.example.backend.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;

import jakarta.annotation.PostConstruct;

@Component
public class AzureResourcemanagerFactor {

    @Value("${azure.client-id}")
    private String clientId;

    @Value("${azure.client-secret}")
    private String clientSecret;

    @Value("${azure.tenant-id}")
    private String tenantId;

    @Value("${azure.subscription-id}")
    private String subscriptionId;

    @PostConstruct
    public void verifyConfig() {
        System.out.println("Azure config: " + clientId + ", tenant: " + tenantId + ", subscription: " + subscriptionId);
    }

    @Bean
    public AzureResourceManager azureResourceManager() {
        ClientSecretCredential cred = new ClientSecretCredentialBuilder()
            .clientId(clientId)
            .clientSecret(clientSecret)
            .tenantId(tenantId)
            .build();

        AzureProfile profile = new AzureProfile(tenantId, subscriptionId, AzureEnvironment.AZURE);

        return AzureResourceManager.authenticate(cred, profile).withSubscription(subscriptionId);
    }
}

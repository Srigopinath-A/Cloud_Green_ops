package com.example.backend.Config;

import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.*;
import com.azure.resourcemanager.AzureResourceManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureResourcemanagerFactor {

    @Value("${azure.tenant-id}")
    private String tenantId;

    @Value("${azure.subscription-id}")
    private String subscriptionId;

    @Bean
    public AzureProfile azureProfile() {
        return new AzureProfile(tenantId, subscriptionId, AzureEnvironment.AZURE);
    }

    @Bean
    public AzureResourceManager createAzureResourceManager() {
        return AzureResourceManager.authenticate(
            new DefaultAzureCredentialBuilder().build(),
            new AzureProfile(tenantId, subscriptionId, AzureEnvironment.AZURE)
        ).withDefaultSubscription();
    }

    @Bean
    public InteractiveBrowserCredential interactiveBrowserCredential() {
        return new InteractiveBrowserCredentialBuilder()
                .tenantId(tenantId) // Ensure this is correct
                .clientId("your-client-id") // Required for InteractiveBrowserCredential
                .redirectUrl("http://localhost:8080/login/oauth2/code/") // Optional but recommended
                .build();
    }

    @Bean
    public DeviceCodeCredential deviceCodeCredential() {
        return new DeviceCodeCredentialBuilder()
            .tenantId(tenantId)
            .challengeConsumer(challenge -> {
                // Print the device code message to console
                System.out.println("\n" + challenge.getMessage() + "\n");
            })
            .build();
    }
}
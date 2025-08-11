package com.example.backend.Config;

import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.*;
import com.azure.resourcemanager.AzureResourceManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.azure.core.credential.TokenCredential;


@Configuration
public class AzureResourcemanagerFactor {

    @Value("${azure.tenant-id}")
    private String tenantId;

    @Value("${azure.subscription-id}")
    private String subscriptionId;

    @Value("${azure.client-id}") // Optional: set via env or config
    private String clientId;

    @Value("${azure.client-secret:}") // Optional: set via env or config
    private String clientSecret;

    /**
     * Azure Profile Bean: used for all ResourceManager authentication flows
     */
    @Bean
    public AzureProfile azureProfile() {
        return new AzureProfile(tenantId, subscriptionId, AzureEnvironment.AZURE);
    }

    /**
     * Credential Selection Logic:
     * - If clientId & clientSecret set: use ClientSecretCredential (Service Principal).
     * - Else: use DefaultAzureCredential, which will fall back to Interactive/DeviceCode/CLI for dev.
     */
    @Bean
    public TokenCredential azureCredential() {
        if (clientId != null && !clientId.isEmpty() && clientSecret != null && !clientSecret.isEmpty()) {
            // Service Principal: most common for server workloads
            return new ClientSecretCredentialBuilder()
                    .tenantId(tenantId)
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .build();
        }
        // Dev fallback: Default flow (can prompt browser or use CLI if available)
        return new DefaultAzureCredentialBuilder().build();
    }

    /**
     * Main ResourceManager bean: uses the chosen credential for all Azure SDK calls.
     */
    @Bean
    public AzureResourceManager azureResourceManager(TokenCredential azureCredential, AzureProfile azureProfile) {
        return AzureResourceManager
                .authenticate(azureCredential, azureProfile)
                .withSubscription(subscriptionId);
    }

    /**
     * InteractiveBrowserCredential bean (for manual login flows in local dev/testing)
     * Only required if you want to inject/use it explicitly.
     */
    @Bean
    public InteractiveBrowserCredential interactiveBrowserCredential() {
        return new InteractiveBrowserCredentialBuilder()
                .tenantId(tenantId)
                .clientId(clientId)
                .redirectUrl("http://localhost:8080/login/oauth2/code/") // Optional: set redirect
                .build();
    }

    /**
     * DeviceCodeCredential bean (for manual login in CLI/SSH environments)
     * Only required if you want to inject/use it explicitly.
     */
    @Bean
    public DeviceCodeCredential deviceCodeCredential() {
        return new DeviceCodeCredentialBuilder()
                .tenantId(tenantId)
                .clientId(clientId)
                .challengeConsumer(challenge -> System.out.println("\n" + challenge.getMessage() + "\n"))
                .build();
    }
}

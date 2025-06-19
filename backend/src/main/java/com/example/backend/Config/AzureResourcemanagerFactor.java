package com.example.backend.Config;

import org.springframework.stereotype.Component;

import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;


@Component
public class AzureResourcemanagerFactor {

    // Removed 'static' keyword. This method should now be called on an instance of AzureResourcemanagerFactor.
    public static AzureResourceManager createAzureResourceManager(String clientId, String clientSecret, String tenantId, String subscriptionId){
        ClientSecretCredential cred = new ClientSecretCredentialBuilder()
        .clientId(clientId)
        .clientSecret(clientSecret)
        .tenantId(tenantId) // Tenant ID is used here for client secret authentication
        .build();

        AzureProfile Profile =  new AzureProfile(tenantId, subscriptionId, AzureEnvironment.AZURE);

        // This line is correct, calling the static authenticate method on AzureResourceManager
        return AzureResourceManager.authenticate(cred, Profile).withSubscription(subscriptionId);
    }
}

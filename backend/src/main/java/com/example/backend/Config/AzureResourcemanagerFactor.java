package com.example.backend.Config;

import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;

public class AzureResourcemanagerFactor {

    private String ClinetID;
    private String clientSecret;
    private String tenantId;
    private String subscriptionId;

    public AzureResourceManager createazureResouceManager(){
        ClientSecretCredential cred = new ClientSecretCredentialBuilder()
        .clientId(ClinetID)
        .clientSecret(clientSecret)
        .tenantId(tenantId)
        .build();

        AzureProfile Profile =  new AzureProfile(tenantId, subscriptionId, AzureEnvironment.AZURE);

        return AzureResourceManager.authenticate(cred, Profile).withSubscription(subscriptionId);

    }
}

export interface BaseMetrics {
    id: string;
    timestamp: string;
    carbonEmissions: number;
    resourceUtilization: number;
    recommendations: string[];
    status: 'success' | 'warning' | 'critical';
    totalCost: number;
}

export interface AWSMetrics extends BaseMetrics {
    ec2Instances: {
        running: number;
        stopped: number;
        totalCost: number;
        utilization: number;
    };
    rdsInstances: {
        active: number;
        idle: number;
        performance: number;
    };
    s3Usage: {
        totalStorage: number;
        accessPatterns: string[];
    };
}

export interface GCPMetrics extends BaseMetrics {
    computeEngine: {
        activeInstances: number;
        cpuUtilization: number;
        memoryUsage: number;
    };
    cloudStorage: {
        bucketCount: number;
        totalSize: number;
    };
    networkUsage: {
        ingress: number;
        egress: number;
        cost: number;
    };
}

export interface AzureMetrics extends BaseMetrics {
    virtualMachines: {
        total: number;
        active: number;
        performanceMetrics: {
            cpu: number;
            memory: number;
            disk: number;
        };
    };
    storage: {
        blobSize: number;
        tableStorage: number;
    };
    services: {
        active: string[];
        resourceGroups: number;
    };
}

export type CloudProvider = 'AWS' | 'Azure' | 'GCP';

export interface ErrorState {
    message: string;
    code?: number;
    details?: string;
}

export interface FilterOptions {
    startDate?: Date;
    endDate?: Date;
    provider?: CloudProvider;
    resourceType?: string;
}
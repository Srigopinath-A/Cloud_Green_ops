import { ScorecardResponse } from "../services/api";

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

export interface GCPMetrics extends ScorecardResponse {
    totalCost: number;
    status: 'success' | 'warning' | 'critical';
    networkUsage: {
        ingress: number;
        egress: number;
        cost: number;
    };
    cloudStorage: {
        bucketCount: number;
        totalSize: number;
    };
    computeEngine: {
        name: string;
        region: string;
        cpuUtilization: number;
        status: string;
        recommendation: string;
    }[];
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


export interface Scorecard {
  score: number;
  week: string;
  recommendations: { recommendation: string }[];
}

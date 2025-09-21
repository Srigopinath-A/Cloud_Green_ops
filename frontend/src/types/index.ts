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

/* ✅ Updated GCPMetrics and Scorecard */
export interface Resource {
    id: string;
    type: string;
    provider: string;
    region: string;
    usage: number;
    carbonfootprint: number;
}

export interface ResourceFinding {
    resource: Resource;
    details: string;
    issueType: string;
}

export interface RemediationPlan {
    finding: ResourceFinding;
    actions: string;
    aiExplanation: string;
}

export interface Scorecard {
    sustainabilityScore: number;
    week: string;
    remediationPlan: RemediationPlan[];
}

export interface GCPMetrics extends Scorecard {
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

/* -------------------- */

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

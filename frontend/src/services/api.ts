const API_BASE_URL = 'http://localhost:8080'; // Update with your backend URL

// 👇 Add your credentials here for local development only
const USERNAME = 'admin';
const PASSWORD = 'admin123';

// 👇 Helper to create the Authorization header
const getAuthHeaders = (): HeadersInit => ({
    'Authorization': `Basic ${btoa(`${USERNAME}:${PASSWORD}`)}`,
    'Content-Type': 'application/json',
});

interface ErrorResponse {
    message: string;
    status: number;
}

async function handleResponse<T>(response: Response): Promise<T> {
    if (!response.ok) {
        const error: ErrorResponse = await response.json();
        throw new Error(error.message || 'An error occurred');
    }
    return response.json();
}

// Base common response interface
export interface ScorecardResponse {
    id: string;
    timestamp: string;
    carbonEmissions: number;
    resourceUtilization: number;
    recommendations: string[];
    totalCost: number;
    status: 'success' | 'warning' | 'critical' | string;
}

// GCP Compute Engine instance details
export interface ComputeEngineInstance {
    name: string;
    region: string;
    cpuUtilization: number;
    status: string;
    recommendation?: string;
}

// GCP Scorecard interface matching your frontend needs
export interface GCPScorecard extends ScorecardResponse {
    networkUsage: {
        ingress: number;
        egress: number;
        cost: number;
    };
    cloudStorage: {
        bucketCount: number;
        totalSize: number;
    };
    computeEngine: ComputeEngineInstance[];
    gcpSpecificMetrics?: {
        computeEngineUsage: number;
    };
}

// AWS and Azure interfaces (can be updated similarly)
export interface AWSScorecard extends ScorecardResponse {
    storage: Record<string, unknown>;
    services: unknown[];
    virtualMachines: unknown;
    rdsInstances: unknown;
    s3Usage: unknown;
    ec2Instances: unknown;
    awsSpecificMetrics?: {
        ec2Utilization: number;
        rdsEfficiency: number;
    };
}

export interface AzureScorecard extends ScorecardResponse {
    services: unknown;
    storage: unknown;
    virtualMachines: unknown;
    azureSpecificMetrics?: {
        vmUtilization: number;
    };
}

// Fetch functions
export const fetchAWSScorecard = async (date?: string): Promise<AWSScorecard> => {
    const dateParam = date ? `?date=${date}` : '';
    const response = await fetch(`${API_BASE_URL}/scorecard/aws${dateParam}`, {
        method: 'GET',
        headers: getAuthHeaders(),
    });
    return handleResponse<AWSScorecard>(response);
};

import { Scorecard } from '../types';

export const fetchGCPScorecard = async (): Promise<Scorecard> => {
  const res = await fetch(`${API_BASE_URL}/scorecard/gcp`, {
    method: 'GET',
    headers: getAuthHeaders()
  });
  return handleResponse<Scorecard>(res);
};


export const fetchAzureScorecard = async (): Promise<AzureScorecard> => {
    const response = await fetch(`${API_BASE_URL}/scorecard/azure`, {
        method: 'GET',
        headers: getAuthHeaders(),
    });
    return handleResponse<AzureScorecard>(response);
};

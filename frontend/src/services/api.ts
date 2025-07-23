const API_BASE_URL = 'http://localhost:8080'; // Update with your backend URL

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

export interface ScorecardResponse {
    id: string;
    timestamp: string;
    carbonEmissions: number;
    resourceUtilization: number;
    recommendations: string[];
    // Add other common fields
}

export interface AWSScorecard extends ScorecardResponse {
    storage: {};
    services: never[];
    virtualMachines: any;
    rdsInstances: any;
    s3Usage: any;
    ec2Instances: any;
    totalCost: number;
    status: string;
    awsSpecificMetrics?: {
        ec2Utilization: number;
        rdsEfficiency: number;
        // Add AWS specific metrics
    };
}

export interface GCPScorecard extends ScorecardResponse {
    totalCost: number;
    status: string;
    networkUsage: any;
    cloudStorage: any;
    computeEngine: any;
    gcpSpecificMetrics?: {
        computeEngineUsage: number;
        // Add GCP specific metrics
    };
}

export interface AzureScorecard extends ScorecardResponse {
    services: any;
    storage: any;
    status: string;
    virtualMachines: any;
    totalCost: number;
    azureSpecificMetrics?: {
        vmUtilization: number;
        // Add Azure specific metrics
    };
}

export const fetchAWSScorecard = async (date?: string): Promise<AWSScorecard> => {
    const dateParam = date ? `?date=${date}` : '';
    const response = await fetch(`${API_BASE_URL}/scorecard/aws${dateParam}`);
    return handleResponse<AWSScorecard>(response);
};

export const fetchGCPScorecard = async (): Promise<GCPScorecard> => {
    const response = await fetch(`${API_BASE_URL}/scorecard/gcp`);
    return handleResponse<GCPScorecard>(response);
};

export const fetchAzureScorecard = async (): Promise<AzureScorecard> => {
    const response = await fetch(`${API_BASE_URL}/scorecard/azure`);
    return handleResponse<AzureScorecard>(response);
};
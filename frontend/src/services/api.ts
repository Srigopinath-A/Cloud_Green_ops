// Hardcoded backend URL & credentials
const API_BASE_URL = "http://localhost:8080";
const USERNAME = "admin";
const PASSWORD = "admin123";

// Add Basic Auth header
const getAuthHeaders = (): HeadersInit => ({
  "Content-Type": "application/json",
  "Authorization": `Basic ${btoa(`${USERNAME}:${PASSWORD}`)}`
});

interface ErrorResponse {
  message: string;
  status: number;
}

async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    try {
      const error: ErrorResponse = await response.json();
      throw new Error(error.message || "An error occurred");
    } catch {
      throw new Error(`HTTP error: ${response.status}`);
    }
  }
  return response.json();
}

// Common Scorecard interface
export interface ScorecardResponse {
  id: string;
  timestamp: string;
  carbonEmissions: number;
  resourceUtilization: number;
  recommendations: string[];
  totalCost: number;
  status: "success" | "warning" | "critical" | string;
}

export interface ComputeEngineInstance {
  name: string;
  region: string;
  cpuUtilization: number;
  status: string;
  recommendation?: string;
}

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
  const dateParam = date ? `?date=${date}` : "";
  const response = await fetch(`${API_BASE_URL}/scorecard/aws${dateParam}`, {
    method: "GET",
    headers: getAuthHeaders(),
  });
  return handleResponse<AWSScorecard>(response);
};

export const fetchGCPScorecard = async (): Promise<GCPScorecard> => {
  const response = await fetch(`${API_BASE_URL}/scorecard/gcp`, {
    method: "GET",
    headers: getAuthHeaders(),
  });
  return handleResponse<GCPScorecard>(response);
};

export const fetchAzureScorecard = async (): Promise<AzureScorecard> => {
  const response = await fetch(`${API_BASE_URL}/scorecard/azure`, {
    method: "GET",
    headers: getAuthHeaders(),
  });
  return handleResponse<AzureScorecard>(response);
};

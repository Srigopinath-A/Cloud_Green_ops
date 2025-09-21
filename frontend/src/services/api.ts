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

// =======================
// ✅ Common Scorecard interface (AWS & Azure still use this)
// =======================
export interface ScorecardResponse {
  id: string;
  timestamp: string;
  carbonEmissions: number;
  resourceUtilization: number;
  recommendations: string[];
  totalCost: number;
  status: "success" | "warning" | "critical" | string;
}

// =======================
// ✅ GCP Scorecard (customized to backend JSON)
// =======================
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

export interface GCPScorecardResponse {
  sustainabilityScore: number;
  week: string;
  remediationPlan: RemediationPlan[];
}

// =======================
// ✅ AWS & Azure remain unchanged
// =======================
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

// =======================
// ✅ Fetch functions
// =======================
export const fetchAWSScorecard = async (date?: string): Promise<AWSScorecard> => {
  const dateParam = date ? `?date=${date}` : "";
  const response = await fetch(`${API_BASE_URL}/scorecard/aws${dateParam}`, {
    method: "GET",
    headers: getAuthHeaders(),
  });
  return handleResponse<AWSScorecard>(response);
};

export const fetchGCPScorecard = async (): Promise<GCPScorecardResponse> => {
  const response = await fetch(`${API_BASE_URL}/scorecard/gcp`, {
    method: "GET",
    headers: getAuthHeaders(),
  });
  return handleResponse<GCPScorecardResponse>(response);
};

export const fetchAzureScorecard = async (): Promise<AzureScorecard> => {
  const response = await fetch(`${API_BASE_URL}/scorecard/azure`, {
    method: "GET",
    headers: getAuthHeaders(),
  });
  return handleResponse<AzureScorecard>(response);
};

import React, { useEffect, useState } from 'react';
import { fetchGCPScorecard } from '../../services/api';
import { GCPMetrics, ErrorState } from '../../types';

const GCPScorecard: React.FC = () => {
    const [scorecard, setScorecard] = useState<GCPMetrics | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<ErrorState | null>(null);

    useEffect(() => {
        const getScorecard = async () => {
            try {
                const data = await fetchGCPScorecard();
                // Type validation and transformation
                const transformedData: GCPMetrics = {
                    ...data,
                    computeEngine: {
                        activeInstances: data.computeEngine?.activeInstances ?? 0,
                        cpuUtilization: data.computeEngine?.cpuUtilization ?? 0,
                        memoryUsage: data.computeEngine?.memoryUsage ?? 0
                    },
                    cloudStorage: {
                        bucketCount: data.cloudStorage?.bucketCount ?? 0,
                        totalSize: data.cloudStorage?.totalSize ?? 0
                    },
                    networkUsage: {
                        ingress: data.networkUsage?.ingress ?? 0,
                        egress: data.networkUsage?.egress ?? 0,
                        cost: data.networkUsage?.cost ?? 0
                    },
                    // Ensure all required fields from BaseMetrics are present
                    id: data.id ?? '',
                    timestamp: data.timestamp ?? new Date().toISOString(),
                    carbonEmissions: data.carbonEmissions ?? 0,
                    resourceUtilization: data.resourceUtilization ?? 0,
                    recommendations: data.recommendations ?? [],
                    status: ['success', 'warning', 'critical'].includes(data.status) ? data.status as 'success' | 'warning' | 'critical' : 'warning',
                    totalCost: data.totalCost ?? 0
                };
                setScorecard(transformedData);
            } catch (err) {
                setError({
                    message: 'Failed to fetch GCP scorecard data',
                    details: err instanceof Error ? err.message : 'Unknown error'
                });
            } finally {
                setLoading(false);
            }
        };

        getScorecard();
    }, []);

    if (loading) return <div className="loading-state">Loading GCP metrics...</div>;
    if (error) return <div className="error-state">{error.message}</div>;

    return (
        <div className="gcp-scorecard">
            <h2>GCP Scorecard</h2>
            {scorecard && (
                <div className="metrics-container">
                    <div className="metric-group">
                        <h3>General Metrics</h3>
                        <div className="metric-item">
                            <label>Carbon Emissions:</label>
                            <span>{scorecard.carbonEmissions} CO2e</span>
                        </div>
                        <div className="metric-item">
                            <label>Resource Utilization:</label>
                            <span>{scorecard.resourceUtilization}%</span>
                        </div>
                    </div>

                    <div className="metric-group">
                        <h3>Compute Engine</h3>
                        <div className="metric-item">
                            <label>Active Instances:</label>
                            <span>{scorecard.computeEngine.activeInstances}</span>
                        </div>
                        <div className="metric-item">
                            <label>CPU Utilization:</label>
                            <span>{scorecard.computeEngine.cpuUtilization}%</span>
                        </div>
                    </div>

                    <div className="metric-group">
                        <h3>Recommendations</h3>
                        <ul className="recommendations-list">
                            {scorecard.recommendations.map((rec, index) => (
                                <li key={index}>{rec}</li>
                            ))}
                        </ul>
                    </div>
                </div>
            )}
        </div>
    );
};

export default GCPScorecard;
import React, { useEffect, useState } from 'react';
import { fetchAWSScorecard, fetchAzureScorecard } from '../../services/api';
import { AWSMetrics, AzureMetrics, ErrorState } from '../../types';

const AzureScorecard: React.FC = () => {
    const [scorecard, setScorecard] = useState<AzureMetrics | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<ErrorState | null>(null);

    useEffect(() => {
        const getScorecard = async () => {
            try {
                const data = await fetchAzureScorecard();
                // Transform and validate data
                const transformedData: AzureMetrics = {
                    id: data.id ?? '',
                    timestamp: data.timestamp ?? new Date().toISOString(),
                    carbonEmissions: data.carbonEmissions ?? 0,
                    resourceUtilization: data.resourceUtilization ?? 0,
                    recommendations: data.recommendations ?? [],
                    status: ['success', 'warning', 'critical'].includes(data.status) ? data.status as 'success' | 'warning' | 'critical' : 'warning',
                    totalCost: data.totalCost ?? 0,
                    virtualMachines: {
                        total: data.virtualMachines?.total ?? 0,
                        active: data.virtualMachines?.active ?? 0,
                        performanceMetrics: {
                            cpu: data.virtualMachines?.performanceMetrics?.cpu ?? 0,
                            memory: data.virtualMachines?.performanceMetrics?.memory ?? 0,
                            disk: data.virtualMachines?.performanceMetrics?.disk ?? 0
                        }
                    },
                    storage: {
                        blobSize: data.storage?.blobSize ?? 0,
                        tableStorage: data.storage?.tableStorage ?? 0
                    },
                    services: {
                        active: data.services?.active ?? [],
                        resourceGroups: data.services?.resourceGroups ?? 0
                    }
                };
                setScorecard(transformedData);
            } catch (err) {
                setError({
                    message: 'Failed to fetch Azure scorecard data',
                    details: err instanceof Error ? err.message : 'Unknown error'
                });
            } finally {
                setLoading(false);
            }
        };

        getScorecard();
    }, []);

    if (loading) return <div className="loading-state">Loading Azure metrics...</div>;
    if (error) return <div className="error-state">{error.message}</div>;

    return (
        <div className="azure-scorecard">
            <h2>Azure Scorecard</h2>
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
                        <h3>Virtual Machines</h3>
                        <div className="metric-item">
                            <label>Total VMs:</label>
                            <span>{scorecard.virtualMachines.total}</span>
                        </div>
                        <div className="metric-item">
                            <label>Active VMs:</label>
                            <span>{scorecard.virtualMachines.active}</span>
                        </div>
                        <div className="metric-item">
                            <label>CPU Usage:</label>
                            <span>{scorecard.virtualMachines.performanceMetrics.cpu}%</span>
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

export default AzureScorecard;
// src/components/AzureScorecard.tsx

import React, { useEffect, useState } from 'react';
import { fetchAzureScorecard } from '../../services/api';
import { AzureMetrics, ErrorState } from '../../types';

const AzureScorecard: React.FC = () => {
    const [scorecard, setScorecard] = useState<AzureMetrics | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<ErrorState | null>(null);

    useEffect(() => {
        const getScorecard = async () => {
            try {
                const data = await fetchAzureScorecard();
                setScorecard(data);
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
                        <p>Carbon Emissions: {scorecard.carbonEmissions} CO2e</p>
                        <p>Resource Utilization: {scorecard.resourceUtilization}%</p>
                    </div>

                    <div className="metric-group">
                        <h3>Virtual Machines</h3>
                        <p>Total VMs: {scorecard.virtualMachines.total}</p>
                        <p>Active VMs: {scorecard.virtualMachines.active}</p>
                        <p>CPU Usage: {scorecard.virtualMachines.performanceMetrics.cpu}%</p>
                        <p>Memory Usage: {scorecard.virtualMachines.performanceMetrics.memory}%</p>
                        <p>Disk Usage: {scorecard.virtualMachines.performanceMetrics.disk}%</p>
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

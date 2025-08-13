import React, { useEffect, useState } from 'react';
import { fetchAWSScorecard } from '../../services/api';
import { AWSMetrics, ErrorState } from '../../types';

const AWSScorecard: React.FC = () => {
    const [scorecard, setScorecard] = useState<AWSMetrics | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<ErrorState | null>(null);

    useEffect(() => {
        const getScorecard = async () => {
            try {
                const data = await fetchAWSScorecard();
                // Transform and validate data
                const transformedData: AWSMetrics = {
                    id: data.id ?? '',
                    timestamp: data.timestamp ?? new Date().toISOString(),
                    carbonEmissions: data.carbonEmissions ?? 0,
                    resourceUtilization: data.resourceUtilization ?? 0,
                    recommendations: data.recommendations ?? [],
                    status: ['success', 'warning', 'critical'].includes(data.status) ? data.status as 'success' | 'warning' | 'critical' : 'warning',
                    totalCost: data.totalCost ?? 0,
                    ec2Instances: {
                        running: data.ec2Instances && typeof data.ec2Instances === 'object' && 'running' in data.ec2Instances ? Number(data.ec2Instances.running) : 0,
                        stopped: data.ec2Instances && typeof data.ec2Instances === 'object' && 'stopped' in data.ec2Instances ? Number(data.ec2Instances.stopped) : 0,
                        totalCost: data.ec2Instances && typeof data.ec2Instances === 'object' && 'totalCost' in data.ec2Instances ? Number(data.ec2Instances.totalCost) : 0,
                        utilization: data.ec2Instances && typeof data.ec2Instances === 'object' && 'utilization' in data.ec2Instances ? Number(data.ec2Instances.utilization) : 0
                    },
                    rdsInstances: {
                        active: data.rdsInstances && typeof data.rdsInstances === 'object' && 'active' in data.rdsInstances ? Number((data.rdsInstances as any).active) : 0,
                        idle: data.rdsInstances && typeof data.rdsInstances === 'object' && 'idle' in data.rdsInstances ? Number((data.rdsInstances as any).idle) : 0,
                        performance: data.rdsInstances && typeof data.rdsInstances === 'object' && 'performance' in data.rdsInstances ? Number((data.rdsInstances as any).performance) : 0
                    },
                    s3Usage: {
                        totalStorage: data.s3Usage && typeof data.s3Usage === 'object' && 'totalStorage' in data.s3Usage ? Number((data.s3Usage as any).totalStorage) : 0,
                        accessPatterns: data.s3Usage && typeof data.s3Usage === 'object' && 'accessPatterns' in data.s3Usage ? (data.s3Usage as any).accessPatterns : []
                    }
                };
                setScorecard(transformedData);
            } catch (err) {
                setError({
                    message: 'Failed to fetch AWS scorecard data',
                    details: err instanceof Error ? err.message : 'Unknown error'
                });
            } finally {
                setLoading(false);
            }
        };

        getScorecard();
    }, []);

    if (loading) return <div className="loading-state">Loading AWS metrics...</div>;
    if (error) return <div className="error-state">{error.message}</div>;

    return (
        <div className="aws-scorecard">
            <h2>AWS Scorecard</h2>
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
                        <h3>EC2 Instances</h3>
                        <div className="metric-item">
                            <label>Running:</label>
                            <span>{scorecard.ec2Instances.running}</span>
                        </div>
                        <div className="metric-item">
                            <label>Stopped:</label>
                            <span>{scorecard.ec2Instances.stopped}</span>
                        </div>
                        <div className="metric-item">
                            <label>Utilization:</label>
                            <span>{scorecard.ec2Instances.utilization}%</span>
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

export default AWSScorecard;
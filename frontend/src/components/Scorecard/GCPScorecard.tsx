import React, { useEffect, useState } from 'react';
import { fetchGCPScorecard } from '../../services/api';
import { Scorecard, RemediationPlan } from '../../types';

const GCPScorecard: React.FC = () => {
  const [scorecard, setScorecard] = useState<Scorecard | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const getScorecard = async () => {
      try {
        const data = await fetchGCPScorecard();
        setScorecard(data);
      } catch {
        setError('Failed to fetch GCP scorecard');
      } finally {
        setLoading(false);
      }
    };
    getScorecard();
  }, []);

  if (loading) return <div>Loading GCP Scorecard...</div>;
  if (error) return <div className="error">{error}</div>;

  return (
    <div className="gcp-scorecard">
      <h2>GCP Scorecard</h2>
      {scorecard && (
        <>
          <p><strong>Sustainability Score:</strong> {scorecard.sustainabilityScore}</p>
          <p><strong>Week:</strong> {scorecard.week}</p>
          <h3>Remediation Plan</h3>
          <ul>
            {scorecard.remediationPlan.length > 0 ? (
              scorecard.remediationPlan.map((plan: RemediationPlan, i: number) => (
                <li key={i}>
                  <strong>Action:</strong> {plan.actions} <br />
                  <strong>AI Explanation:</strong> {plan.aiExplanation} <br />
                  <strong>Resource ID:</strong> {plan.finding.resource.id} <br />
                  <strong>Resource Type:</strong> {plan.finding.resource.type} <br />
                  <strong>Provider:</strong> {plan.finding.resource.provider} <br />
                  <strong>Region:</strong> {plan.finding.resource.region} <br />
                  <strong>Usage:</strong> {plan.finding.resource.usage}% <br />
                  <strong>Carbon Footprint:</strong> {plan.finding.resource.carbonfootprint} <br />
                  <strong>Details:</strong> {plan.finding.details} <br />
                  <strong>Issue Type:</strong> {plan.finding.issueType}
                </li>
              ))
            ) : (
              <li>No remediation plans</li>
            )}
          </ul>
        </>
      )}
    </div>
  );
};

export default GCPScorecard;

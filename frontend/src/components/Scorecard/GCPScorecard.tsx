import React, { useEffect, useState } from 'react';
import { fetchGCPScorecard } from '../../services/api';
import { ErrorState } from '../../types';
import { Scorecard } from '../../types'; // ✅ Make sure this matches the backend shape

const GCPScorecard: React.FC = () => {
  const [scorecard, setScorecard] = useState<Scorecard | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<ErrorState | null>(null);

  useEffect(() => {
    const getScorecard = async () => {
      try {
        const data = await fetchGCPScorecard(); // returns score, week, recommendations
        setScorecard(data); // ✅ Now matches
      } catch (err) {
        setError({ message: 'Failed to fetch GCP scorecard' });
      } finally {
        setLoading(false);
      }
    };
    getScorecard();
  }, []);



  if (loading) return <div>Loading GCP Scorecard...</div>;
  if (error) return <div className="error">{error.message}</div>;

  return (
    <div className="gcp-scorecard">
      <h2>GCP Scorecard</h2>
      {scorecard && (
        <>
          <p><strong>Score:</strong> {scorecard.score}</p>
          <p><strong>Week:</strong> {scorecard.week}</p>
          <h3>Recommendations</h3>
          <ul>
            {scorecard.recommendations.length ? (
              scorecard.recommendations.map((r, i) => (
                <li key={i}>{r.recommendation}</li>
              ))
            ) : (
              <li>No recommendations</li>
            )}
          </ul>
        </>
      )}
    </div>
  );
};

export default GCPScorecard;

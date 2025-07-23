import React, { useState } from 'react';
import AWSScorecard from './AWSScorecard';
import AzureScorecard from './AzureScorecard';
import GCPScorecard from './GCPScorecard';

export { AWSScorecard, AzureScorecard, GCPScorecard };

type CloudProvider = 'AWS' | 'Azure' | 'GCP';

const ScorecardDashboard: React.FC = () => {
    const [activeProvider, setActiveProvider] = useState<CloudProvider>('AWS');

    const renderScorecard = () => {
        switch (activeProvider) {
            case 'AWS':
                return <AWSScorecard />;
            case 'Azure':
                return <AzureScorecard />;
            case 'GCP':
                return <GCPScorecard />;
            default:
                return null;
        }
    };

    return (
        <div className="scorecard-dashboard">
            <div className="provider-selector">
                <button 
                    className={`provider-btn ${activeProvider === 'AWS' ? 'active' : ''}`}
                    onClick={() => setActiveProvider('AWS')}
                >
                    AWS
                </button>
                <button 
                    className={`provider-btn ${activeProvider === 'Azure' ? 'active' : ''}`}
                    onClick={() => setActiveProvider('Azure')}
                >
                    Azure
                </button>
                <button 
                    className={`provider-btn ${activeProvider === 'GCP' ? 'active' : ''}`}
                    onClick={() => setActiveProvider('GCP')}
                >
                    GCP
                </button>
            </div>
            <div className="scorecard-content">
                {renderScorecard()}
            </div>
        </div>
    );
};

export default ScorecardDashboard;
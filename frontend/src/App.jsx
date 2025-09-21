import React from 'react'
import ScorecardDashboard from './components/Scorecard'
import './App.css'

function App() {
  return (
    <div className="app-container">
      <header className="app-header">
        <h1>Cloud Green Ops Dashboard</h1>
        <p>Monitor and optimize your cloud resource efficiency</p>
      </header> 
      
      <main className="app-main">
        <ScorecardDashboard />
      </main>

      <footer className="app-footer">
        <p>© 2025 Cloud Green Ops. All rights reserved.</p>
      </footer>
    </div>
  )
}

export default App
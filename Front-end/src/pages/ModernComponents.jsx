import React, { useState } from 'react';
import { useToast } from '../hooks/useToast';
import ToastContainer from '../components/ToastContainer';
import SearchFilter from '../components/SearchFilter';
import SortableTable from '../components/SortableTable';
import '../styles/modern-improvements.css';

const ModernComponents = () => {
  const { toasts, removeToast, success, error, warning, info } = useToast();
  const [searchValue, setSearchValue] = useState('');
  
  const sampleData = [
    { id: 1, name: 'Jean Dupont', email: 'jean@example.com', role: 'Student', score: 85 },
    { id: 2, name: 'Marie Martin', email: 'marie@example.com', role: 'Admin', score: 92 },
    { id: 3, name: 'Pierre Durand', email: 'pierre@example.com', role: 'Student', score: 78 },
    { id: 4, name: 'Sophie Bernard', email: 'sophie@example.com', role: 'Supervisor', score: 95 },
    { id: 5, name: 'Luc Petit', email: 'luc@example.com', role: 'Student', score: 67 },
  ];

  const columns = [
    { key: 'id', label: 'ID', sortable: true },
    { key: 'name', label: 'Name', sortable: true },
    { key: 'email', label: 'Email', sortable: true },
    { key: 'role', label: 'Role', sortable: true },
    { 
      key: 'score', 
      label: 'Score', 
      sortable: true,
      render: (value) => (
        <span className={value >= 80 ? 'badge badge-success' : value >= 60 ? 'badge badge-warning' : 'badge badge-error'}>
          {value}
        </span>
      )
    },
  ];

  return (
    <div className="dashboard-container">
      <div className="dashboard-header">
        <h1 className="dashboard-title">
          Modern <span>Components</span>
        </h1>
        <p style={{ color: 'var(--text-secondary)', textAlign: 'center' }}>
          Guide des nouveaux composants modernes avec animations fluides
        </p>
      </div>

      <ToastContainer toasts={toasts} removeToast={removeToast} />

      {/* Toast Examples */}
      <section className="modern-card" style={{ marginBottom: 'var(--spacing-xl)' }}>
        <h2 style={{ color: 'var(--accent-cyan)', marginBottom: 'var(--spacing-md)' }}>
          Toast Notifications
        </h2>
        <p style={{ color: 'var(--text-secondary)', marginBottom: 'var(--spacing-lg)' }}>
          Notifications animées avec 4 types : success, error, warning, info
        </p>
        <div style={{ display: 'flex', gap: 'var(--spacing-md)', flexWrap: 'wrap' }}>
          <button className="btn-modern" onClick={() => success('Operation completed successfully!')}>
            <span>Show Success</span>
          </button>
          <button className="btn-modern" onClick={() => error('An error occurred!')}>
            <span>Show Error</span>
          </button>
          <button className="btn-modern" onClick={() => warning('Warning: Check your input!')}>
            <span>Show Warning</span>
          </button>
          <button className="btn-modern" onClick={() => info('Information: New update available')}>
            <span>Show Info</span>
          </button>
        </div>
      </section>

      {/* Search Filter */}
      <section className="modern-card" style={{ marginBottom: 'var(--spacing-xl)' }}>
        <h2 style={{ color: 'var(--accent-cyan)', marginBottom: 'var(--spacing-md)' }}>
          Search Filter
        </h2>
        <p style={{ color: 'var(--text-secondary)', marginBottom: 'var(--spacing-lg)' }}>
          Champ de recherche moderne avec autocomplétion
        </p>
        <SearchFilter
          placeholder="Search by name..."
          value={searchValue}
          onChange={setSearchValue}
          suggestions={sampleData.map(d => d.name)}
          onSuggestionClick={setSearchValue}
        />
        <p style={{ marginTop: 'var(--spacing-md)', color: 'var(--text-primary)' }}>
          Current search: <strong>{searchValue || 'None'}</strong>
        </p>
      </section>

      {/* Sortable Table */}
      <section className="modern-card" style={{ marginBottom: 'var(--spacing-xl)' }}>
        <h2 style={{ color: 'var(--accent-cyan)', marginBottom: 'var(--spacing-md)' }}>
          Sortable Table
        </h2>
        <p style={{ color: 'var(--text-secondary)', marginBottom: 'var(--spacing-lg)' }}>
          Tableau avec tri et pagination automatiques
        </p>
        <SortableTable
          columns={columns}
          data={sampleData}
          itemsPerPage={3}
        />
      </section>

      {/* Badges */}
      <section className="modern-card" style={{ marginBottom: 'var(--spacing-xl)' }}>
        <h2 style={{ color: 'var(--accent-cyan)', marginBottom: 'var(--spacing-md)' }}>
          Badges
        </h2>
        <p style={{ color: 'var(--text-secondary)', marginBottom: 'var(--spacing-lg)' }}>
          Badges colorés pour statuts et catégories
        </p>
        <div style={{ display: 'flex', gap: 'var(--spacing-md)', flexWrap: 'wrap' }}>
          <span className="badge badge-success">Success</span>
          <span className="badge badge-error">Error</span>
          <span className="badge badge-warning">Warning</span>
          <span className="badge badge-info">Info</span>
        </div>
      </section>

      {/* Alerts */}
      <section className="modern-card" style={{ marginBottom: 'var(--spacing-xl)' }}>
        <h2 style={{ color: 'var(--accent-cyan)', marginBottom: 'var(--spacing-md)' }}>
          Animated Alerts
        </h2>
        <p style={{ color: 'var(--text-secondary)', marginBottom: 'var(--spacing-lg)' }}>
          Alertes animées pour messages importants
        </p>
        
        <div className="alert-success animated-alert">
          <span className="alert-icon">✓</span>
          <span className="alert-message">Success! Your changes have been saved.</span>
        </div>

        <div className="alert-warning animated-alert">
          <span className="alert-icon">⚠️</span>
          <span className="alert-message">Warning: The deadline is approaching!</span>
        </div>

        <div className="alert-error animated-alert">
          <span className="alert-icon">✕</span>
          <span className="alert-message">Error: Failed to connect to server.</span>
        </div>

        <div className="alert-info animated-alert">
          <span className="alert-icon">ℹ</span>
          <span className="alert-message">Info: New features are now available.</span>
        </div>
      </section>

      {/* Buttons & Inputs */}
      <section className="modern-card" style={{ marginBottom: 'var(--spacing-xl)' }}>
        <h2 style={{ color: 'var(--accent-cyan)', marginBottom: 'var(--spacing-md)' }}>
          Modern Buttons & Inputs
        </h2>
        <p style={{ color: 'var(--text-secondary)', marginBottom: 'var(--spacing-lg)' }}>
          Boutons et champs de saisie modernisés
        </p>
        
        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--spacing-md)' }}>
          <button className="btn-modern">
            <span>Modern Button</span>
          </button>
          
          <input 
            type="text" 
            className="input-modern" 
            placeholder="Modern input field..."
          />
          
          <textarea 
            className="input-modern" 
            placeholder="Modern textarea..."
            rows="3"
          />
        </div>
      </section>

      {/* Loading States */}
      <section className="modern-card">
        <h2 style={{ color: 'var(--accent-cyan)', marginBottom: 'var(--spacing-md)' }}>
          Loading States
        </h2>
        <p style={{ color: 'var(--text-secondary)', marginBottom: 'var(--spacing-lg)' }}>
          États de chargement avec animations
        </p>
        
        <div style={{ display: 'flex', gap: 'var(--spacing-xl)', alignItems: 'center', flexWrap: 'wrap' }}>
          <div className="loading-spinner"></div>
          <div className="skeleton" style={{ width: '200px', height: '40px' }}></div>
          <div className="skeleton" style={{ width: '150px', height: '40px' }}></div>
        </div>
      </section>

      <div className="divider"></div>

      <div style={{ textAlign: 'center', padding: 'var(--spacing-xl)', color: 'var(--text-secondary)' }}>
        <p>🎨 Tous les composants sont responsive mobile-first et conservent vos couleurs cyan/blue</p>
        <p style={{ marginTop: 'var(--spacing-sm)' }}>
          📱 Testez sur différentes tailles d'écran pour voir l'adaptation responsive
        </p>
      </div>
    </div>
  );
};

export default ModernComponents;

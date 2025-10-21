import React from 'react';
import '../styles/forms.css';

const CompletedPage = ({ onEdit }) => {
  return (
    <div className="contact-card" style={{ textAlign: 'center' }}>
      <h2>Questionnaire Completed</h2>
      <p style={{ color: 'var(--text-muted)', margin: 'var(--spacing-lg) 0' }}>
        You have already submitted your answers for this questionnaire
      </p>
      <button onClick={onEdit} className="codepen-button">
        <span>Modify the answers</span>
      </button>
    </div>
  );
};

export default CompletedPage;

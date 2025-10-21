import React from 'react';
import { Link } from 'react-router-dom';

const LogoutSuccess = () => {
  return (
    <div className="login-container">
      <div className="login-box">
        <h2>Déconnexion réussie</h2>
        <p>Vous avez été déconnecté de votre compte.</p>
        <Link to="/login" className="login-button">
          Se connecter
        </Link>
      </div>
    </div>
  );
};

export default LogoutSuccess;

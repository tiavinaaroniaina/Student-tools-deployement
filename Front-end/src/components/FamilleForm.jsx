import React, { useState } from 'react';
import API_BASE_URL from '../config';
import '../styles/forms.css'; // Importation des styles de formulaire globaux
import '../styles/FamilleForm.css'; // Importation des styles spécifiques

const FamilleForm = ({ user, onFormSubmit }) => {
  const [formData, setFormData] = useState({
    nom: '',
    animateur_nom: '',
    nombre_personnes: 1,
  });
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setError(null);

    try {
      const response = await fetch(`${API_BASE_URL}/api/famille`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ 
          ...formData,
          user_id: user.id
        }),
        credentials: 'include',
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || 'Une erreur est survenue.');
      }

      const newFamille = await response.json();
      onFormSubmit(newFamille);

    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="contact-card famille-form-container">
      <h2>Création de votre dossier famille</h2>
      <p>Veuillez remplir ce formulaire pour commencer le processus de demande de bourse.</p>
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="nom">Nom de la famille</label>
          <input
            type="text"
            id="nom"
            name="nom"
            className="input"
            value={formData.nom}
            onChange={handleChange}
            required
          />
        </div>
        <div className="form-group">
          <label htmlFor="animateur_nom">Nom de l'animateur</label>
          <input
            type="text"
            id="animateur_nom"
            name="animateur_nom"
            className="input"
            value={formData.animateur_nom}
            onChange={handleChange}
          />
        </div>
        <div className="form-group">
          <label htmlFor="nombre_personnes">Nombre de personnes dans le foyer</label>
          <input
            type="number"
            id="nombre_personnes"
            name="nombre_personnes"
            className="input"
            value={formData.nombre_personnes}
            onChange={handleChange}
            min="1"
            required
          />
        </div>
        {error && <p className="error-message">{error}</p>}
        <button type="submit" className="codepen-button" disabled={submitting}>
          <span>{submitting ? 'Enregistrement...' : 'Enregistrer et continuer'}</span>
        </button>
      </form>
    </div>
  );
};

export default FamilleForm;
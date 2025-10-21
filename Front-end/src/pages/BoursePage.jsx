import React, { useState, useEffect } from 'react';
import FamilleForm from '../components/FamilleForm';
import QuestionsPage from './QuestionsPage';
import CompletedPage from './CompletedPage'; // Importer le nouveau composant
import API_BASE_URL from '../config';

const BoursePage = ({ user }) => {
  const [famille, setFamille] = useState(null);
  const [hasCompleted, setHasCompleted] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const checkInitialState = async () => {
      if (!user || !user.id) {
        setError('Informations utilisateur non disponibles.');
        setLoading(false);
        return;
      }

      try {
        // 1. Vérifier si une famille existe
        const familleResponse = await fetch(`${API_BASE_URL}/api/famille/${user.id}`, {
          credentials: 'include',
        });

        if (familleResponse.ok) {
          const familleData = await familleResponse.json();
          setFamille(familleData);

          // 2. Si la famille existe, vérifier si des réponses ont été soumises
          const answersResponse = await fetch(`${API_BASE_URL}/api/famille/${familleData.id}/reponses`, {
            credentials: 'include',
          });

          if (answersResponse.ok) {
            const answersData = await answersResponse.json();
            // Si l'objet de réponses n'est pas vide, l'utilisateur a terminé
            if (Object.keys(answersData).length > 0) {
              setHasCompleted(true);
            }
          }
        } else if (familleResponse.status !== 404) {
          throw new Error('Erreur lors de la récupération du dossier famille.');
        }
        // Si 404, la famille n'existe pas, on affiche le formulaire de création

      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    checkInitialState();
  }, [user]);

  const handleFormSubmit = (newFamille) => {
    setFamille(newFamille);
    // Après la création, l'utilisateur n'a pas encore répondu
    setHasCompleted(false);
  };

  const handleEdit = () => {
    setIsEditing(true);
  };

  if (loading) {
    return <div className="loading-container">Chargement...</div>;
  }

  if (error) {
    return <div className="error-message">{error}</div>;
  }

  // Cas 1: Pas de famille -> Créer la famille
  if (!famille) {
    return <FamilleForm user={user} onFormSubmit={handleFormSubmit} />;
  }

  // Cas 2: Famille existe, a terminé, et n'est pas en mode édition -> Afficher la page "Terminé"
  if (hasCompleted && !isEditing) {
    return <CompletedPage onEdit={handleEdit} />;
  }

  // Cas 3: La famille existe et (n'a pas terminé OU est en mode édition) -> Afficher les questions
  return <QuestionsPage famille={famille} />;
};

export default BoursePage;
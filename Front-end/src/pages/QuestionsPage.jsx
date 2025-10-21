import React, { useState, useEffect } from 'react';
import API_BASE_URL from '../config';
import { useParams } from 'react-router-dom';
import '../styles/forms.css';
import '../styles/QuestionsPage.css';

const QuestionsPage = ({ famille }) => {
  const { categoryType } = useParams();
  const [questions, setQuestions] = useState([]);
  const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);
  const [selectedAnswers, setSelectedAnswers] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [totalScore, setTotalScore] = useState(null);

  useEffect(() => {
    const fetchInitialData = async () => {
      try {
        // 1. Fetch questions
        const questionsResponse = await fetch(`${API_BASE_URL}/api/questions`, {
          credentials: 'include',
        });
        if (!questionsResponse.ok) {
          throw new Error('Impossible de charger les questions.');
        }
        const questionsData = await questionsResponse.json();
        const filteredQuestions = questionsData.filter(q => q.type === categoryType);
        setQuestions(filteredQuestions);

        // 2. Fetch previous answers for the family
        if (famille && famille.id) {
          const answersResponse = await fetch(`${API_BASE_URL}/api/famille/${famille.id}/reponses`, {
            credentials: 'include',
          });
          if (answersResponse.ok) {
            const answersData = await answersResponse.json();
            setSelectedAnswers(answersData);
          }
          // We don't throw an error for 404, it just means no previous answers
        }

      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchInitialData();
  }, [famille]);

  const handleAnswerChange = (questionId, reponseId) => {
    setSelectedAnswers(prev => ({ ...prev, [questionId]: reponseId }));
  };

  const handleNext = () => {
    if (currentQuestionIndex < questions.length - 1) {
      setCurrentQuestionIndex(prev => prev + 1);
    }
  };

  const handlePrevious = () => {
    if (currentQuestionIndex > 0) {
      setCurrentQuestionIndex(prev => prev - 1);
    }
  };

  const handleSubmit = async () => {
    setSubmitting(true);
    setError(null);
    try {
      const reponseIds = Object.values(selectedAnswers);
      const response = await fetch(`${API_BASE_URL}/api/famille/${famille.id}/reponses`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(reponseIds),
        credentials: 'include',
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || 'Erreur lors de la soumission des réponses.');
      }

      const score = await response.json();
      setTotalScore(score);

    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return <div className="contact-card">question loading...</div>;
  }

  if (error) {
    return <div className="contact-card error-message">{error}</div>;
  }

  if (totalScore !== null) {
    return (
      <div className="contact-card score-container">
        <h2>Thank you for replying!!!!!</h2>
      </div>
    );
  }

  if (questions.length === 0) {
    return <div className="contact-card">no question found.</div>;
  }

  const currentQuestion = questions[currentQuestionIndex];
  const isAnswerSelected = selectedAnswers.hasOwnProperty(currentQuestion.id);
  const isLastQuestion = currentQuestionIndex === questions.length - 1;

  return (
    <div className="contact-card questions-container">
      <div className="question-header">
        <h3>Question {currentQuestionIndex + 1} / {questions.length}</h3>
        <h2>{currentQuestion.texte}</h2>
      </div>

      <div className="answers-body">
        {currentQuestion.reponses.map(reponse => (
          <div key={reponse.id} className="answer-option">
            <input
              type="radio"
              id={`reponse-${reponse.id}`}
              name={`question-${currentQuestion.id}`}
              value={reponse.id}
              onChange={() => handleAnswerChange(currentQuestion.id, reponse.id)}
              checked={selectedAnswers[currentQuestion.id] === reponse.id}
            />
            <label htmlFor={`reponse-${reponse.id}`}>{reponse.texte}</label>
          </div>
        ))}
      </div>

      <div className="navigation-footer">
        <button 
          onClick={handlePrevious} 
          disabled={currentQuestionIndex === 0}
          className="codepen-button"
        >
          <span>Previous</span>
        </button>
        
        {isLastQuestion ? (
          <button 
            onClick={handleSubmit} 
            disabled={!isAnswerSelected || submitting}
            className="codepen-button"
          >
            <span>{submitting ? 'Envoi...' : 'Terminer'}</span>
          </button>
        ) : (
          <button 
            onClick={handleNext} 
            disabled={!isAnswerSelected}
            className="codepen-button"
          >
            <span>Next</span>
          </button>
        )}
      </div>
    </div>
  );
};

export default QuestionsPage;
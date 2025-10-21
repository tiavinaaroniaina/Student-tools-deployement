import React from 'react';
import { useNavigate } from 'react-router-dom';
import '../../src/styles/CategorySelection.css'; // We will create this CSS file

const CategorySelectionPage = () => {
  const navigate = useNavigate();

  const handleCategoryClick = (categoryType) => {
    navigate(`/app/questions/${categoryType}`);
  };

  return (
    <div className="category-selection-container">
      <h1>Sélectionnez une catégorie de questions</h1>
      <div className="category-cards-wrapper">
        <div className="category-card" onClick={() => handleCategoryClick('bourse')}>
          <h2>Bourse</h2>
          <p>Questions relatives aux bourses et aides financières.</p>
        </div>
        <div className="category-card" onClick={() => handleCategoryClick('projet')}>
          <h2>Projet</h2>
          <p>Questions relatives aux projets et travaux académiques.</p>
        </div>
      </div>
    </div>
  );
};

export default CategorySelectionPage;
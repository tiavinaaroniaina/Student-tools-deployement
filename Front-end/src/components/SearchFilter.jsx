import React, { useState } from 'react';
import '../styles/search-filter.css';

const SearchFilter = ({ 
  placeholder = "Search...", 
  value, 
  onChange, 
  suggestions = [], 
  onSuggestionClick,
  onFilterClick 
}) => {
  const [showSuggestions, setShowSuggestions] = useState(false);

  return (
    <div className="search-filter">
      <div className="search-input-wrapper">
        <i className="fas fa-search search-icon"></i>
        <input
          type="text"
          className="search-input"
          placeholder={placeholder}
          value={value}
          onChange={(e) => {
            onChange(e.target.value);
            setShowSuggestions(true);
          }}
          onFocus={() => setShowSuggestions(true)}
          onBlur={() => setTimeout(() => setShowSuggestions(false), 200)}
          autoComplete="off"
        />
        {value && (
          <button 
            className="search-clear" 
            onClick={() => {
              onChange('');
              setShowSuggestions(false);
            }}
            aria-label="Clear"
          >
            <i className="fas fa-times"></i>
          </button>
        )}
        {onFilterClick && (
          <button 
            className="filter-icon-button" 
            onClick={onFilterClick}
            aria-label="Filter"
          >
            <i className="fas fa-filter"></i>
          </button>
        )}
      </div>
      {showSuggestions && suggestions.length > 0 && (
        <ul className="suggestions-dropdown">
          {suggestions.map((suggestion, index) => (
            <li 
              key={index}
              onMouseDown={() => {
                onSuggestionClick(suggestion);
                setShowSuggestions(false);
              }}
              className="suggestion-item"
            >
              {suggestion}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
};

export default SearchFilter;

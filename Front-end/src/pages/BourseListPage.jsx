import React, { useState, useEffect } from 'react';
import '../styles/BourseListPage.css';
import '../styles/modern-improvements.css';
import API_BASE_URL from '../config.js';
import ErrorPopup from '../components/ErrorPopup.jsx';
import SearchFilter from '../components/SearchFilter.jsx';
import SortableTable from '../components/SortableTable.jsx';

class ResponseStatusException extends Error {
  constructor(status, message) {
    super(message);
    this.status = status;
  }
}

const ITEMS_PER_PAGE = 10;

const BourseListPage = () => {
  const [bourseFamilies, setBourseFamilies] = useState([]);
  const [categoryFamilies, setCategoryFamilies] = useState([]);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);
  const [filterLogin, setFilterLogin] = useState("");
  const [selectedCategory, setSelectedCategory] = useState(null);
  const [showMostActive, setShowMostActive] = useState(false);

  const fetchBourseFamilies = async () => {
    try {
      const res = await fetch(`${API_BASE_URL}/api/bourse/elligibles`, { credentials: 'include' });
      if (!res.ok) throw new ResponseStatusException(res.status, "Error fetching bourse families");
      return await res.json();
    } catch (err) {
      console.error("Erreur lors de la récupération des familles pour la bourse:", err);
      setError(err.message || "Failed to fetch bourse families.");
      if (err.status === 401) setTimeout(() => { window.location.href = `${API_BASE_URL}/login`; }, 2000);
      return [];
    }
  };

  const fetchCategoryFamilies = async () => {
    try {
      const res = await fetch(`${API_BASE_URL}/api/bourse/elligibles_par_categorie`, { credentials: 'include' });
      if (!res.ok) throw new ResponseStatusException(res.status, "Error fetching category families");
      return await res.json();
    } catch (err) {
      console.error("Erreur lors de la récupération des familles par catégorie:", err);
      // Do not set a global error for this fetch, as it's a secondary data source
      return [];
    }
  };

  useEffect(() => {
    const fetchAllData = async () => {
      setLoading(true);
      const [bourseData, categoryData] = await Promise.all([
        fetchBourseFamilies(),
        fetchCategoryFamilies(),
      ]);
      setBourseFamilies(bourseData);
      setCategoryFamilies(categoryData);
      setLoading(false);
    };
    fetchAllData();
  }, []);

  const handleCategoryClick = (category) => {
    setSelectedCategory(category);
    setShowMostActive(false);
  };

  const handleMostActiveClick = () => {
    setShowMostActive(true);
    setSelectedCategory(null);
  };

  const dataToShow = showMostActive
    ? []
    : selectedCategory
    ? categoryFamilies.filter(family => family.categorie_nom.toUpperCase() === selectedCategory)
    : bourseFamilies;

  const filteredFamilies = filterLogin
    ? dataToShow.filter(family =>
        (family.nom_famille && family.nom_famille.toLowerCase().includes(filterLogin.toLowerCase())) ||
        (family.login_utilisateur && family.login_utilisateur.toLowerCase().includes(filterLogin.toLowerCase()))
      )
    : dataToShow;

  const columns = [
    { key: "nom_famille", label: "Nom de la Famille", sortable: true },
    { key: "login_utilisateur", label: "Login de l'utilisateur", sortable: true },
  ];

  return (
    <div>
      <ErrorPopup error={error} setError={setError} />
      <div className="bourse-categories">
        <button className="btn-modern" onClick={() => handleCategoryClick('ECONOMIE')}><span>ECONOMY</span></button>
        <button className="btn-modern" onClick={() => handleCategoryClick('SANTE')}><span>HEALTH</span></button>
        <button className="btn-modern" onClick={() => handleCategoryClick('EDUCATION')}><span>EDUCATION</span></button>
        <button className="btn-modern" onClick={() => handleCategoryClick('SOCIAL')}><span>SOCIAL</span></button>
        <button className="btn-modern" onClick={() => handleCategoryClick('CONFORT DE VIE')}><span>QUALITY OF LIFE</span></button>
        <button className="btn-modern" onClick={() => handleCategoryClick(null)}><span>ALL</span></button>
        <button className="btn-modern" onClick={handleMostActiveClick}><span>MOST ACTIVE</span></button>
      </div>



      <div className="filter-box">
        <label htmlFor="filterLogin">Filter by Name or Login</label>
        <SearchFilter
          placeholder="Search..."
          value={filterLogin}
          onChange={setFilterLogin}
        />
      </div>

      {loading && <p>Loading data...</p>}

      {!loading && (
        <>
          {filteredFamilies.length > 0 ? (
            <SortableTable columns={columns} data={filteredFamilies} itemsPerPage={ITEMS_PER_PAGE} />
          ) : (
            <p className="no-data">No eligible families found.</p>
          )}
        </>
      )}
    </div>
  );
};

export default BourseListPage;
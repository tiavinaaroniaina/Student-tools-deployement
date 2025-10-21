import React, { useState, useEffect } from "react";
import "../styles/notification.css";
import API_BASE_URL from "../config.js";

const ITEMS_PER_PAGE = 10;

const Notification = ({ userResponse, kind, sidebarVisible }) => {
  const [absenceData, setAbsenceData] = useState([]);
  const [error, setError] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [currentPage, setCurrentPage] = useState(1);
  const [sortOrder, setSortOrder] = useState("desc");
  const [dateDebut, setDateDebut] = useState("2025-03-03");
  const [granulariteMin, setGranulariteMin] = useState(5);
  const [granulariteMax, setGranulariteMax] = useState(100);
  const [notificationMessage, setNotificationMessage] = useState("");
  const [minError, setMinError] = useState(null); // New state for min granularity error
  const [maxError, setMaxError] = useState(null); // New state for max granularity error

  const handleMinChange = (e) => {
    const value = e.target.value;
    setGranulariteMin(value);

    if (value === '' || value === '-') { // Allow empty or just '-' for typing
      setMinError(null);
      return;
    }

    const numValue = Number(value); // Use Number() to handle floats and 'e'
    if (isNaN(numValue)) {
      setMinError("Minimum granularity must be a number.");
    } else if (!Number.isInteger(numValue)) {
      setMinError("Minimum granularity must be a whole number.");
    } else if (numValue < 0) {
      setMinError("Minimum granularity cannot be negative.");
    } else {
      setMinError(null);
    }
  };

  const handleMaxChange = (e) => {
    const value = e.target.value;
    setGranulariteMax(value);

    if (value === '' || value === '-') { // Allow empty or just '-' for typing
      setMaxError(null);
      return;
    }

    const numValue = Number(value); // Use Number() to handle floats and 'e'
    if (isNaN(numValue)) {
      setMaxError("Maximum granularity must be a number.");
    } else if (!Number.isInteger(numValue)) {
      setMaxError("Maximum granularity must be a whole number.");
    } else if (numValue < 0) {
      setMaxError("Maximum granularity cannot be negative.");
    } else {
      setMaxError(null);
    }
  };

  const isApplyFiltersDisabled = minError || maxError; // Disable button if any error exists

  const fetchAbsentUsers = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const url = `${API_BASE_URL}/stats/absences?granulariteMin=${granulariteMin}&granulariteMax=${granulariteMax}&dateDebut=${dateDebut}`;
      const response = await fetch(url, { credentials: "include" });
      if (!response.ok) {
        throw new Error("Server error");
      }
      const data = await response.json();
      setAbsenceData(data);
    } catch (err) {
      console.error("Failed to fetch absent users:", err);
      setError("Unable to load absence data.");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchAbsentUsers();
  }, []);

  const handleNotify = async (userLogin, userEmail) => {
    try {
      const response = await fetch(`${API_BASE_URL}/stats/notify`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        credentials: "include",
        body: JSON.stringify({ login: userLogin, email: userEmail }),
      });

      if (!response.ok) {
        throw new Error("Failed to send notification.");
      }

      setNotificationMessage(`Notification sent to ${userLogin}`);
      setTimeout(() => setNotificationMessage(""), 3000);
    } catch (err) {
      setNotificationMessage(`Error: ${err.message}`);
      setTimeout(() => setNotificationMessage(""), 3000);
    }
  };

  const handleSort = () => {
    const newSortOrder = sortOrder === "desc" ? "asc" : "desc";
    setSortOrder(newSortOrder);
    const sortedData = [...absenceData].sort((a, b) =>
      newSortOrder === "desc"
        ? b.joursAbsentsConsecutifs - a.joursAbsentsConsecutifs
        : a.joursAbsentsConsecutifs - b.joursAbsentsConsecutifs
    );
    setAbsenceData(sortedData);
  };

  const totalPages = Math.ceil(absenceData.length / ITEMS_PER_PAGE);
  const paginatedData = absenceData.slice(
    (currentPage - 1) * ITEMS_PER_PAGE,
    currentPage * ITEMS_PER_PAGE
  );

  return (
    <div className={`notification-page ${sidebarVisible ? "sidebar-visible" : ""}`}>
      <div className="notification-container">
        <h2>Consecutive Absences</h2>

        {notificationMessage && <div className="notification">{notificationMessage}</div>}

        <div className="filters">
          <div className="filter-group">
            <label>
              Start Date:
              <input
                type="date"
                value={dateDebut}
                onChange={(e) => setDateDebut(e.target.value)}
              />
            </label>
          </div>
          <div className="filter-group">
            <label>
              Min Granularity:
              <input
                type="number"
                value={granulariteMin}
                onChange={handleMinChange}
              />
            </label>
            {minError && <p className="error-message">{minError}</p>}
          </div>
          <div className="filter-group">
            <label>
              Max Granularity:
              <input
                type="number"
                value={granulariteMax}
                onChange={handleMaxChange}
              />
            </label>
            {maxError && <p className="error-message">{maxError}</p>}
          </div>
          <button onClick={fetchAbsentUsers} className="filter-btn" disabled={isApplyFiltersDisabled}>
            Apply Filters
          </button>
        </div>

        {isLoading && <p className="loading-message">Loading data...</p>}
        {error && <p className="error-message">{error}</p>}

        {!isLoading && absenceData.length > 0 ? (
          <div className="absent-users-list">
            <h4>Total absent users: {absenceData.length}</h4>
            <table className="absence-table">
              <thead>
                <tr>
                  <th>Login</th>
                  <th>Last Name</th>
                  <th>First Name</th>
                  <th onClick={handleSort} className="sortable">
                    Consecutive Absences
                    <span className={`sort-arrow ${sortOrder}`}>
                      {sortOrder === "desc" ? "↓" : "↑"}
                    </span>
                  </th>
                  <th>Total Absences</th>
                  <th>Notify</th>
                </tr>
              </thead>
              <tbody>
                {paginatedData.map((user, index) => (
                  <tr key={index}>
                    <td>{user.login}</td>
                    <td>{user.lastName}</td>
                    <td>{user.firstName}</td>
                    <td>{user.joursAbsentsConsecutifs}</td>
                    <td>{user.totalAbsences}</td>
                    <td>
                      <button
                        className="notify-btn"
                        onClick={() => handleNotify(user.login, user.email)}
                      >
                        Notify
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>

            <div className="pagination">
              <button
                onClick={() => setCurrentPage((p) => p - 1)}
                disabled={currentPage === 1}
                className="pagination-btn"
              >
                Previous
              </button>
              <span>Page {currentPage} of {totalPages}</span>
              <button
                onClick={() => setCurrentPage((p) => p + 1)}
                disabled={currentPage === totalPages}
                className="pagination-btn"
              >
                Next
              </button>
            </div>
          </div>
        ) : (
          !isLoading && <p className="empty-message">No absent users found.</p>
        )}
      </div>
    </div>
  );
};

export default Notification;
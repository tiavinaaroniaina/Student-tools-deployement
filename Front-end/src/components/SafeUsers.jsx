import React, { useState, useEffect } from "react";
import "../styles/safe-users.css";
import API_BASE_URL from "../config.js";

const SafeUsers = ({ userResponse, kind, sidebarVisible }) => {
  const [safeUsers, setSafeUsers] = useState([]);
  const [error, setError] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [currentPage, setCurrentPage] = useState(1);
  const ITEMS_PER_PAGE = 10;

  useEffect(() => {
    if (userResponse && kind?.toLowerCase() === "admin") {
      setIsLoading(true);
      setError(null);

      fetch(`${API_BASE_URL}/api/safe-users`, { credentials: "include" })
        .then(res => {
          if (!res.ok) {
            throw new Error("Could not fetch safe users");
          }
          return res.json();
        })
        .then(data => {
          setSafeUsers(data);
        })
        .catch(err => {
          console.error("Failed to fetch safe users:", err);
          setError(err.message || "Unable to load data.");
        })
        .finally(() => {
          setIsLoading(false);
        });
    }
  }, [userResponse, kind]);

  const handleExport = () => {
    const csvContent =
      "data:text/csv;charset=utf-8," +
      "Login,nom,prenom,contact1,Email\n" +
      safeUsers
        .map(user => `${user.login},${user.lastName},${user.firstName},${user.contactPhone1},${user.email}`)
        .join("\n");

    const encodedUri = encodeURI(csvContent);
    const link = document.createElement("a");
    link.setAttribute("href", encodedUri);
    link.setAttribute("download", "safe_users.csv");
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  const totalPages = Math.ceil(safeUsers.length / ITEMS_PER_PAGE);
  const paginatedData = safeUsers.slice(
    (currentPage - 1) * ITEMS_PER_PAGE,
    currentPage * ITEMS_PER_PAGE
  );

  return (
    <div className={`safe-users-page ${sidebarVisible ? "sidebar-visible" : ""}`}>
      <div className="safe-users-container">
        <h2>Users Not in Blackhole</h2>

        {kind?.toLowerCase() === "admin" && (
          <div className="filters">
            <div className="filter-group">
              <button onClick={handleExport} className="export-btn">
                Export CSV
              </button>
            </div>
          </div>
        )}

        {isLoading && <p className="loading-message">Loading data...</p>}
        {error && <p className="error-message">{error}</p>}

        {!isLoading && safeUsers.length > 0 ? (
          <div className="safe-users-list">
            <h4>Total Safe Users: {safeUsers.length}</h4>
            <table className="safe-users-table">
              <thead>
                <tr>
                  <th>Login</th>
                  <th>nom</th>
                  <th>prenom</th>
                  <th>contact_phone1</th>
                  <th>Email</th>

                </tr>
              </thead>
              <tbody>
                {paginatedData.map((user, index) => (
                  <tr key={index}>
                    <td>{user.login}</td>
                    <td>{user.lastName}</td>
                    <td>{user.firstName}</td>
                    <td>{user.contactPhone1}</td>
                    <td>{user.email}</td>
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
              <span>
                Page {currentPage} of {totalPages}
              </span>
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
          !isLoading && !error && <p className="empty-message">No safe users found.</p>
        )}
      </div>
    </div>
  );
};

export default SafeUsers;

import React, { useState, useEffect } from "react";
import "../index.css";
import API_BASE_URL from "../config.js";
import SearchFilter from "./SearchFilter";

const ITEMS_PER_PAGE = 10;

const Blackhole = ({ userResponse, kind, sidebarVisible, suggestions = [] }) => {
  const [blackholeData, setBlackholeData] = useState([]);
  const [error, setError] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [currentPage, setCurrentPage] = useState(1);
  const [sortOrder, setSortOrder] = useState("asc");
  const [loginFilter, setLoginFilter] = useState("");
  const [filteredSuggestions, setFilteredSuggestions] = useState([]);
  const [notificationMessage, setNotificationMessage] = useState("");

  const fetchBlackholeStudents = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const url = `${API_BASE_URL}/api/blackhole`;
      const response = await fetch(url, { credentials: "include" });

      if (response.status === 403) {
        throw new Error("Access denied: administrator rights required.");
      }
      if (!response.ok) {
        throw new Error("Server error while fetching Blackhole data.");
      }

      const data = await response.json();
      setBlackholeData(data);
    } catch (err) {
      console.error("Failed to fetch Blackhole students:", err);
      setError(err.message || "Unable to load Blackhole data.");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    if (userResponse && kind?.toLowerCase() === "admin") {
      fetchBlackholeStudents();
    }
  }, [userResponse, kind]);

  const handleSort = () => {
    const newSortOrder = sortOrder === "desc" ? "asc" : "desc";
    setSortOrder(newSortOrder);
    const sortedData = [...blackholeData].sort((a, b) =>
      newSortOrder === "desc"
        ? b.daysUntilBlackhole - a.daysUntilBlackhole
        : a.daysUntilBlackhole - b.daysUntilBlackhole
    );
    setBlackholeData(sortedData);
  };

  const handleLoginChange = (value) => {
    setLoginFilter(value);

    if (kind?.toLowerCase() === "admin" && value.trim() !== "") {
      const matches = suggestions
        .filter((s) => s.toLowerCase().includes(value.toLowerCase()))
        .slice(0, 5);
      setFilteredSuggestions(matches);
    } else {
      setFilteredSuggestions([]);
    }
  };

  const handleSuggestionClick = (s) => {
    setLoginFilter(s);
    setFilteredSuggestions([]);
  };

  const handleNotify = async (studentLogin, studentEmail) => {
    try {
      const response = await fetch(`${API_BASE_URL}/api/notify`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        credentials: "include",
        body: JSON.stringify({ login: studentLogin, email: studentEmail }),
      });

      if (!response.ok) {
        throw new Error("Failed to send notification.");
      }

      setNotificationMessage(`Notification sent to ${studentLogin}`);
      setTimeout(() => setNotificationMessage(""), 3000);
    } catch (err) {
      setNotificationMessage(`Error: ${err.message}`);
      setTimeout(() => setNotificationMessage(""), 3000);
    }
  };

  const handleExport = () => {
    const csvContent =
      "data:text/csv;charset=utf-8," +
      "Login,Blackhole Date,Days Remaining\n" +
      filteredData
        .map(
          (student) =>
            `${student.login},${student.blackholedAt},${student.daysUntilBlackhole}`
        )
        .join("\n");

    const encodedUri = encodeURI(csvContent);
    const link = document.createElement("a");
    link.setAttribute("href", encodedUri);
    link.setAttribute("download", "blackhole_students.csv");
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  const filteredData = loginFilter
    ? blackholeData.filter((student) =>
        student.login.toLowerCase().includes(loginFilter.toLowerCase())
      )
    : blackholeData;

  const totalPages = Math.ceil(filteredData.length / ITEMS_PER_PAGE);
  const paginatedData = filteredData.slice(
    (currentPage - 1) * ITEMS_PER_PAGE,
    currentPage * ITEMS_PER_PAGE
  );

  return (
    <div className={`blackhole-page ${sidebarVisible ? "sidebar-visible" : ""}`}>
      <div className="blackhole-container">
        <h2>Blackhole Students</h2>

        {notificationMessage && <div className="notification">{notificationMessage}</div>}

        {kind?.toLowerCase() === "admin" && (
          <div className="filters">
            <div className="filter-group">
              <label>Student Login:</label>
              <SearchFilter
                placeholder="Search by login..."
                value={loginFilter}
                onChange={handleLoginChange}
                suggestions={filteredSuggestions}
                onSuggestionClick={handleSuggestionClick}
              />
            </div>
            <div className="filter-group">
              <button onClick={handleExport} className="export-btn">
                Export CSV
              </button>
            </div>
          </div>
        )}

        {isLoading && <p className="loading-message">Loading data...</p>}
        {error && <p className="error-message">{error}</p>}

        {!isLoading && filteredData.length > 0 ? (
          <div className="blackhole-users-list">
            <h4>Total Blackhole Students: {filteredData.length}</h4>
            <table className="blackhole-table">
              <thead>
                <tr>
                  <th>Badge</th>
                  <th>Login</th>
                  <th>Blackhole Date</th>
                  <th onClick={handleSort} className="sortable">
                    Days Remaining
                    <span className={`sort-arrow ${sortOrder}`}>
                      {sortOrder === "desc" ? "↓" : "↑"}
                    </span>
                  </th>
                  <th>Notify</th>
                </tr>
              </thead>
              <tbody>
                {paginatedData.map((student, index) => (
                  <tr key={index}>
                    <td>
                      <span
                        className={`blackhole-badge ${student.badgeColor}`}
                        title={`${student.daysUntilBlackhole} days remaining`}
                      ></span>
                    </td>
                    <td>{student.login}</td>
                    <td>{student.blackholedAt}</td>
                    <td>{student.daysUntilBlackhole}</td>
                    <td>
                      <button
                        className="notify-btn"
                        onClick={() => handleNotify(student.login, student.email)}
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
          !isLoading &&
          !error && <p className="empty-message">No Blackhole students found.</p>
        )}
      </div>
    </div>
  );
};

export default Blackhole;
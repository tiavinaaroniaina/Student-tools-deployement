import React, { useState, useEffect } from "react";
import { Doughnut } from "react-chartjs-2";
import { Chart as ChartJS, ArcElement, Tooltip, Legend } from "chart.js";
import API_BASE_URL from "../config.js";
import ErrorPopup from "./ErrorPopup.jsx";
import SearchFilter from "./SearchFilter.jsx";
import SortableTable from "./SortableTable.jsx";

ChartJS.register(ArcElement, Tooltip, Legend);

class ResponseStatusException extends Error {
  constructor(status, message) {
    super(message);
    this.status = status;
  }
}

const ITEMS_PER_PAGE = 10;

const Check = ({ user, kind, suggestions: globalSuggestions = [] }) => {
  const [startDate, setStartDate] = useState(
    new Date(new Date().setMonth(new Date().getMonth() - 1)).toISOString().split("T")[0]
  );
  const [endDate, setEndDate] = useState(new Date().toISOString().split("T")[0]);
  const [userStats, setUserStats] = useState([]);
  const [hourlyAverages, setHourlyAverages] = useState([]);
  const [globalRate, setGlobalRate] = useState(0);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);
  const [filterLogin, setFilterLogin] = useState("");
  const [activeView, setActiveView] = useState("jour");

  const fetchStats = async () => {
    setLoading(true);
    setError(null);

    try {
      if (activeView === 'jour') {
        const resGlobal = await fetch(
          `${API_BASE_URL}/stats/global?startDate=${startDate}&endDate=${endDate}`,
          { credentials: "include" }
        );
        if (!resGlobal.ok) throw new ResponseStatusException(resGlobal.status, "Error fetching global stats");
        const global = await resGlobal.json();
        setGlobalRate(global || 0);

        if (kind === "admin") {
          const url = `${API_BASE_URL}/stats/users?startDate=${startDate}&endDate=${endDate}`;
          const resUsers = await fetch(url, { credentials: "include" });
          if (!resUsers.ok) throw new ResponseStatusException(resUsers.status, "Error fetching user stats");
          const users = await resUsers.json();
          setUserStats(users);
        }
      } else if (activeView === 'heure') {
        if (kind === "admin") {
          const res = await fetch(`${API_BASE_URL}/stats/moyennes-heures`, { credentials: "include" });
          if (!res.ok) throw new ResponseStatusException(res.status, "Error fetching hourly averages");
          const averages = await res.json();
          setHourlyAverages(averages);
        }
      }
    } catch (err) {
      console.error("Error fetching stats:", err.message);
      setError(err.message || "Failed to fetch statistics.");
      if (err.status === 401) {
        setTimeout(() => { window.location.href = `${API_BASE_URL}/login`; }, 2000);
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchStats();
  }, [activeView, kind]);

  const filteredSuggestions = filterLogin
    ? globalSuggestions.filter(s => s.toLowerCase().includes(filterLogin.toLowerCase())).slice(0, 5)
    : [];

  const filteredDailyStats = filterLogin
    ? userStats.filter((u) => u.login.toLowerCase().includes(filterLogin.toLowerCase()))
    : userStats;

  const filteredHourlyStats = filterLogin
    ? hourlyAverages.filter((u) => u.login.toLowerCase().includes(filterLogin.toLowerCase()))
    : hourlyAverages;

  const dailyTableColumns = [
    { key: "firstName", label: "First Name", sortable: true },
    { key: "lastName", label: "Last Name", sortable: true, render: (value) => value?.toUpperCase() },
    { key: "login", label: "Login", sortable: true },
    { key: "joursPresent", label: "Days Present", sortable: true },
    { key: "joursTotaux", label: "Total Days", sortable: true },
    {
      key: "tauxPresence",
      label: "Presence Rate (%)",
      sortable: true,
      render: (value) => (
        <span className={value >= 80 ? "badge badge-success" : value >= 60 ? "badge badge-warning" : "badge badge-error"}>
          {value}%
        </span>
      ),
    },
  ];

  const hourlyTableColumns = [
    { key: "login", label: "Login", sortable: true },
    { key: "moyenneHeureDepuisDebut", label: "Moyenne (début)", sortable: true },
    { key: "moyenneHeureDepuis3Mois", label: "Moyenne (3 mois)", sortable: true },
    { key: "moyenneHeureDepuis1Mois", label: "Moyenne (1 mois)", sortable: true },
    { key: "moyenneHeureDepuis1Semaine", label: "Moyenne (1 semaine)", sortable: true },
  ];

  const doughnutData = {
    labels: ["Present", "Absent"],
    datasets: [
      { data: [globalRate, 100 - globalRate], backgroundColor: ["#00ffc0", "#333"], hoverBackgroundColor: ["#00ffd0", "#555"], borderWidth: 0 },
    ],
  };

  const doughnutOptions = {
    responsive: true,
    cutout: "70%",
    plugins: { legend: { display: false }, tooltip: { enabled: false } },
  };

  return (
    <div className="checking-admin">
      <ErrorPopup error={error} setError={setError} />

      <div className="checking-tabs">
        <ul>
          <li onClick={() => setActiveView("jour")} className={activeView === "jour" ? "active" : ""}>
            Jour
            {activeView === "jour" && <u />}
          </li>
          <li onClick={() => setActiveView("heure")} className={activeView === "heure" ? "active" : ""}>
            Heure
            {activeView === "heure" && <u />}
          </li>
        </ul>
      </div>

      <form onSubmit={(e) => { e.preventDefault(); fetchStats(); }} className="checking-form">
        {activeView === "jour" && (
          <div className="date-group">
            <div>
              <label htmlFor="startDate">Start Date</label>
              <input type="date" id="startDate" value={startDate} onChange={(e) => setStartDate(e.target.value)} required />
            </div>
            <div>
              <label htmlFor="endDate">End Date</label>
              <input type="date" id="endDate" value={endDate} onChange={(e) => setEndDate(e.target.value)} required />
            </div>
          </div>
        )}

        <div className="fetch-button-container">
          <button type="submit" className="codepen-button" disabled={loading}>Fetch</button>
        </div>
      </form>

      {loading && <p>Loading data...</p>}

      {!loading && (
        <div className="results-section">
          {kind === "admin" && (
            <div className="filter-box">
              <label htmlFor="filterLogin">Filter by Login</label>
              <SearchFilter
                placeholder="Search by login..."
                value={filterLogin}
                onChange={setFilterLogin}
                suggestions={filteredSuggestions}
                onSuggestionClick={setFilterLogin}
              />
            </div>
          )}

          {activeView === 'jour' && (
            <>
              <div style={{ position: "relative", width: "200px", margin: "20px auto" }}>
                <Doughnut data={doughnutData} options={doughnutOptions} />
                <div style={{ position: "absolute", top: "50%", left: "50%", transform: "translate(-50%, -50%)", fontSize: "1.5rem", fontWeight: "700", color: "#00ffc0" }}>
                  {globalRate}%
                </div>
              </div>
              {filteredDailyStats.length > 0 ? (
                <SortableTable columns={dailyTableColumns} data={filteredDailyStats} itemsPerPage={ITEMS_PER_PAGE} />
              ) : (
                <p className="no-data">No attendance records found for this period.</p>
              )}
            </>
          )}

          {activeView === 'heure' && (
            <>
              {filteredHourlyStats.length > 0 ? (
                <SortableTable columns={hourlyTableColumns} data={filteredHourlyStats} itemsPerPage={ITEMS_PER_PAGE} />
              ) : (
                <p className="no-data">No hourly attendance records found.</p>
              )}
            </>
          )}
        </div>
      )}
    </div>
  );
};

export default Check;

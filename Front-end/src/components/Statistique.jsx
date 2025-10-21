import React, { useState, useEffect } from "react";
import { Line } from "react-chartjs-2";
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
} from "chart.js";
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faCalendarAlt } from '@fortawesome/free-solid-svg-icons';
import API_BASE_URL from "../config.js";
import ErrorPopup from "./ErrorPopup.jsx";
import "../styles/statistics.css";

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend
);

class ResponseStatusException extends Error {
  constructor(status, message) {
    super(message);
    this.status = status;
  }
}

const Statistique = () => {
  const [monthlyPresenceData, setMonthlyPresenceData] = useState([]);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);
  const [selectedYear, setSelectedYear] = useState(''); // New state for selected year

  const fetchMonthlyPresence = async () => {
    setLoading(true);
    setError(null);
    try {
      let url = `${API_BASE_URL}/stats/monthly-presence`;
      if (selectedYear) {
        url += `?year=${selectedYear}`; // Add year parameter if selected
      }
      const res = await fetch(
        url,
        { credentials: "include" }
      );
      if (!res.ok) {
        if (res.status === 401) {
          setError("Authentication required. Redirecting to login...");
          setTimeout(() => {
            window.location.href = `${API_BASE_URL}/login`;
          }, 2000);
          return;
        }
        const errorData = await res.json().catch(() => ({}));
        throw new ResponseStatusException(
          res.status,
          errorData.error || `HTTP error! status: ${res.status}`
        );
      }
      const data = await res.json();
      setMonthlyPresenceData(data);
    } catch (err) {
      console.error("Error fetching monthly presence stats:", err.message);
      setError(err.message || "Failed to fetch monthly presence statistics.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchMonthlyPresence();
  }, [selectedYear]); // Re-fetch when selectedYear changes

  // Generate years for the dropdown (e.g., current year and past 5 years)
  const currentYear = new Date().getFullYear();
  const years = Array.from({ length: 5 }, (_, i) => currentYear - i); // Last 5 years including current

  const chartData = {
    labels: monthlyPresenceData.map((item) => item.month),
    datasets: [
      {
        label: "Global Presence Rate (%)",
        data: monthlyPresenceData.map((item) => item.presenceRate),
        fill: false,
        borderColor: "rgb(75, 192, 192)",
        tension: 0.1,
        borderWidth: 3,
        pointRadius: 6,
      },
    ],
  };

  const chartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: "top",
        labels: {
          font: {
            size: 16,
          },
        },
      },
      title: {
        display: true,
        text: "Monthly Global Presence Rate ",
        font: {
          size: 24,
        },
      },
      tooltip: {
        titleFont: { size: 16 },
        bodyFont: { size: 14 },
      },
    },
    scales: {
      y: {
        beginAtZero: true,
        max: 100,
        title: {
          display: true,
          text: "Presence Rate (%)",
          font: {
            size: 18,
          },
        },
        ticks: {
          font: {
            size: 14,
          },
        },
      },
      x: {
        title: {
          display: true,
          text: "Month",
          font: {
            size: 18,
          },
        },
        ticks: {
          font: {
            size: 14,
          },
        },
      },
    },
  };

  return (
    <div className="contact-card statistique-content-card">
      <ErrorPopup error={error} setError={setError} />

      <h3 className="dashboard-title-stats">Monthly Global Presence Rate</h3>

      <div className="filter-controls">
        <label htmlFor="year-select">
          <FontAwesomeIcon icon={faCalendarAlt} style={{ marginRight: '8px' }} />
          Filter by Year:
        </label>
        <select
          id="year-select"
          value={selectedYear}
          onChange={(e) => setSelectedYear(e.target.value)}
        >
          <option value="">Last 3 Months</option>
          {years.map((year) => (
            <option key={year} value={year}>
              {year}
            </option>
          ))}
        </select>
      </div>

      {loading && <p>Loading data...</p>}

      {!loading && monthlyPresenceData.length > 0 ? (
        <div className="chart-container">
          <Line data={chartData} options={chartOptions} />
        </div>
      ) : (
        !loading && <p className="no-data">No monthly presence statistics available.</p>
      )}
    </div>
  );
};

export default Statistique;
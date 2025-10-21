import React, { useState, useEffect } from "react";
import "../index.css";
import SearchFilter from "./SearchFilter";
import { useToast } from "../hooks/useToast";
import ToastContainer from "./ToastContainer";

const Calendar = ({ userResponse, kind, suggestions = [], initialDataType = 'students' }) => {
  const [view, setView] = useState("month");
  const [page, setPage] = useState(0);
  const [year, setYear] = useState(new Date().getFullYear());
  const [calendarData, setCalendarData] = useState(null);
  const [login, setLogin] = useState(kind === "admin" ? "" : userResponse?.login || "");
  const [previousLogin, setPreviousLogin] = useState(login);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [filteredSuggestions, setFilteredSuggestions] = useState([]);
  const [hasFetched, setHasFetched] = useState(false);
  const [dataType, setDataType] = useState(initialDataType); // 'students' or 'piscine'
  const { toasts, removeToast, warning } = useToast();

  useEffect(() => {
    setDataType(initialDataType);
    setCalendarData(null);
    setLogin("");
    setHasFetched(false);
    setError(null);
  }, [initialDataType]);

  const baseMonth = new Date().getMonth();

  const fetchCalendar = async (loginValue = "", currentView = view, currentYear = year) => {
    try {
      setLoading(true);
      setError(null);

      const params = new URLSearchParams({ year: currentYear.toString() });
      if (currentView === "month") params.append("month", (baseMonth + 1).toString());

      let loginToUse = "";
      if (kind === "admin" && loginValue.trim() !== "") {
        loginToUse = loginValue.trim();
      } else if (kind !== "admin" && userResponse?.login) {
        loginToUse = userResponse.login;
      }
      
      if (loginToUse) {
        params.append("login", loginToUse);
      }
      
      const res = await fetch(`http://localhost:9090/calendar?${params}`, { credentials: "include" });
      const data = await res.json();
      if (data.error) throw new Error(data.error);

      // Check for student on piscine page
      if (dataType === 'piscine' && (data.milestones?.length > 0 || data.blackholed_at)) {
        warning("This is a student, not a pisciner.");
        setCalendarData(null);
      // Check for pisciner on student page
      } else if (dataType === 'students' && (!data.milestones || data.milestones.length === 0) && !data.blackholed_at) {
        warning("This is a pisciner, not a student.");
        setCalendarData(null);
      } else {
        console.log("Calendar data fetched:", data);
        setCalendarData(data);
      }

      setHasFetched(true);
    } catch (err) {
      console.error("Error loading calendar:", err);
      setError(err.message || "Error loading calendar");
    } finally {
      setLoading(false);
    }
  };

  // This function is no longer needed as the buttons are removed.
  // const handleDataTypeChange = (type) => {
  //   setDataType(type);
  //   setLogin("");
  //   setCalendarData(null);
  //   setHasFetched(false);
  // };

  useEffect(() => {
    if (kind !== "admin" && userResponse?.login) {
      fetchCalendar(userResponse.login);
    }
  }, [userResponse, kind]);

  const handleLoginChange = (value) => {
    setLogin(value);

    if (kind === "admin" && value.trim() !== "") {
      const matches = suggestions
        .filter((s) => s.toLowerCase().includes(value.toLowerCase()))
        .slice(0, 5);
      setFilteredSuggestions(matches);
    } else {
      setFilteredSuggestions([]);
    }

    if (value.trim() !== previousLogin.trim()) {
      setHasFetched(false);
      setPreviousLogin(value);
    }
  };

  const handleSuggestionClick = (s) => {
    setLogin(s);
    setFilteredSuggestions([]);
    setPreviousLogin(s);
    // fetchCalendar(s); // Auto-fetch removed as per request
  };

  const parseDurationToHours = (duration) => {
    if (!duration) return 0;
    try {
      const regex = /(\d+h)?\s*(\d+m)?/;
      const matches = duration.match(regex);
      let hours = 0;
      let minutes = 0;
      if (matches[1]) hours = parseInt(matches[1].replace("h", ""));
      if (matches[2]) minutes = parseInt(matches[2].replace("m", ""));
      return hours + minutes / 60;
    } catch (e) {
      console.error("Error parsing duration:", duration, e);
      return 0;
    }
  };

  const getDurationColorClass = (duration) => {
    const hours = parseDurationToHours(duration);
    if (hours < 2) return "presence-low";
    if (hours < 4) return "presence-medium-low";
    if (hours < 6) return "presence-medium";
    if (hours < 8) return "presence-medium-high";
    return "presence-high";
  };

  const getFreezeOrBonusEvent = (dateStr) => {
    if (!calendarData?.freezeAndBonusEvents) return null;
    return calendarData.freezeAndBonusEvents.find((event) => {
      try {
        const start = new Date(event.start);
        const end = new Date(event.end);
        const date = new Date(dateStr);
        return date >= start && date <= end;
      } catch (e) {
        console.error("Error parsing freeze/bonus dates:", event, e);
        return false;
      }
    });
  };

  const generateMonthDays = (targetYear, targetMonth) => {
    const firstDayOfMonth = new Date(targetYear, targetMonth, 1).getDay();
    const lastDay = new Date(targetYear, targetMonth + 1, 0).getDate();
    const days = [];
    const offset = firstDayOfMonth === 0 ? 6 : firstDayOfMonth - 1;

    let isBlackholedNear = false;
    if (calendarData?.blackholed_at) {
      try {
        const today = new Date();
        const blackholedDate = new Date(calendarData.blackholed_at);
        const diffTime = blackholedDate - today;
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
        isBlackholedNear = diffDays > 0 && diffDays <= 21;
      } catch (e) {
        console.error("Error parsing blackholed_at date:", e);
      }
    }

    for (let i = 0; i < offset; i++) {
      days.push({ day: null, isPresent: false, validatedMilestone: null, deadlineMilestone: null, isBlackholedNear, duration: null, freezeOrBonus: null });
    }

    for (let i = 1; i <= lastDay; i++) {
      const dateStr = `${targetYear}-${String(targetMonth + 1).padStart(2, "0")}-${String(i).padStart(2, "0")}`;
      const isPresent = calendarData?.presence?.includes(dateStr) || false;
      const presenceStat = calendarData?.presenceStats?.find((stat) => stat.date === dateStr);
      const duration = isPresent && presenceStat ? presenceStat.duration : null;
      const validatedMilestone = calendarData?.milestones?.find((m) => m.validated_at === dateStr);
      const deadlineMilestone = calendarData?.milestones?.find((m) => m.deadline === dateStr);
      const isDeadline = calendarData?.blackholed_at === dateStr;
      const freezeOrBonus = getFreezeOrBonusEvent(dateStr);

      days.push({ day: i, isPresent, validatedMilestone, deadlineMilestone, isDeadline, isBlackholedNear, duration, freezeOrBonus });
    }

    return days;
  };

  const renderMonth = (targetYear, targetMonth) => {
    const days = generateMonthDays(targetYear, targetMonth);
    const monthName = new Date(targetYear, targetMonth).toLocaleString("en-US", { month: "long" });

    return (
      <div className="calendar-block" key={`${targetYear}-${targetMonth}`}>
        <h4>{monthName} {targetYear}</h4>
        <div className="calendar-grid">
          {days.map((d, idx) => {
            const classNames = [
              "calendar-day",
              d.day ? "" : "empty",
              d.isPresent ? `presence ${getDurationColorClass(d.duration)}` : "",
              d.validatedMilestone ? "milestone-validated" : "",
              d.deadlineMilestone ? "milestone-deadline" : "",
              d.isDeadline ? "deadline" : "",
              d.isDeadline && d.isBlackholedNear ? "blink" : "",
              d.freezeOrBonus ? d.freezeOrBonus.type === "freeze" ? "freeze" : "bonus" : "",
            ].filter(Boolean).join(" ");

            const title = d.day
              ? [
                  d.validatedMilestone
                    ? `Milestone Level ${d.validatedMilestone.level}: Validated on ${d.validatedMilestone.validated_at}${d.validatedMilestone.deadline ? `, Deadline: ${d.validatedMilestone.deadline}` : ""}`
                    : "",
                  d.deadlineMilestone && !d.validatedMilestone
                    ? `Milestone Level ${d.deadlineMilestone.level}: Deadline on ${d.deadlineMilestone.deadline}`
                    : "",
                  d.isDeadline ? "Blackholed Deadline" : "",
                  d.isPresent && d.duration ? `Presence: ${d.duration}` : "",
                  d.freezeOrBonus ? `${d.freezeOrBonus.title} (${d.freezeOrBonus.reason})` : "",
                ].filter(Boolean).join("\n")
              : "";

            return (
              <div key={idx} className={classNames} title={title}>
                {!d.freezeOrBonus && d.day && <span className="day-number">{d.day}</span>}
                {d.validatedMilestone && <span className="milestone-validated-marker">✔</span>}
                {d.deadlineMilestone && <span className="milestone-deadline-marker">⏳</span>}
                {d.isDeadline && <span className="deadline-marker">☠</span>}
                {d.freezeOrBonus && (
                  <span className="freeze-bonus-marker">
                    {d.freezeOrBonus.type === "freeze" ? (
                      <span className="icon-freeze">❄</span>
                    ) : (
                      <span className="icon-bonus">★</span>
                    )}
                  </span>
                )}
                {d.isPresent && d.duration && (
                  <div className="presence-tooltip">{d.duration}</div>
                )}
              </div>
            );
          })}
        </div>
      </div>
    );
  };

  const renderCalendar = () => {
    if (view === "month") {
      return renderMonth(year, baseMonth);
    }
    if (view === "quarter") {
      const startMonth = page * 3;
      return (
        <div className="calendar-multi">
          {Array.from({ length: 3 }, (_, i) => renderMonth(year, (startMonth + i) % 12))}
        </div>
      );
    }
    if (view === "semester") {
      const startMonth = page * 6;
      return (
        <div className="calendar-multi">
          {Array.from({ length: 6 }, (_, i) => renderMonth(year, (startMonth + i) % 12))}
        </div>
      );
    }
    if (view === "year") {
      return (
        <div className="calendar-multi year-view">
          {Array.from({ length: 12 }, (_, i) => renderMonth(year, i))}
        </div>
      );
    }
    return null;
  };

  const renderBlackholedAlert = () => {
    if (!calendarData?.blackholed_at) return null;

    const today = new Date();
    const blackholedDate = new Date(calendarData.blackholed_at);
    const diffTime = blackholedDate - today;
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    if (diffDays > 0 && diffDays <= 21) {
      return (
        <div className="alert-warning">
          B Warning: The blackholed date is in {diffDays} day{diffDays > 1 ? "s" : ""}!
        </div>
      );
    }
    return null;
  };

  const renderImportantDates = () => {
    return (
      <div className="important-dates">
        <h4>Important Dates</h4>
        {calendarData?.milestones && calendarData.milestones.length > 0 ? (
          <div className="milestone-dates">
            <h5>Milestones</h5>
            <ul>
              {calendarData.milestones.map((m, index) => (
                <li key={index} className={m.validated_at ? "validated" : "deadline"}>
                  Milestone {m.level}: {m.validated_at ? `Validated on ${m.validated_at}` : `Deadline on ${m.deadline}`}
                  {m.validated_at && m.deadline ? ` (Deadline: ${m.deadline})` : ""}
                </li>
              ))}
            </ul>
          </div>
        ) : (
          <p>No milestone dates.</p>
        )}
        {calendarData?.blackholed_at ? (
          <div className="blackholed-date">
            <h5>Blackholed Date</h5>
            <p>{calendarData.blackholed_at}</p>
          </div>
        ) : (
          <p>No blackholed date.</p>
        )}
        {calendarData?.freezeAndBonusEvents && calendarData.freezeAndBonusEvents.length > 0 ? (
          <div className="freeze-bonus-dates">
            <h5>Freeze and Bonus Periods</h5>
            <ul>
              {calendarData.freezeAndBonusEvents.map((event, index) => (
                <li key={index} className={event.type === "freeze" ? "freeze" : "bonus"}>
                  {event.title}: {event.start} to {event.end} ({event.reason})
                </li>
              ))}
            </ul>
          </div>
        ) : (
          <p>No freeze or bonus periods.</p>
        )}
      </div>
    );
  };

  const handlePrev = () => {
    if (view === "year") {
      const newYear = year - 1;
      setYear(newYear);
      setPage(0);
    } else {
      setPage((p) => Math.max(0, p - 1));
    }
  };

  const handleNext = () => {
    if (view === "year") {
      const newYear = year + 1;
      setYear(newYear);
      setPage(0);
    } else {
      const maxPage = view === "quarter" ? 3 : view === "semester" ? 1 : 0;
      setPage((p) => Math.min(maxPage, p + 1));
    }
  };

  return (
    <div className="checking-admin">
      <ToastContainer toasts={toasts} removeToast={removeToast} />
      <div className="checking-form">
        
        {kind === "admin" && (
          <div className="filter-box">
            <label htmlFor="login">Student Login</label>
            <SearchFilter
              placeholder="Enter login..."
              value={login}
              onChange={handleLoginChange}
              suggestions={filteredSuggestions}
              onSuggestionClick={handleSuggestionClick}
            />
            <button 
              type="button" 
              onClick={() => {
                if (!login.trim()) {
                  warning("Login is required.");
                } else {
                  fetchCalendar(login, view, year);
                }
              }}
            >
              Fetch Calendar
            </button>
          </div>
        )}
        <div className="date-group">
          <label htmlFor="view">View</label>
          <select
            id="view"
            value={view}
            onChange={(e) => {
              const newView = e.target.value;
              setView(newView);
              setPage(0);
            }}
          >
            <option value="month">Month</option>
            <option value="quarter">Quarter</option>
            <option value="semester">Semester</option>
            <option value="year">Year</option>
          </select>
        </div>
      </div>
      <div className="results-section">
        {loading && (
          <div className="loading-container">
            <span className="loading-spinner"></span>
            <p>Loading Calendar...</p>
          </div>
        )}
        {error && <p className="error">{error}</p>}
        {calendarData && !loading && (
          <>
            <div className="calendar-pagination">
              <button onClick={handlePrev} disabled={page === 0 && view !== "year"}>
                ◀ Prev
              </button>
              <span>{view === "year" ? `Year ${year}` : `Page ${page + 1}`}</span>
              <button onClick={handleNext}>
                Next ▶
              </button>
            </div>
            {renderBlackholedAlert()}
            {renderCalendar()}
          </>
        )}
        {!calendarData && !loading && !error && (
          <p className="no-data">No data available. Please fetch a calendar.</p>
        )}
      </div>
      <div className="important-dates-container">
        {calendarData && renderImportantDates()}
      </div>
    </div>
  );
};

export default Calendar;
import React, { useState } from "react";
import "../styles/sidebar.css";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faHome,
  faSnowflake,
  faCheckSquare,
  faCalendar,
  faBell,
  faCircle,
  faSignOutAlt,
  faChartBar,
  faUserGraduate,
  faFish,
  faUserShield,
  faDollarSign,
} from "@fortawesome/free-solid-svg-icons";
import { useNavigate } from "react-router-dom";
import API_BASE_URL from "../config";

const SidebarHover = ({ userKind, sidebarVisible, setSidebarVisible }) => {
  const navigate = useNavigate();
  const [isCalendarOpen, setCalendarOpen] = useState(false);
  const [isBourseOpen, setBourseOpen] = useState(false);

  const handleCalendarClick = (e) => {
    e.preventDefault();
    setCalendarOpen(!isCalendarOpen);
  };

  const handleBourseClick = (e) => {
    e.preventDefault();
    setBourseOpen(!isBourseOpen);
  };

  const handleLogout = async () => {
    try {
      const res = await fetch(`${API_BASE_URL}/logout`, {
        method: "GET",
        credentials: "include",
      });
      if (res.ok) {
        window.location.href = "/login";
      } else {
        console.error("Logout failed: ", await res.text());
        alert("Failed to log out. Please try again.");
      }
    } catch (error) {
      console.error("Logout failed", error);
      alert("An error occurred during logout. Please try again.");
    }
  };

  return (
    <>
      {/* Button toggle mobile */}
      <button
        className="sidebar-toggle-btn"
        onClick={() => setSidebarVisible(!sidebarVisible)}
      >
        ☰
      </button>

      <aside className={`sidebar ${sidebarVisible ? "visible" : ""}`}>
        <div className="sidebar-logo">
          <img src="/images/logo.png" alt="Logo" />
        </div>

        <ul>
          <li>
            <a href="/app/certificate">
              <FontAwesomeIcon icon={faHome} />
              <span>Certificat Scolarité</span>
            </a>
          </li>

          <li>
            <a href="/app/freeze-begin">
              <FontAwesomeIcon icon={faSnowflake} />
              <span>Freeze</span>
            </a>
          </li>

          <li className="has-submenu">
            <a href="/app/check">
              <FontAwesomeIcon icon={faCheckSquare} />
              <span>Checking</span>
            </a>
          </li>

          {/* Calendar */}
          <li className="has-submenu">
            <a href="#" onClick={handleCalendarClick}>
              <FontAwesomeIcon icon={faCalendar} />
              <span>Calendar</span>
            </a>
            {isCalendarOpen && (
              <ul className="submenu-calendar">
                <li>
                  <a
                    href="#"
                    onClick={(e) => {
                      e.preventDefault();
                      navigate("/app/calendar/student");
                      setCalendarOpen(false);
                    }}
                  >
                    <FontAwesomeIcon icon={faUserGraduate} />
                    <span>Student</span>
                  </a>
                </li>
                <li>
                  <a
                    href="#"
                    onClick={(e) => {
                      e.preventDefault();
                      navigate("/app/calendar/piscine");
                      setCalendarOpen(false);
                    }}
                  >
                    <FontAwesomeIcon icon={faFish} />
                    <span>Pisciner</span>
                  </a>
                </li>
              </ul>
            )}
          </li>

          {/* Absence - admin only */}
          {userKind === "admin" && (
            <li>
              <a href="/app/notification">
                <FontAwesomeIcon icon={faBell} />
                <span>Absence</span>
              </a>
            </li>
          )}

          {/* Blackhole - admin only */}
          {userKind === "admin" && (
            <li>
              <a href="/app/blackhole">
                <FontAwesomeIcon icon={faCircle} />
                <span>Blackhole</span>
              </a>
            </li>
          )}

          {/* Bourse (Scholarship) */}
          <li className="has-submenu">
            <a href="#" onClick={handleBourseClick}>
              <FontAwesomeIcon icon={faDollarSign} />
              <span>Scholarship</span>
            </a>
            {isBourseOpen && (
              <ul className="submenu-calendar">
                {/* Form visible pour tout le monde */}
                <li>
                  <a
                    href="#"
                    onClick={(e) => {
                      e.preventDefault();
                      navigate("/app/bourse");
                      setBourseOpen(false);
                    }}
                  >
                    <FontAwesomeIcon icon={faDollarSign} />
                    <span>Form</span>
                  </a>
                </li>

                {/* List visible seulement pour admin */}
                {userKind === "admin" && (
                  <li>
                    <a
                      href="#"
                      onClick={(e) => {
                        e.preventDefault();
                        navigate("/app/bourse/list");
                        setBourseOpen(false);
                      }}
                    >
                      <FontAwesomeIcon icon={faDollarSign} />
                      <span>List</span>
                    </a>
                  </li>
                )}
              </ul>
            )}
          </li>

          {/* Stats + Safe Users (admin only) */}
          {userKind === "admin" && (
            <>
              <li>
                <a href="/app/statistics">
                  <FontAwesomeIcon icon={faChartBar} />
                  <span>Statistics</span>
                </a>
              </li>
              <li>
                <a href="/app/safe-users">
                  <FontAwesomeIcon icon={faUserShield} />
                  <span>Safe Users</span>
                </a>
              </li>
            </>
          )}
        </ul>

        {/* Logout */}
        <div className="sidebar-logout">
          <a href="#" onClick={handleLogout}>
            <FontAwesomeIcon icon={faSignOutAlt} />
            <span>Logout</span>
          </a>
        </div>
      </aside>
    </>
  );
};

export default SidebarHover;
  
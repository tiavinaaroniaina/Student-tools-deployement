import React from "react";
import ThemeToggle from "./ThemeToggle";
import API_BASE_URL from "../config";

const Header = ({ user }) => {
  return (
    <header className="profile-card">
      <div className="profile-info">
        <img
          src={user?.image?.link || "/images/profil.jpeg"}
          alt={`${user?.first_name} ${user?.last_name}`}
          className="profile-pic"
          onError={(e) => {
            e.target.src = "/images/profil.jpeg";
          }}
        />
        <div className="name-status">
          <h1>
            {user
              ? `${user.first_name} ${user.last_name?.toUpperCase()}`
              : "Nom Inconnu"}
          </h1>
          <span className="username">{user?.login}</span>
          <p className="user-email">
            <i className="fa fa-envelope contact-icon" aria-hidden="true"></i>
            <span>{user?.email}</span>
          </p>
        </div>
      </div>

      <div className="header-controls">
        <ThemeToggle />
      </div>

      <div className="logo-section">
        <img src="/images/logo-42.png" alt="42 Logo" className="logo-img" />
      </div>
    </header>
  );
};

export default Header;

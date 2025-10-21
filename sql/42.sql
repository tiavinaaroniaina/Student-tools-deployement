\c postgres
DROP DATABASE IF EXISTS "e42";
CREATE DATABASE "e42";
\c e42

-- Table Image
CREATE TABLE Image (
    image_id SERIAL PRIMARY KEY,
    link VARCHAR(250),
    large_ VARCHAR(250),
    medium VARCHAR(250),
    small VARCHAR(250),
    micro VARCHAR(250),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- Table User_
CREATE TABLE User_ (
    user_id VARCHAR(250),
    email VARCHAR(250),
    login VARCHAR(250),
    first_name VARCHAR(250),
    last_name VARCHAR(250),
    usual_full_name VARCHAR(250),
    usual_first_name VARCHAR(250),
    url VARCHAR(250),
    phone VARCHAR(250),
    displayname VARCHAR(250),
    kind VARCHAR(250),
    staff BOOLEAN,
    correction_point NUMERIC(15,2),
    anonymize_date TIMESTAMPTZ,
    data_erasure_date TIMESTAMPTZ,
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ,
    alumnized_at TIMESTAMPTZ,
    alumni BOOLEAN,
    active BOOLEAN,
    pool_month VARCHAR(250),
    wallet NUMERIC(15,2),
    pool_year VARCHAR(50),
    location VARCHAR(250),
    image_id INTEGER NOT NULL,
    PRIMARY KEY(user_id),
    FOREIGN KEY(image_id) REFERENCES Image(image_id)
);

ALTER TABLE User_
ADD CONSTRAINT unique_user_id UNIQUE (user_id);

-- Table Stats
CREATE TABLE Stats (
    id SERIAL PRIMARY KEY,
    date_ DATE,
    duration INTERVAL,
    user_id VARCHAR(250),
    FOREIGN KEY(user_id) REFERENCES User_(user_id) ON DELETE CASCADE
);

ALTER TABLE Stats
ADD CONSTRAINT stats_user_date_unique UNIQUE (user_id, date_);

-- Table Locations
CREATE TABLE Locations(
   locations_id VARCHAR(50) PRIMARY KEY,
   begin_at TIMESTAMPTZ,
   end_at TIMESTAMPTZ,
   primary_location BOOLEAN,
   floor_ VARCHAR(250),
   row_ VARCHAR(250),
   post VARCHAR(250),
   host VARCHAR(250),
   campus_id VARCHAR(50),
   created_at TIMESTAMPTZ,
   updated_at TIMESTAMPTZ,
   user_id VARCHAR(250) NOT NULL,
   FOREIGN KEY(user_id) REFERENCES User_(user_id)
);

-- Table User_candidatures
CREATE TABLE User_candidatures (
    id SERIAL PRIMARY KEY,
    user_id VARCHAR(250) NOT NULL,
    birth_date DATE,
    gender VARCHAR(50),
    zip_code VARCHAR(50),
    country VARCHAR(100),
    birth_city VARCHAR(100),
    birth_country VARCHAR(100),
    postal_street VARCHAR(250),
    postal_complement VARCHAR(250),
    postal_city VARCHAR(100),
    postal_zip_code VARCHAR(50),
    postal_country VARCHAR(100),
    contact_affiliation VARCHAR(250),
    contact_last_name VARCHAR(250),
    contact_first_name VARCHAR(250),
    contact_phone1 VARCHAR(50),
    contact_phone2 VARCHAR(50),
    max_level_memory NUMERIC(15,2),
    max_level_logic NUMERIC(15,2),
    other_information TEXT,
    language VARCHAR(50),
    meeting_date TIMESTAMPTZ,
    piscine_date TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    phone VARCHAR(50),
    email VARCHAR(250),
    pin VARCHAR(50),
    phone_country_code VARCHAR(10),
    hidden_phone BOOLEAN,
    FOREIGN KEY(user_id) REFERENCES User_(user_id) ON DELETE CASCADE
);

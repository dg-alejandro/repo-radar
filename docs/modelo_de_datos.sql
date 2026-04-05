CREATE DATABASE IF NOT EXISTS repo_radar;
USE repo_radar;

CREATE TABLE administrator (
    id         INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(150) NOT NULL,
    email      VARCHAR(150) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE app_user (
    id            INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(150) NOT NULL,
    email         VARCHAR(150) NOT NULL UNIQUE,
    password      VARCHAR(255) NOT NULL,
    registered_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE category (
    id   SMALLINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE technology (
    id   SMALLINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE project (
    id               INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    repository_url   VARCHAR(250) NOT NULL UNIQUE,
    name             VARCHAR(150) NOT NULL,
    description      TEXT,
    author           VARCHAR(100) NOT NULL,
    stars            INT          NOT NULL DEFAULT 0,
    import_date      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME,
    status           ENUM('PUBLISHED', 'HIDDEN') NOT NULL DEFAULT 'HIDDEN',
    administrator_id INT UNSIGNED NOT NULL,
    CONSTRAINT fk_project_administrator
        FOREIGN KEY (administrator_id) REFERENCES administrator(id)
);

CREATE TABLE project_category (
    project_id  INT UNSIGNED      NOT NULL,
    category_id SMALLINT UNSIGNED NOT NULL,
    PRIMARY KEY (project_id, category_id),
    CONSTRAINT fk_pc_project
        FOREIGN KEY (project_id)  REFERENCES project(id) ON DELETE CASCADE,
    CONSTRAINT fk_pc_category
        FOREIGN KEY (category_id) REFERENCES category(id)
);

CREATE TABLE project_technology (
    project_id    INT UNSIGNED      NOT NULL,
    technology_id SMALLINT UNSIGNED NOT NULL,
    PRIMARY KEY (project_id, technology_id),
    CONSTRAINT fk_pt_project
        FOREIGN KEY (project_id)    REFERENCES project(id) ON DELETE CASCADE,
    CONSTRAINT fk_pt_technology
        FOREIGN KEY (technology_id) REFERENCES technology(id)
);

CREATE TABLE user_favorite (
    user_id    INT UNSIGNED NOT NULL,
    project_id INT UNSIGNED NOT NULL,
    added_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, project_id),
    CONSTRAINT fk_uf_user
        FOREIGN KEY (user_id)    REFERENCES app_user(id),
    CONSTRAINT fk_uf_project
        FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE
);
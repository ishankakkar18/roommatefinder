-- Run once as MySQL root:
--   mysql -u root < scripts/db-setup.sql
CREATE DATABASE IF NOT EXISTS flatmate
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

CREATE USER IF NOT EXISTS 'flatmate'@'localhost' IDENTIFIED BY 'flatmate';
GRANT ALL PRIVILEGES ON flatmate.* TO 'flatmate'@'localhost';
FLUSH PRIVILEGES;

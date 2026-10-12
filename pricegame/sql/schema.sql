CREATE DATABASE IF NOT EXISTS pricegame CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE pricegame;

DROP TABLE IF EXISTS round_history;
DROP TABLE IF EXISTS match_history;
DROP TABLE IF EXISTS product;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
  id           INT AUTO_INCREMENT PRIMARY KEY,
  username     VARCHAR(50)  NOT NULL UNIQUE,
  salt         VARCHAR(32)  NOT NULL,
  password     VARCHAR(64)  NOT NULL,            -- SHA-256(salt + password), hex
  total_score  DECIMAL(10,1) NOT NULL DEFAULT 0,
  total_wins   INT NOT NULL DEFAULT 0,
  total_played INT NOT NULL DEFAULT 0
);

CREATE TABLE product (
  id           INT AUTO_INCREMENT PRIMARY KEY,
  name         VARCHAR(150) NOT NULL,
  description  VARCHAR(500),
  image        VARCHAR(200),
  price        BIGINT NOT NULL                   -- gia thuc te (VND)
);

CREATE TABLE match_history (
  id          INT AUTO_INCREMENT PRIMARY KEY,
  player1_id  INT NOT NULL,
  player2_id  INT NOT NULL,
  score1      DECIMAL(4,1) NOT NULL,
  score2      DECIMAL(4,1) NOT NULL,
  winner_id   INT NULL,                          -- NULL neu hoa
  end_reason  VARCHAR(30) NOT NULL,              -- NORMAL / EXIT / DISCONNECT
  played_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (player1_id) REFERENCES users(id),
  FOREIGN KEY (player2_id) REFERENCES users(id)
);

CREATE TABLE round_history (
  id         INT AUTO_INCREMENT PRIMARY KEY,
  match_id   INT NOT NULL,
  round_no   INT NOT NULL,
  product_id INT NOT NULL,
  guess1     BIGINT NULL,
  guess2     BIGINT NULL,
  score1     DECIMAL(3,1) NOT NULL,
  score2     DECIMAL(3,1) NOT NULL,
  FOREIGN KEY (match_id)   REFERENCES match_history(id),
  FOREIGN KEY (product_id) REFERENCES product(id)
);

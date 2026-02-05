CREATE TABLE IF NOT EXISTS franchises (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(60) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS branches (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    franchise_id BIGINT NOT NULL,
    name VARCHAR(60) NOT NULL,
    FOREIGN KEY (franchise_id) REFERENCES franchises(id),
    UNIQUE(franchise_id, name)
);

CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    branch_id BIGINT NOT NULL,
    name VARCHAR(60) NOT NULL,
    stock INT NOT NULL,
    FOREIGN KEY (branch_id) REFERENCES branches(id),
    UNIQUE(branch_id, name)
);



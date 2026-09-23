CREATE TABLE IF NOT EXISTS client (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    cpf VARCHAR(255),
    phone VARCHAR(255),
    photo_url VARCHAR(255),
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uk_client_cpf ON client (cpf);

CREATE INDEX idx_client_name ON client (name);

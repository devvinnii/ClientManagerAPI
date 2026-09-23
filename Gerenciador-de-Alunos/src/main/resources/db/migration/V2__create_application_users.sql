CREATE TABLE app_user (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_app_user_username UNIQUE (username)
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role VARCHAR(30) NOT NULL,
    PRIMARY KEY (user_id, role),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES app_user (id)
);

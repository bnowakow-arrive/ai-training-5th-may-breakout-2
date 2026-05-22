CREATE TABLE competitors (
    id      BIGSERIAL    PRIMARY KEY,
    name    VARCHAR(255) NOT NULL,
    domain  VARCHAR(255) NOT NULL,
    is_own  BOOLEAN      NOT NULL DEFAULT FALSE
);

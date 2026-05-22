CREATE TABLE domain_metrics_snapshot (
    id               BIGSERIAL      PRIMARY KEY,
    competitor_id    BIGINT         NOT NULL REFERENCES competitor(id) ON DELETE CASCADE,
    domain           VARCHAR(255)   NOT NULL,
    organic_keywords BIGINT         NOT NULL,
    organic_traffic  BIGINT         NOT NULL,
    organic_cost     NUMERIC(18, 2) NOT NULL,
    top10_keywords   BIGINT         NOT NULL,
    fetched_at       TIMESTAMP      NOT NULL
);

CREATE INDEX idx_domain_metrics_competitor_fetched
    ON domain_metrics_snapshot (competitor_id, fetched_at DESC);

CREATE TABLE domain_traffic_history (
    id                BIGSERIAL    PRIMARY KEY,
    competitor_id     BIGINT       NOT NULL REFERENCES competitors(id) ON DELETE CASCADE,
    month             DATE         NOT NULL,
    organic_traffic   BIGINT       NOT NULL,
    organic_keywords  BIGINT       NOT NULL,
    fetched_at        TIMESTAMPTZ  NOT NULL,
    UNIQUE (competitor_id, month)
);

CREATE INDEX idx_dth_competitor_month ON domain_traffic_history (competitor_id, month);

CREATE TABLE keyword_gap_row (
    id                  BIGSERIAL      PRIMARY KEY,
    competitor_id       BIGINT         NOT NULL REFERENCES competitor(id) ON DELETE CASCADE,
    keyword             VARCHAR(512)   NOT NULL,
    gap_type            VARCHAR(16)    NOT NULL,
    volume              BIGINT         NOT NULL,
    kd                  INTEGER,
    position_base       INTEGER,
    position_competitor INTEGER,
    cpc                 NUMERIC(10, 2),
    fetched_at          TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_keyword_gap_competitor_type
    ON keyword_gap_row (competitor_id, gap_type);

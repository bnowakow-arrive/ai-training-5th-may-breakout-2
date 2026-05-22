CREATE TABLE keyword_gap_row (
    id                   BIGSERIAL     PRIMARY KEY,
    competitor_id        BIGINT        NOT NULL REFERENCES competitors(id) ON DELETE CASCADE,
    keyword              VARCHAR(500)  NOT NULL,
    gap_type             VARCHAR(20)   NOT NULL,
    volume               BIGINT        NOT NULL,
    kd                   INTEGER,
    position_base        INTEGER,
    position_competitor  INTEGER,
    cpc                  NUMERIC(10, 2)
);

CREATE INDEX idx_kgr_competitor_type ON keyword_gap_row (competitor_id, gap_type);

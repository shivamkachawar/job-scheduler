ALTER TABLE outbox_events
ADD COLUMN expired_at TIMESTAMPTZ;

DROP INDEX idx_outbox_events_unpublished;

CREATE INDEX idx_outbox_events_unpublished
    ON outbox_events (created_at)
    WHERE published_at IS NULL
      AND expired_at IS NULL;
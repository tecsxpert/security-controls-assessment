-- Day 3 Java Developer 2 Task
-- Create audit_log table with indexes

CREATE TABLE audit_log (
    id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(100) NOT NULL,
    entity_id BIGINT NOT NULL,
    action_type VARCHAR(30) NOT NULL, -- CREATE / UPDATE / DELETE
    old_data TEXT,
    new_data TEXT,
    changed_by VARCHAR(100),
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Composite index for fast lookup by entity
CREATE INDEX idx_audit_entity
ON audit_log(entity_type, entity_id);

-- Index for sorting/filtering by time
CREATE INDEX idx_audit_changed_at
ON audit_log(changed_at);

-- Index for user-based lookup
CREATE INDEX idx_audit_changed_by
ON audit_log(changed_by);
-- AUDIT LOG TABLE for tracking all operations
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(255) NOT NULL,
    action VARCHAR(50) NOT NULL,
    username VARCHAR(100),
    old_data TEXT,
    new_data TEXT,
    method_name VARCHAR(255),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- INDEXES for audit log queries
CREATE INDEX idx_audit_logs_entity_type ON audit_logs(entity_type);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);
CREATE INDEX idx_audit_logs_username ON audit_logs(username);
CREATE INDEX idx_audit_logs_timestamp ON audit_logs(timestamp);
-- Improve filtering performance
CREATE INDEX IF NOT EXISTS idx_assessment_status_created
ON assessment(status, created_at);

-- Improve search performance
CREATE INDEX IF NOT EXISTS idx_assessment_name_lower
ON assessment(LOWER(name));

-- Improve foreign key lookup
CREATE INDEX IF NOT EXISTS idx_assessment_created_by
ON assessment(created_by);

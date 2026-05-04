CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- INDEX for faster username lookup (login)
CREATE INDEX idx_users_username ON users(username);

-- ASSESSMENT TABLE (core business table)
CREATE TABLE assessment (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL,        -- e.g., PENDING, COMPLETED
    score INT CHECK (score >= 0 AND score <= 100),
    category VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    CONSTRAINT fk_created_by
        FOREIGN KEY (created_by)
        REFERENCES users(id)
        ON DELETE SET NULL
);

-- INDEXES for fast filtering/search
CREATE INDEX idx_assessment_status ON assessment(status);
CREATE INDEX idx_assessment_category ON assessment(category);
CREATE INDEX idx_assessment_created_at ON assessment(created_at);

-- FULL-TEXT LIKE SEARCH SUPPORT (optional basic index)
CREATE INDEX idx_assessment_name ON assessment(name);
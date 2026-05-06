-- Default user accounts for application roles

INSERT INTO users (username, password, role)
VALUES 
('admin', '$2a$10$examplehashedpassword', 'ADMIN'),
('manager', '$2a$10$examplehashedpassword', 'MANAGER'),
('viewer', '$2a$10$examplehashedpassword', 'VIEWER');

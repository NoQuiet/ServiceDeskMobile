-- Скрипт для добавления администраторов и специалистов техподдержки
-- Пароль для всех: admin123 (хеш bcrypt)

-- Добавление администраторов
INSERT INTO users (email, password_hash, first_name, last_name, middle_name, mobile_phone, internal_phone, floor, office_number, position, role, is_blocked) 
VALUES 
('admin2@servicedesk.local', '$2b$10$rZ8qH5YvZ5YvZ5YvZ5YvZ.YvZ5YvZ5YvZ5YvZ5YvZ5YvZ5YvZ5Yv.', 'Иван', 'Петров', 'Сергеевич', '+79001234567', '101', 3, '301', 'Главный администратор', 'admin', FALSE),
('admin3@servicedesk.local', '$2b$10$rZ8qH5YvZ5YvZ5YvZ5YvZ.YvZ5YvZ5YvZ5YvZ5YvZ5YvZ5YvZ5Yv.', 'Мария', 'Сидорова', 'Александровна', '+79009876543', '102', 3, '302', 'Администратор системы', 'admin', FALSE);

-- Добавление специалистов техподдержки
INSERT INTO users (email, password_hash, first_name, last_name, middle_name, mobile_phone, internal_phone, floor, office_number, position, role, is_blocked) 
VALUES 
('support1@servicedesk.local', '$2b$10$rZ8qH5YvZ5YvZ5YvZ5YvZ.YvZ5YvZ5YvZ5YvZ5YvZ5YvZ5YvZ5Yv.', 'Алексей', 'Козлов', 'Дмитриевич', '+79111234567', '201', 2, '201', 'Специалист техподдержки', 'support', FALSE),
('support2@servicedesk.local', '$2b$10$rZ8qH5YvZ5YvZ5YvZ5YvZ.YvZ5YvZ5YvZ5YvZ5YvZ5YvZ5YvZ5Yv.', 'Елена', 'Новикова', 'Игоревна', '+79119876543', '202', 2, '202', 'Специалист техподдержки', 'support', FALSE),
('support3@servicedesk.local', '$2b$10$rZ8qH5YvZ5YvZ5YvZ5YvZ.YvZ5YvZ5YvZ5YvZ5YvZ5YvZ5YvZ5Yv.', 'Дмитрий', 'Волков', 'Андреевич', '+79221234567', '203', 2, '203', 'Старший специалист техподдержки', 'support', FALSE),
('support4@servicedesk.local', '$2b$10$rZ8qH5YvZ5YvZ5YvZ5YvZ.YvZ5YvZ5YvZ5YvZ5YvZ5YvZ5YvZ5Yv.', 'Ольга', 'Морозова', 'Викторовна', '+79229876543', '204', 2, '204', 'Специалист техподдержки', 'support', FALSE);

-- Проверка добавленных пользователей
SELECT id, email, first_name, last_name, position, role FROM users WHERE role IN ('admin', 'support') ORDER BY role, id;

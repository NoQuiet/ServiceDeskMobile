const express = require('express');
const router = express.Router();
const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken');
const { body, validationResult } = require('express-validator');
const db = require('../config/database');
const { authenticateToken } = require('../middleware/auth');

router.post('/register', [
    body('email').isEmail().normalizeEmail(),
    body('password').isLength({ min: 6 }),
    body('first_name').trim().notEmpty(),
    body('last_name').trim().notEmpty(),
    body('mobile_phone').optional().trim(),
    body('internal_phone').optional().trim(),
    body('floor').optional().isInt(),
    body('office_number').optional().trim(),
    body('position').trim().notEmpty()
], async (req, res) => {
    try {
        const errors = validationResult(req);
        if (!errors.isEmpty()) {
            return res.status(400).json({ errors: errors.array() });
        }

        const { email, password, first_name, last_name, middle_name, mobile_phone, 
                internal_phone, floor, office_number, position } = req.body;

        const [existingUsers] = await db.query('SELECT id FROM users WHERE email = ?', [email]);
        if (existingUsers.length > 0) {
            return res.status(400).json({ error: 'Пользователь с таким email уже существует' });
        }

        const passwordHash = await bcrypt.hash(password, 10);

        const [result] = await db.query(
            `INSERT INTO users (email, password_hash, first_name, last_name, middle_name, 
             mobile_phone, internal_phone, floor, office_number, position, role) 
             VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'user')`,
            [email, passwordHash, first_name, last_name, middle_name, mobile_phone, 
             internal_phone, floor, office_number, position]
        );

        res.status(201).json({ 
            message: 'Регистрация успешна',
            userId: result.insertId 
        });
    } catch (error) {
        console.error('Registration error:', error);
        res.status(500).json({ error: 'Ошибка регистрации' });
    }
});

router.post('/login', [
    body('email').isEmail().normalizeEmail(),
    body('password').notEmpty()
], async (req, res) => {
    try {
        const errors = validationResult(req);
        if (!errors.isEmpty()) {
            return res.status(400).json({ errors: errors.array() });
        }

        const { email, password, device_info } = req.body;

        const [users] = await db.query(
            'SELECT * FROM users WHERE email = ?',
            [email]
        );

        if (users.length === 0) {
            return res.status(401).json({ error: 'Неверный email или пароль' });
        }

        const user = users[0];
        
        // Check if account is blocked (is_blocked is TINYINT(1), so check for 1)
        if (user.is_blocked === 1 || user.is_blocked === true) {
            return res.status(403).json({ error: 'Ваша учетная запись заблокирована. Обратитесь к администратору.' });
        }
        
        const isValidPassword = await bcrypt.compare(password, user.password_hash);

        if (!isValidPassword) {
            return res.status(401).json({ error: 'Неверный email или пароль' });
        }

        const token = jwt.sign(
            { userId: user.id, email: user.email, role: user.role },
            process.env.JWT_SECRET,
            { expiresIn: process.env.JWT_EXPIRES_IN || '7d' }
        );

        const expiresAt = new Date();
        expiresAt.setDate(expiresAt.getDate() + 7);

        await db.query(
            'INSERT INTO sessions (user_id, token, device_info, expires_at) VALUES (?, ?, ?, ?)',
            [user.id, token, device_info || null, expiresAt]
        );

        res.json({
            token,
            user: {
                id: user.id,
                email: user.email,
                role: user.role,
                first_name: user.first_name,
                last_name: user.last_name,
                middle_name: user.middle_name,
                mobile_phone: user.mobile_phone,
                internal_phone: user.internal_phone,
                floor: user.floor,
                office_number: user.office_number,
                position: user.position
            }
        });
    } catch (error) {
        console.error('Login error:', error);
        res.status(500).json({ error: 'Ошибка входа' });
    }
});

router.post('/logout', authenticateToken, async (req, res) => {
    try {
        const authHeader = req.headers['authorization'];
        const token = authHeader && authHeader.split(' ')[1];

        await db.query('DELETE FROM sessions WHERE token = ?', [token]);

        res.json({ message: 'Выход выполнен успешно' });
    } catch (error) {
        console.error('Logout error:', error);
        res.status(500).json({ error: 'Ошибка выхода' });
    }
});

router.get('/me', authenticateToken, async (req, res) => {
    try {
        const [users] = await db.query(
            `SELECT id, email, role, first_name, last_name, middle_name, mobile_phone, 
             internal_phone, floor, office_number, position, created_at 
             FROM users WHERE id = ?`,
            [req.user.id]
        );

        if (users.length === 0) {
            return res.status(404).json({ error: 'Пользователь не найден' });
        }

        res.json(users[0]);
    } catch (error) {
        console.error('Get user error:', error);
        res.status(500).json({ error: 'Ошибка получения данных пользователя' });
    }
});

module.exports = router;

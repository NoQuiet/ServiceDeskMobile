const express = require('express');
const router = express.Router();
const bcrypt = require('bcrypt');
const { body, validationResult } = require('express-validator');
const db = require('../config/database');
const { authenticateToken, requireRole } = require('../middleware/auth');

router.get('/', authenticateToken, requireRole('admin', 'support'), async (req, res) => {
    try {
        const { role } = req.query;
        
        console.log(`Get users request - User role: ${req.user.role}, Filter by role: ${role}`);
        
        let query = `SELECT id, email, role, first_name, last_name, middle_name, 
                     mobile_phone, internal_phone, floor, office_number, position, 
                     is_blocked, created_at FROM users`;
        let params = [];

        if (role) {
            query += ' WHERE role = ?';
            params.push(role);
        }

        query += ' ORDER BY created_at DESC';

        const [users] = await db.query(query, params);
        console.log(`Found ${users.length} users`);
        res.json(users);
    } catch (error) {
        console.error('Get users error:', error);
        res.status(500).json({ error: 'Ошибка получения пользователей' });
    }
});

router.get('/:id', authenticateToken, async (req, res) => {
    try {
        const userId = req.params.id;

        if (req.user.role === 'user' && req.user.id !== parseInt(userId)) {
            return res.status(403).json({ error: 'Нет доступа' });
        }

        const [users] = await db.query(
            `SELECT id, email, role, first_name, last_name, middle_name, 
             mobile_phone, internal_phone, floor, office_number, position, 
             is_blocked, created_at FROM users WHERE id = ?`,
            [userId]
        );

        if (users.length === 0) {
            return res.status(404).json({ error: 'Пользователь не найден' });
        }

        res.json(users[0]);
    } catch (error) {
        console.error('Get user error:', error);
        res.status(500).json({ error: 'Ошибка получения пользователя' });
    }
});

router.put('/:id', authenticateToken, async (req, res) => {
    try {
        const userId = req.params.id;

        if (req.user.role === 'user' && req.user.id !== parseInt(userId)) {
            return res.status(403).json({ error: 'Вы можете редактировать только свой профиль' });
        }

        const { first_name, last_name, middle_name, mobile_phone, internal_phone, 
                floor, office_number, position } = req.body;

        const updates = [];
        const values = [];

        if (first_name) { updates.push('first_name = ?'); values.push(first_name); }
        if (last_name) { updates.push('last_name = ?'); values.push(last_name); }
        if (middle_name !== undefined) { updates.push('middle_name = ?'); values.push(middle_name); }
        if (mobile_phone !== undefined) { updates.push('mobile_phone = ?'); values.push(mobile_phone); }
        if (internal_phone !== undefined) { updates.push('internal_phone = ?'); values.push(internal_phone); }
        if (floor !== undefined) { updates.push('floor = ?'); values.push(floor); }
        if (office_number !== undefined) { updates.push('office_number = ?'); values.push(office_number); }
        if (position) { updates.push('position = ?'); values.push(position); }

        if (updates.length === 0) {
            return res.status(400).json({ error: 'Нет данных для обновления' });
        }

        values.push(userId);

        await db.query(
            `UPDATE users SET ${updates.join(', ')} WHERE id = ?`,
            values
        );

        res.json({ message: 'Профиль обновлен' });
    } catch (error) {
        console.error('Update user error:', error);
        res.status(500).json({ error: 'Ошибка обновления профиля' });
    }
});

router.post('/support', authenticateToken, requireRole('admin'), [
    body('email').isEmail().normalizeEmail(),
    body('password').isLength({ min: 6 }),
    body('first_name').trim().notEmpty(),
    body('last_name').trim().notEmpty()
], async (req, res) => {
    try {
        const errors = validationResult(req);
        if (!errors.isEmpty()) {
            return res.status(400).json({ errors: errors.array() });
        }

        const { email, password, first_name, last_name, middle_name } = req.body;

        const [existingUsers] = await db.query('SELECT id FROM users WHERE email = ?', [email]);
        if (existingUsers.length > 0) {
            return res.status(400).json({ error: 'Пользователь с таким email уже существует' });
        }

        const passwordHash = await bcrypt.hash(password, 10);

        const [result] = await db.query(
            `INSERT INTO users (email, password_hash, first_name, last_name, middle_name, 
             role, position) VALUES (?, ?, ?, ?, ?, 'support', 'Специалист техподдержки')`,
            [email, passwordHash, first_name, last_name, middle_name]
        );

        res.status(201).json({ 
            message: 'Специалист техподдержки создан',
            userId: result.insertId 
        });
    } catch (error) {
        console.error('Create support error:', error);
        res.status(500).json({ error: 'Ошибка создания специалиста' });
    }
});

router.patch('/:id/block', authenticateToken, requireRole('admin'), async (req, res) => {
    try {
        const userId = req.params.id;
        const { is_blocked } = req.body;

        if (typeof is_blocked !== 'boolean') {
            return res.status(400).json({ error: 'Неверное значение is_blocked' });
        }

        await db.query('UPDATE users SET is_blocked = ? WHERE id = ?', [is_blocked, userId]);

        if (is_blocked) {
            await db.query('DELETE FROM sessions WHERE user_id = ?', [userId]);
        }

        res.json({ message: is_blocked ? 'Пользователь заблокирован' : 'Пользователь разблокирован' });
    } catch (error) {
        console.error('Block user error:', error);
        res.status(500).json({ error: 'Ошибка блокировки пользователя' });
    }
});

router.delete('/:id', authenticateToken, requireRole('admin'), async (req, res) => {
    try {
        const userId = req.params.id;

        if (req.user.id === parseInt(userId)) {
            return res.status(400).json({ error: 'Нельзя удалить свою учетную запись' });
        }

        await db.query('DELETE FROM users WHERE id = ?', [userId]);

        res.json({ message: 'Пользователь удален' });
    } catch (error) {
        console.error('Delete user error:', error);
        res.status(500).json({ error: 'Ошибка удаления пользователя' });
    }
});

router.put('/:id/admin', authenticateToken, requireRole('admin'), async (req, res) => {
    try {
        const userId = req.params.id;
        const { email, first_name, last_name, middle_name, mobile_phone, internal_phone, 
                floor, office_number, position, role } = req.body;

        const updates = [];
        const values = [];

        if (email) { updates.push('email = ?'); values.push(email); }
        if (first_name) { updates.push('first_name = ?'); values.push(first_name); }
        if (last_name) { updates.push('last_name = ?'); values.push(last_name); }
        if (middle_name !== undefined) { updates.push('middle_name = ?'); values.push(middle_name); }
        if (mobile_phone !== undefined) { updates.push('mobile_phone = ?'); values.push(mobile_phone); }
        if (internal_phone !== undefined) { updates.push('internal_phone = ?'); values.push(internal_phone); }
        if (floor !== undefined) { updates.push('floor = ?'); values.push(floor); }
        if (office_number !== undefined) { updates.push('office_number = ?'); values.push(office_number); }
        if (position) { updates.push('position = ?'); values.push(position); }
        if (role && ['admin', 'support', 'user'].includes(role)) { 
            updates.push('role = ?'); 
            values.push(role); 
        }

        if (updates.length === 0) {
            return res.status(400).json({ error: 'Нет данных для обновления' });
        }

        values.push(userId);

        await db.query(
            `UPDATE users SET ${updates.join(', ')} WHERE id = ?`,
            values
        );

        res.json({ message: 'Данные пользователя обновлены' });
    } catch (error) {
        console.error('Admin update user error:', error);
        res.status(500).json({ error: 'Ошибка обновления пользователя' });
    }
});

module.exports = router;

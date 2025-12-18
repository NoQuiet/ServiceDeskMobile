const express = require('express');
const router = express.Router();
const { body, validationResult } = require('express-validator');
const db = require('../config/database');
const { authenticateToken, requireRole } = require('../middleware/auth');
const upload = require('../config/multer');

router.post('/', authenticateToken, [
    body('title').trim().notEmpty(),
    body('description').trim().notEmpty()
], async (req, res) => {
    try {
        const errors = validationResult(req);
        if (!errors.isEmpty()) {
            return res.status(400).json({ errors: errors.array() });
        }

        const { title, description } = req.body;

        const [users] = await db.query('SELECT position FROM users WHERE id = ?', [req.user.id]);
        const position = users[0].position;

        const isVIP = position && position.toLowerCase().includes('начальник управления');
        const priority = isVIP ? 'vip' : 'normal';
        
        const deadline = new Date();
        deadline.setHours(deadline.getHours() + (isVIP ? 6 : 24));

        const [result] = await db.query(
            `INSERT INTO tickets (user_id, title, description, priority, deadline, status) 
             VALUES (?, ?, ?, ?, ?, 'new')`,
            [req.user.id, title, description, priority, deadline]
        );

        await db.query(
            'INSERT INTO ticket_history (ticket_id, user_id, action, new_value) VALUES (?, ?, ?, ?)',
            [result.insertId, req.user.id, 'created', 'new']
        );

        res.status(201).json({ 
            message: 'Заявка создана',
            ticketId: result.insertId,
            priority: priority,
            id: result.insertId
        });
    } catch (error) {
        console.error('Create ticket error:', error);
        res.status(500).json({ error: 'Ошибка создания заявки' });
    }
});

router.get('/', authenticateToken, async (req, res) => {
    try {
        let query;
        let params = [];

        if (req.user.role === 'admin') {
            query = `SELECT t.*, u.first_name, u.last_name, u.email, u.position,
                     s.first_name as support_first_name, s.last_name as support_last_name
                     FROM tickets t
                     JOIN users u ON t.user_id = u.id
                     LEFT JOIN users s ON t.assigned_to = s.id
                     ORDER BY t.created_at DESC`;
        } else if (req.user.role === 'support') {
            query = `SELECT t.*, u.first_name, u.last_name, u.email, u.position,
                     s.first_name as support_first_name, s.last_name as support_last_name
                     FROM tickets t
                     JOIN users u ON t.user_id = u.id
                     LEFT JOIN users s ON t.assigned_to = s.id
                     WHERE t.status != 'archived'
                     ORDER BY t.priority DESC, t.created_at ASC`;
        } else {
            query = `SELECT t.*, s.first_name as support_first_name, s.last_name as support_last_name
                     FROM tickets t
                     LEFT JOIN users s ON t.assigned_to = s.id
                     WHERE t.user_id = ?
                     ORDER BY t.created_at DESC`;
            params = [req.user.id];
        }

        const [tickets] = await db.query(query, params);
        res.json(tickets);
    } catch (error) {
        console.error('Get tickets error:', error);
        res.status(500).json({ error: 'Ошибка получения заявок' });
    }
});

router.get('/:id', authenticateToken, async (req, res) => {
    try {
        const ticketId = req.params.id;

        const [tickets] = await db.query(
            `SELECT t.*, u.first_name, u.last_name, u.email, u.mobile_phone, u.internal_phone,
             u.floor, u.office_number, u.position,
             s.first_name as support_first_name, s.last_name as support_last_name
             FROM tickets t
             JOIN users u ON t.user_id = u.id
             LEFT JOIN users s ON t.assigned_to = s.id
             WHERE t.id = ?`,
            [ticketId]
        );

        if (tickets.length === 0) {
            return res.status(404).json({ error: 'Заявка не найдена' });
        }

        const ticket = tickets[0];

        if (req.user.role === 'user' && ticket.user_id !== req.user.id) {
            return res.status(403).json({ error: 'Нет доступа к этой заявке' });
        }

        res.json(ticket);
    } catch (error) {
        console.error('Get ticket error:', error);
        res.status(500).json({ error: 'Ошибка получения заявки' });
    }
});

router.patch('/:id/status', authenticateToken, requireRole('admin', 'support'), async (req, res) => {
    try {
        const ticketId = req.params.id;
        const { status } = req.body;

        const validStatuses = ['new', 'in_progress', 'resolved', 'closed', 'archived'];
        if (!validStatuses.includes(status)) {
            return res.status(400).json({ error: 'Недопустимый статус' });
        }

        const [tickets] = await db.query('SELECT status FROM tickets WHERE id = ?', [ticketId]);
        if (tickets.length === 0) {
            return res.status(404).json({ error: 'Заявка не найдена' });
        }

        const oldStatus = tickets[0].status;
        const updateData = { status };

        if (status === 'resolved' || status === 'closed') {
            updateData.resolved_at = new Date();
        }

        if (status === 'archived') {
            updateData.archived_at = new Date();
        }

        const fields = Object.keys(updateData).map(key => `${key} = ?`).join(', ');
        const values = [...Object.values(updateData), ticketId];

        await db.query(`UPDATE tickets SET ${fields} WHERE id = ?`, values);

        await db.query(
            'INSERT INTO ticket_history (ticket_id, user_id, action, old_value, new_value) VALUES (?, ?, ?, ?, ?)',
            [ticketId, req.user.id, 'status_changed', oldStatus, status]
        );

        res.json({ message: 'Статус обновлен' });
    } catch (error) {
        console.error('Update status error:', error);
        res.status(500).json({ error: 'Ошибка обновления статуса' });
    }
});

router.patch('/:id/assign', authenticateToken, requireRole('admin', 'support'), async (req, res) => {
    try {
        const ticketId = req.params.id;
        const { support_id, supportId } = req.body;

        let assignTo = support_id || supportId;
        
        if (req.user.role === 'support' && !assignTo) {
            assignTo = req.user.id;
        }

        if (assignTo) {
            const [support] = await db.query(
                'SELECT id FROM users WHERE id = ? AND role = "support"',
                [assignTo]
            );

            if (support.length === 0) {
                return res.status(400).json({ error: 'Специалист не найден' });
            }
        }

        await db.query('UPDATE tickets SET assigned_to = ? WHERE id = ?', [assignTo, ticketId]);

        await db.query(
            'INSERT INTO ticket_history (ticket_id, user_id, action, new_value) VALUES (?, ?, ?, ?)',
            [ticketId, req.user.id, 'assigned', assignTo]
        );

        res.json({ message: 'Заявка назначена' });
    } catch (error) {
        console.error('Assign ticket error:', error);
        res.status(500).json({ error: 'Ошибка назначения заявки' });
    }
});

router.post('/:id/rating', authenticateToken, [
    body('rating').isInt({ min: 1, max: 5 })
], async (req, res) => {
    try {
        const errors = validationResult(req);
        if (!errors.isEmpty()) {
            return res.status(400).json({ errors: errors.array() });
        }

        const ticketId = req.params.id;
        const { rating } = req.body;

        const [tickets] = await db.query(
            'SELECT user_id, status FROM tickets WHERE id = ?',
            [ticketId]
        );

        if (tickets.length === 0) {
            return res.status(404).json({ error: 'Заявка не найдена' });
        }

        if (tickets[0].user_id !== req.user.id) {
            return res.status(403).json({ error: 'Вы можете оценить только свои заявки' });
        }

        if (tickets[0].status !== 'resolved' && tickets[0].status !== 'closed') {
            return res.status(400).json({ error: 'Можно оценить только выполненные заявки' });
        }

        await db.query(
            'UPDATE tickets SET rating = ?, status = ?, archived_at = NOW() WHERE id = ?', 
            [rating, 'archived', ticketId]
        );

        await db.query(
            'INSERT INTO ticket_history (ticket_id, user_id, action, new_value) VALUES (?, ?, ?, ?)',
            [ticketId, req.user.id, 'status_changed', 'archived']
        );

        res.json({ message: 'Оценка сохранена, заявка перемещена в архив' });
    } catch (error) {
        console.error('Rate ticket error:', error);
        res.status(500).json({ error: 'Ошибка сохранения оценки' });
    }
});

router.get('/archive/all', authenticateToken, requireRole('admin'), async (req, res) => {
    try {
        const [tickets] = await db.query(
            `SELECT t.*, u.first_name, u.last_name, u.email,
             s.first_name as support_first_name, s.last_name as support_last_name
             FROM tickets t
             JOIN users u ON t.user_id = u.id
             LEFT JOIN users s ON t.assigned_to = s.id
             WHERE t.status = 'archived'
             ORDER BY t.archived_at DESC`
        );

        res.json(tickets);
    } catch (error) {
        console.error('Get archive error:', error);
        res.status(500).json({ error: 'Ошибка получения архива' });
    }
});

// Upload attachments to ticket
router.post('/:id/attachments', authenticateToken, upload.array('images', 5), async (req, res) => {
    try {
        const ticketId = req.params.id;
        
        // Verify ticket exists and user has access
        const [tickets] = await db.query(
            'SELECT * FROM tickets WHERE id = ?',
            [ticketId]
        );
        
        if (tickets.length === 0) {
            return res.status(404).json({ error: 'Заявка не найдена' });
        }
        
        if (!req.files || req.files.length === 0) {
            return res.status(400).json({ error: 'Файлы не загружены' });
        }
        
        const attachments = [];
        for (const file of req.files) {
            const [result] = await db.query(
                `INSERT INTO ticket_attachments (ticket_id, file_name, file_path, file_size, mime_type)
                 VALUES (?, ?, ?, ?, ?)`,
                [ticketId, file.originalname, file.filename, file.size, file.mimetype]
            );
            
            attachments.push({
                id: result.insertId,
                fileName: file.originalname,
                filePath: file.filename,
                fileSize: file.size,
                mimeType: file.mimetype
            });
        }
        
        res.status(201).json({ 
            message: 'Файлы загружены',
            attachments: attachments
        });
    } catch (error) {
        console.error('Upload attachments error:', error);
        res.status(500).json({ error: 'Ошибка загрузки файлов' });
    }
});

// Get ticket attachments
router.get('/:id/attachments', authenticateToken, async (req, res) => {
    try {
        const ticketId = req.params.id;
        
        const [attachments] = await db.query(
            'SELECT id, file_name, file_path, file_size, mime_type, created_at FROM ticket_attachments WHERE ticket_id = ?',
            [ticketId]
        );
        
        res.json(attachments);
    } catch (error) {
        console.error('Get attachments error:', error);
        res.status(500).json({ error: 'Ошибка получения файлов' });
    }
});

module.exports = router;

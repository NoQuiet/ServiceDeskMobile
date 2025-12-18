const express = require('express');
const router = express.Router();
const { body, validationResult } = require('express-validator');
const db = require('../config/database');
const { authenticateToken } = require('../middleware/auth');
const upload = require('../config/multer');

router.post('/', authenticateToken, [
    body('ticket_id').isInt(),
    body('message').trim().notEmpty()
], async (req, res) => {
    try {
        const errors = validationResult(req);
        if (!errors.isEmpty()) {
            return res.status(400).json({ errors: errors.array() });
        }

        const { ticket_id, message, is_internal } = req.body;

        const [tickets] = await db.query(
            'SELECT user_id FROM tickets WHERE id = ?',
            [ticket_id]
        );

        if (tickets.length === 0) {
            return res.status(404).json({ error: 'Заявка не найдена' });
        }

        if (req.user.role === 'user' && tickets[0].user_id !== req.user.id) {
            return res.status(403).json({ error: 'Нет доступа к этой заявке' });
        }

        const [result] = await db.query(
            'INSERT INTO comments (ticket_id, user_id, message, is_internal) VALUES (?, ?, ?, ?)',
            [ticket_id, req.user.id, message, is_internal || false]
        );

        res.status(201).json({ 
            message: 'Комментарий добавлен',
            commentId: result.insertId,
            id: result.insertId
        });
    } catch (error) {
        console.error('Create comment error:', error);
        res.status(500).json({ error: 'Ошибка добавления комментария' });
    }
});

router.get('/ticket/:ticketId', authenticateToken, async (req, res) => {
    try {
        const ticketId = req.params.ticketId;

        const [tickets] = await db.query(
            'SELECT user_id FROM tickets WHERE id = ?',
            [ticketId]
        );

        if (tickets.length === 0) {
            return res.status(404).json({ error: 'Заявка не найдена' });
        }

        if (req.user.role === 'user' && tickets[0].user_id !== req.user.id) {
            return res.status(403).json({ error: 'Нет доступа к этой заявке' });
        }

        let query = `SELECT c.*, u.first_name, u.last_name, u.role
                     FROM comments c
                     JOIN users u ON c.user_id = u.id
                     WHERE c.ticket_id = ?`;
        
        if (req.user.role === 'user') {
            query += ' AND c.is_internal = FALSE';
        }

        query += ' ORDER BY c.created_at ASC';

        const [comments] = await db.query(query, [ticketId]);
        
        console.log(`Comments for ticket ${ticketId}:`, comments.length);

        res.json(comments);
    } catch (error) {
        console.error('Get comments error:', error);
        res.status(500).json({ error: 'Ошибка получения комментариев' });
    }
});

router.delete('/:id', authenticateToken, async (req, res) => {
    try {
        const commentId = req.params.id;

        const [comments] = await db.query(
            'SELECT user_id FROM comments WHERE id = ?',
            [commentId]
        );

        if (comments.length === 0) {
            return res.status(404).json({ error: 'Комментарий не найден' });
        }

        if (comments[0].user_id !== req.user.id) {
            return res.status(403).json({ error: 'Вы можете удалить только свой комментарий' });
        }

        await db.query('DELETE FROM comments WHERE id = ?', [commentId]);

        res.json({ message: 'Комментарий удален' });
    } catch (error) {
        console.error('Delete comment error:', error);
        res.status(500).json({ error: 'Ошибка удаления комментария' });
    }
});

// Upload attachments to comment
router.post('/:id/attachments', authenticateToken, upload.array('images', 5), async (req, res) => {
    try {
        const commentId = req.params.id;
        
        // Verify comment exists and user has access
        const [comments] = await db.query(
            'SELECT user_id FROM comments WHERE id = ?',
            [commentId]
        );
        
        if (comments.length === 0) {
            return res.status(404).json({ error: 'Комментарий не найден' });
        }
        
        if (!req.files || req.files.length === 0) {
            return res.status(400).json({ error: 'Файлы не загружены' });
        }
        
        const attachments = [];
        for (const file of req.files) {
            const [result] = await db.query(
                `INSERT INTO comment_attachments (comment_id, file_name, file_path, file_size, mime_type)
                 VALUES (?, ?, ?, ?, ?)`,
                [commentId, file.originalname, file.filename, file.size, file.mimetype]
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
        console.error('Upload comment attachments error:', error);
        res.status(500).json({ error: 'Ошибка загрузки файлов' });
    }
});

// Get comment attachments
router.get('/:id/attachments', authenticateToken, async (req, res) => {
    try {
        const commentId = req.params.id;
        
        const [attachments] = await db.query(
            'SELECT id, file_name, file_path, file_size, mime_type, created_at FROM comment_attachments WHERE comment_id = ?',
            [commentId]
        );
        
        res.json(attachments);
    } catch (error) {
        console.error('Get comment attachments error:', error);
        res.status(500).json({ error: 'Ошибка получения файлов' });
    }
});

module.exports = router;

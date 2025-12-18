const jwt = require('jsonwebtoken');
const db = require('../config/database');

const authenticateToken = async (req, res, next) => {
    try {
        const authHeader = req.headers['authorization'];
        const token = authHeader && authHeader.split(' ')[1];

        if (!token) {
            return res.status(401).json({ error: 'Токен не предоставлен' });
        }

        const decoded = jwt.verify(token, process.env.JWT_SECRET);
        
        const [sessions] = await db.query(
            'SELECT s.*, u.* FROM sessions s JOIN users u ON s.user_id = u.id WHERE s.token = ? AND s.expires_at > NOW() AND u.is_blocked = FALSE',
            [token]
        );

        if (sessions.length === 0) {
            return res.status(401).json({ error: 'Недействительный или истекший токен' });
        }

        req.user = {
            id: sessions[0].user_id,
            email: sessions[0].email,
            role: sessions[0].role,
            first_name: sessions[0].first_name,
            last_name: sessions[0].last_name
        };

        next();
    } catch (error) {
        return res.status(403).json({ error: 'Ошибка аутентификации' });
    }
};

const requireRole = (...roles) => {
    return (req, res, next) => {
        if (!req.user) {
            return res.status(401).json({ error: 'Не авторизован' });
        }

        if (!roles.includes(req.user.role)) {
            return res.status(403).json({ error: 'Недостаточно прав' });
        }

        next();
    };
};

module.exports = { authenticateToken, requireRole };

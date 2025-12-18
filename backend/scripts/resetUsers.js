const bcrypt = require('bcrypt');
const mysql = require('mysql2/promise');
require('dotenv').config();

async function resetUsers() {
    const connection = await mysql.createConnection({
        host: process.env.DB_HOST,
        user: process.env.DB_USER,
        password: process.env.DB_PASSWORD,
        database: process.env.DB_NAME
    });

    const password = 'admin123';
    const passwordHash = await bcrypt.hash(password, 10);

    try {
        // Удаляем пользователей с неправильными хешами
        console.log('Удаление пользователей с MD5 хешами...');
        await connection.query(
            `DELETE FROM users WHERE email IN (
                'admin2@servicedesk.local',
                'admin3@servicedesk.local',
                'support1@servicedesk.local',
                'support2@servicedesk.local',
                'support3@servicedesk.local',
                'support4@servicedesk.local'
            )`
        );
        console.log('✓ Старые пользователи удалены');

        const users = [
            // Администраторы
            {
                email: 'admin2@servicedesk.local',
                firstName: 'Иван',
                lastName: 'Петров',
                middleName: 'Сергеевич',
                mobilePhone: '+79001234567',
                internalPhone: '101',
                floor: 3,
                officeNumber: '301',
                position: 'Главный администратор',
                role: 'admin'
            },
            {
                email: 'admin3@servicedesk.local',
                firstName: 'Мария',
                lastName: 'Сидорова',
                middleName: 'Александровна',
                mobilePhone: '+79009876543',
                internalPhone: '102',
                floor: 3,
                officeNumber: '302',
                position: 'Администратор системы',
                role: 'admin'
            },
            // Специалисты техподдержки
            {
                email: 'support1@servicedesk.local',
                firstName: 'Алексей',
                lastName: 'Козлов',
                middleName: 'Дмитриевич',
                mobilePhone: '+79111234567',
                internalPhone: '201',
                floor: 2,
                officeNumber: '201',
                position: 'Специалист техподдержки',
                role: 'support'
            },
            {
                email: 'support2@servicedesk.local',
                firstName: 'Елена',
                lastName: 'Новикова',
                middleName: 'Игоревна',
                mobilePhone: '+79119876543',
                internalPhone: '202',
                floor: 2,
                officeNumber: '202',
                position: 'Специалист техподдержки',
                role: 'support'
            },
            {
                email: 'support3@servicedesk.local',
                firstName: 'Дмитрий',
                lastName: 'Волков',
                middleName: 'Андреевич',
                mobilePhone: '+79221234567',
                internalPhone: '203',
                floor: 2,
                officeNumber: '203',
                position: 'Старший специалист техподдержки',
                role: 'support'
            },
            {
                email: 'support4@servicedesk.local',
                firstName: 'Ольга',
                lastName: 'Морозова',
                middleName: 'Викторовна',
                mobilePhone: '+79229876543',
                internalPhone: '204',
                floor: 2,
                officeNumber: '204',
                position: 'Специалист техподдержки',
                role: 'support'
            }
        ];

        console.log('\nСоздание пользователей с правильными bcrypt хешами...');
        for (const user of users) {
            await connection.query(
                `INSERT INTO users (email, password_hash, first_name, last_name, middle_name, 
                 mobile_phone, internal_phone, floor, office_number, position, role, is_blocked) 
                 VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, FALSE)`,
                [
                    user.email,
                    passwordHash,
                    user.firstName,
                    user.lastName,
                    user.middleName,
                    user.mobilePhone,
                    user.internalPhone,
                    user.floor,
                    user.officeNumber,
                    user.position,
                    user.role
                ]
            );

            console.log(`✓ Создан: ${user.email} (${user.role})`);
        }

        console.log('\n=== Все пользователи ===');
        const [allUsers] = await connection.query(
            'SELECT id, email, first_name, last_name, position, role FROM users WHERE role IN ("admin", "support") ORDER BY role, id'
        );
        console.table(allUsers);

        console.log('\n=== Данные для входа ===');
        console.log('Email: любой из списка выше');
        console.log('Пароль: admin123');
        console.log('\n✓ Готово! Теперь можете войти в приложение.');

    } catch (error) {
        console.error('Ошибка:', error);
    } finally {
        await connection.end();
    }
}

resetUsers();

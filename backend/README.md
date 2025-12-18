# Service Desk Backend API

Backend сервер для приложения техподдержки на Node.js + Express + MySQL.

## Установка

### Требования
- Node.js 14+
- MySQL 5.7+
- npm или yarn

### Шаги установки

1. Установите зависимости:
```bash
cd backend
npm install
```

2. Создайте файл `.env` на основе `.env.example`:
```bash
cp .env.example .env
```

3. Настройте параметры в `.env`:
```
DB_HOST=localhost
DB_USER=root
DB_PASSWORD=your_password
DB_NAME=service_desk
DB_PORT=3306

JWT_SECRET=your_secret_key_here
JWT_EXPIRES_IN=7d

PORT=3000
NODE_ENV=development
```

4. Создайте базу данных MySQL:
```bash
mysql -u root -p < database/schema.sql
```

5. Запустите сервер:
```bash
npm start
```

Для разработки с автоперезагрузкой:
```bash
npm run dev
```

## API Endpoints

### Аутентификация (`/api/auth`)

#### POST `/api/auth/register`
Регистрация нового пользователя
```json
{
  "email": "user@example.com",
  "password": "password123",
  "first_name": "Иван",
  "last_name": "Иванов",
  "middle_name": "Иванович",
  "mobile_phone": "+79001234567",
  "internal_phone": "1234",
  "floor": 3,
  "office_number": "305",
  "position": "Менеджер"
}
```

#### POST `/api/auth/login`
Вход в систему
```json
{
  "email": "user@example.com",
  "password": "password123",
  "device_info": "Android 12"
}
```

#### POST `/api/auth/logout`
Выход из системы (требует токен)

#### GET `/api/auth/me`
Получить данные текущего пользователя (требует токен)

### Заявки (`/api/tickets`)

#### POST `/api/tickets`
Создать новую заявку (требует токен)
```json
{
  "title": "Не работает принтер",
  "description": "Принтер в кабинете 305 не печатает документы"
}
```

#### GET `/api/tickets`
Получить список заявок (требует токен)
- Обычный пользователь видит только свои заявки
- Специалист видит все активные заявки
- Админ видит все заявки

#### GET `/api/tickets/:id`
Получить детали заявки (требует токен)

#### PATCH `/api/tickets/:id/status`
Изменить статус заявки (требует роль support или admin)
```json
{
  "status": "in_progress"
}
```
Статусы: `new`, `in_progress`, `resolved`, `closed`, `archived`

#### PATCH `/api/tickets/:id/assign`
Назначить заявку на специалиста (требует роль support или admin)
```json
{
  "supportId": 5
}
```
Специалист может взять заявку на себя, не указывая supportId

#### POST `/api/tickets/:id/rating`
Оценить выполненную заявку (требует токен, только автор заявки)
```json
{
  "rating": 5
}
```

#### GET `/api/tickets/archive/all`
Получить архив заявок (требует роль admin)

### Комментарии (`/api/comments`)

#### POST `/api/comments`
Добавить комментарий к заявке (требует токен)
```json
{
  "ticket_id": 1,
  "message": "Проблема в драйвере принтера",
  "is_internal": false
}
```

#### GET `/api/comments/ticket/:ticketId`
Получить все комментарии к заявке (требует токен)

### Пользователи (`/api/users`)

#### GET `/api/users`
Получить список пользователей (требует роль support или admin)
Query параметры:
- `role` - фильтр по роли (admin, support, user)

#### GET `/api/users/:id`
Получить данные пользователя (требует токен)

#### PUT `/api/users/:id`
Обновить профиль пользователя (требует токен)
```json
{
  "first_name": "Иван",
  "last_name": "Петров",
  "mobile_phone": "+79001234567",
  "floor": 4,
  "office_number": "405"
}
```

#### POST `/api/users/support`
Создать специалиста техподдержки (требует роль admin)
```json
{
  "email": "support@example.com",
  "password": "password123",
  "first_name": "Петр",
  "last_name": "Сидоров",
  "middle_name": "Петрович"
}
```

#### PATCH `/api/users/:id/block`
Заблокировать/разблокировать пользователя (требует роль admin)
```json
{
  "is_blocked": true
}
```

#### DELETE `/api/users/:id`
Удалить пользователя (требует роль admin)

#### PUT `/api/users/:id/admin`
Обновить данные пользователя администратором (требует роль admin)
```json
{
  "email": "newemail@example.com",
  "first_name": "Иван",
  "role": "support"
}
```

## Роли пользователей

- **admin** - Администратор системы
  - Полный доступ ко всем функциям
  - Управление пользователями
  - Просмотр архива
  - Назначение заявок

- **support** - Специалист техподдержки
  - Просмотр всех активных заявок
  - Изменение статусов заявок
  - Взятие заявок на себя
  - Комментирование заявок

- **user** - Обычный пользователь
  - Создание заявок
  - Просмотр своих заявок
  - Комментирование своих заявок
  - Оценка выполненных заявок

## Приоритеты заявок

- **VIP** - для должности "Начальник управления" (срок выполнения 6 часов)
- **Normal** - для всех остальных должностей (срок выполнения 24 часа)

## WebSocket события

Сервер поддерживает WebSocket соединения для real-time обновлений:

- `join_ticket` - присоединиться к комнате заявки
- `leave_ticket` - покинуть комнату заявки
- `new_comment` - новый комментарий в заявке
- `status_changed` - изменение статуса заявки

## Безопасность

- Пароли хешируются с использованием bcrypt
- JWT токены для аутентификации
- Валидация всех входных данных
- Проверка прав доступа на уровне middleware
- Защита от SQL инъекций через параметризованные запросы

## Структура базы данных

- `users` - пользователи системы
- `tickets` - заявки
- `comments` - комментарии к заявкам
- `ticket_history` - история изменений заявок
- `sessions` - активные сессии пользователей

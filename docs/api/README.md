# 🔗 API Documentation

Backend API документация.

---

## Документы

| Документ | Описание |
|----------|----------|
| [BACKEND_API_REFERENCE.md](BACKEND_API_REFERENCE.md) | Полный справочник по Backend API |

---

## API Endpoints Overview

### Auth (`/api/v1/auth`)

| Endpoint | Method | Описание |
|----------|--------|----------|
| `/register` | POST | Регистрация |
| `/login` | POST | Вход |
| `/logout` | POST | Выход |
| `/refresh` | POST | Обновление токена |
| `/me` | GET | Текущий пользователь |

### Catalog (`/api/v1`)

| Endpoint | Method | Описание |
|----------|--------|----------|
| `/providers` | GET | Поиск мастеров |
| `/providers/{id}` | GET | Профиль мастера |
| `/providers/{id}/services` | GET | Услуги мастера |
| `/categories` | GET | Категории услуг |

### Booking (`/api/v1`)

| Endpoint | Method | Описание |
|----------|--------|----------|
| `/slots` | GET | Доступные слоты |
| `/bookings` | POST | Создание записи |
| `/bookings/{id}` | GET | Детали записи |
| `/bookings/{id}/cancel` | PATCH | Отмена записи |

---

## Аутентификация

```http
Authorization: Bearer <access_token>
```

**Access Token:** 15 минут
**Refresh Token:** 30 дней (HTTP-only cookie)

---

**Назад:** [← Индекс документации](../00_INDEX.md)

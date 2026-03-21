# Implementation Gap Analysis - Mobile vs Backend

**Дата анализа:** 2026-03-21
**Анализируемая документация:** `docs/business/` (24 файла)

---

## 📊 Executive Summary

### Статус готовности

| Компонент | Backend | Mobile | Gap | Приоритет |
|-----------|---------|--------|-----|-----------|
| **Auth (E1)** | 100% | 30% | 🔴 70% | P0 |
| **Catalog (E2)** | 100% | 0% | 🔴 100% | P0 |
| **Booking (E3)** | 100% | 0% | 🔴 100% | P0 |
| **Profile (E4)** | 80% | 0% | 🔴 80% | P1 |
| **Reviews (E5)** | 0% | 0% | ⚪ 0% | P2 |
| **Notifications (E6)** | 0% | 0% | ⚪ 0% | P2 |
| **i18n (E7)** | 100% | 70% | 🟡 30% | P1 |

**Общий Gap:** Mobile отстаёт на ~80% от готового Backend MVP.

---

## 🔴 Критические пропуски (P0)

### 1. Auth Feature (E1) - 30% готов

| Компонент | Backend API | Mobile UI | Status |
|-----------|-------------|-----------|--------|
| Login | ✅ POST /auth/login | ✅ LoginScreen | Done |
| Registration | ✅ POST /auth/register | ❌ Отсутствует | **TODO** |
| Role Selection | ✅ Multi-role support | ❌ Отсутствует | **TODO** |
| Role Switching | ✅ PATCH /auth/context | ❌ Отсутствует | **TODO** |
| Profile Edit | ✅ PATCH /users/me | ❌ Отсутствует | **TODO** |
| Email Verification | ✅ SendGrid integration | ❌ Отсутствует | **TODO** |

**Что нужно реализовать:**
1. `RegistrationScreen` - форма регистрации
2. `RoleSelectionScreen` - выбор client/provider
3. `ProfileEditScreen` - редактирование профиля
4. `RoleSwitcher` - переключение контекста

### 2. Catalog Feature (E2) - 0% готов

| Компонент | Backend API | Mobile UI | Status |
|-----------|-------------|-----------|--------|
| Providers List | ✅ GET /providers | ❌ Отсутствует | **TODO** |
| Geo-Search | ✅ PostGIS enabled | ❌ Отсутствует | **TODO** |
| Categories | ✅ GET /categories | ❌ Отсутствует | **TODO** |
| Provider Profile | ✅ GET /providers/{id} | ❌ Отсутствует | **TODO** |
| Filters | ✅ Query params | ❌ Отсутствует | **TODO** |
| Map View | N/A (Google Maps) | ❌ Отсутствует | **TODO** |

**Что нужно реализовать:**
1. `HomeScreen` - главный экран с картой
2. `ProviderListScreen` - список провайдеров
3. `ProviderProfileScreen` - профиль провайдера
4. `FilterBottomSheet` - фильтры поиска
5. Google Maps интеграция

### 3. Booking Feature (E3) - 0% готов

| Компонент | Backend API | Mobile UI | Status |
|-----------|-------------|-----------|--------|
| Services | ✅ GET /providers/{id}/services | ❌ Отсутствует | **TODO** |
| Time Slots | ✅ GET /slots | ❌ Отсутствует | **TODO** |
| Create Booking | ✅ POST /bookings | ❌ Отсутствует | **TODO** |
| My Bookings | ✅ GET /bookings | ❌ Отсутствует | **TODO** |
| Cancel Booking | ✅ PATCH /bookings/{id}/cancel | ❌ Отсутствует | **TODO** |

**Что нужно реализовать:**
1. `ServiceSelectionScreen` - выбор услуг
2. `CalendarScreen` - выбор даты
3. `TimeSlotsScreen` - выбор времени
4. `BookingConfirmationScreen` - подтверждение
5. `MyBookingsScreen` - список записей

---

## 🟡 Средние пропуски (P1)

### 4. i18n Feature (E7) - 70% готов

| Компонент | Требование | Реализация | Status |
|-----------|------------|------------|--------|
| AppLocale enum | RU/HE/EN + RTL | ✅ Done | Done |
| StringKey | 100+ keys | ✅ 76+ keys | Done |
| DefaultStrings | 3 languages | ✅ Done | Done |
| RTL LayoutDirection | Dynamic switch | ⚠️ Static | **IMPROVE** |
| Date/Currency Formatters | Locale-aware | ❌ Missing | **TODO** |
| Language Selection Screen | UI | ❌ Missing | **TODO** |

**Что нужно добавить:**
1. `LanguageSelectionScreen` - выбор языка
2. `DateFormatUtils` - форматирование дат
3. `CurrencyFormatUtils` - форматирование валют

### 5. Profile Feature (E4) - 0% готов

| Компонент | Backend API | Mobile UI | Status |
|-----------|-------------|-----------|--------|
| Get Profile | ✅ GET /users/me | ❌ Отсутствует | **TODO** |
| Update Profile | ✅ PATCH /users/me | ❌ Отсутствует | **TODO** |
| Change Password | ✅ POST /auth/change-password | ❌ Отсутствует | **TODO** |
| Settings | N/A | ❌ Отсутствует | **TODO** |

---

## ⚪ Phase 2 Features (P2)

### 6. Reviews (E5) - 0% готов
- Backend не готов
- Mobile не требуется до Phase 2

### 7. Notifications (E6) - 0% готов
- Backend не готов
- Mobile не требуется до Phase 2

---

## 📅 Рекомендуемый Timeline

### Week 1-2: Auth Complete (P0)
- [ ] Registration flow
- [ ] Role selection
- [ ] Profile editing
- [ ] Role switching

### Week 3-4: Catalog Core (P0)
- [ ] Home screen с картой
- [ ] Provider list
- [ ] Provider profile
- [ ] Basic filters

### Week 5-6: Booking Flow (P0)
- [ ] Service selection
- [ ] Calendar + Time slots
- [ ] Booking confirmation
- [ ] My bookings list

### Week 7-8: Polish + i18n (P1)
- [ ] Language selection
- [ ] Date/currency formatters
- [ ] RTL testing
- [ ] Settings screen

---

## 🎯 MVP Success Criteria

### Minimum Viable Product должен включать:

1. **Auth:** Login + Registration + Role selection
2. **Catalog:** Map view + Provider list + Provider profile
3. **Booking:** Service selection + Calendar + Confirmation
4. **i18n:** 3 languages + RTL + Language switcher

### Без этих функций MVP не имеет ценности:
- ❌ Пользователь не может зарегистрироваться
- ❌ Пользователь не может найти мастера
- ❌ Пользователь не может записаться

---

## 📊 Risk Assessment

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Timeline slip | High | High | Prioritize P0 only |
| API integration issues | Medium | Medium | Use mock API first |
| RTL bugs | Medium | Low | Test with Hebrew speaker |
| Map integration complexity | High | Medium | Start with list view |

---

**Анализ проведён:** 2026-03-21
**Следующий review:** После завершения Auth Complete (Week 2)

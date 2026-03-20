# Secrets Management

Управление секретами (API keys, tokens) для проекта.

## Файлы

| Файл | Назначение |
|------|------------|
| `secrets.properties.template` | Шаблон с placeholder'ами |

## Локальная разработка

### 1. Создайте локальный файл секретов

```bash
# Из корня проекта
cp config/secrets/secrets.properties.template secrets.properties
```

### 2. Заполните реальные значения

```properties
# secrets.properties
api.key=your_actual_api_key_here
map.api.key=your_google_maps_key
analytics.key=your_analytics_key
```

### 3. Файл автоматически загружается

Gradle загружает `secrets.properties` из корня проекта (см. `build.gradle.kts`).

## CI/CD (GitHub Actions)

Секреты передаются через environment variables:

```yaml
env:
  API_KEY: ${{ secrets.API_KEY }}
  MAP_API_KEY: ${{ secrets.MAP_API_KEY }}
```

## Доступные параметры

| Параметр | Описание | Обязательный |
|----------|----------|--------------|
| `api.key` | API ключ бэкенда | Да |
| `map.api.key` | Google Maps API ключ | Нет |
| `analytics.key` | Ключ аналитики | Нет |

## Безопасность

### ✅ DO
- Используйте `secrets.properties.template` как шаблон
- Добавляйте реальные ключи только в `secrets.properties` (в .gitignore)
- Используйте GitHub Secrets для CI/CD

### ❌ DON'T
- Не коммитьте `secrets.properties`
- Не хардкодьте ключи в коде
- Не логгируйте API ключи

## Связанные файлы

- `build.gradle.kts` - загрузка секретов
- `androidApp/build.gradle.kts` - BuildConfig поля
- `core:config/` - runtime доступ к конфигурации
- `.gitignore` - исключение secrets.properties

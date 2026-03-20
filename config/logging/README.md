# Logging Configuration

Конфигурации логирования для Android приложения.

## Файлы

| Файл | Назначение |
|------|------------|
| `logback.xml` | Конфигурация Logback для Android |

## Архитектура логирования

```
┌─────────────────────────────────────────┐
│           SLF4J API                     │
│  (общий интерфейс логирования)          │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│           Logback                       │
│  (реализация для Android)               │
│  - LogcatAppender (debug)               │
│  - FileAppender (crash analysis)        │
└─────────────────────────────────────────┘
```

## Уровни логирования

| Logger | Debug | Release |
|--------|-------|---------|
| `com.aggregateservice` | DEBUG | WARN |
| `io.ktor` | DEBUG | OFF |
| `org.koin` | ERROR | ERROR |
| Root | WARN | ERROR |

## Appenders

### LOGCAT (Console)
- Вывод в Android Logcat
- Используется для debug сборок

### FILE (Rolling)
- Файлы: `{LOG_DIR}/aggregate-service.log`
- Ротация: по дням
- История: 7 дней
- Лимит: 10MB

## Изменение конфигурации

Файл копируется в `androidApp/src/androidMain/assets/` при сборке через Gradle task `copyLogbackConfig`.

## Связанные файлы

- `androidApp/build.gradle.kts` - Copy task
- `androidApp/src/androidMain/.../MainApplication.kt` - инициализация

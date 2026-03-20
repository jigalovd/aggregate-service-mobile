# Configuration Directory

Единая директория для всех конфигурационных файлов проекта aggregate-mobile.

## Структура

```
config/
├── quality/          # Инструменты качества кода (detekt, ktlint)
├── logging/          # Конфигурации логирования (logback)
└── secrets/          # Шаблоны секретов (API keys)
```

## Быстрый справочник

| Что нужно изменить | Где искать |
|-------------------|------------|
| Правила Detekt | `quality/detekt.yml` |
| Правила ktlint | `quality/.editorconfig` |
| Настройки логирования | `logging/logback.xml` |
| Шаблон секретов | `secrets/secrets.properties.template` |

## Иерархия конфигураций проекта

1. **gradle.properties** (корень) - настройки Gradle JVM
2. **gradle/libs.versions.toml** - версии зависимостей
3. **build-logic/** - convention plugins
4. **config/** - ресурсные конфиги (этот каталог)
5. **core:config/** - runtime конфигурация (expect/actual)

## Связанная документация

- [CONFIG_MANAGEMENT.md](../docs/CONFIG_MANAGEMENT.md) - управление конфигурацией
- [BUILD_LOGIC.md](../docs/BUILD_LOGIC.md) - Gradle сборка

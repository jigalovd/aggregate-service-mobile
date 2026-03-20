# Quality Tools Configuration

Конфигурации инструментов статического анализа кода.

## Файлы

| Файл | Назначение |
|------|------------|
| `detekt.yml` | Правила статического анализа Kotlin (Detekt) |
| `.editorconfig` | Правила форматирования (ktlint) |

## Использование

### Detekt

```bash
# Проверка всех модулей
./gradlew detektAll

# Проверка конкретного модуля
./gradlew :feature:auth:detekt
```

### Ktlint

```bash
# Проверка форматирования
./gradlew ktlintCheckAll

# Автоисправление
./gradlew ktlintFormatAll
```

## Добавление новых правил

### Detekt

Отредактируйте `detekt.yml`:

```yaml
complexity:
  LongMethod:
    threshold: 60
    excludes: ["**/test/**"]
```

### Ktlint

Отредактируйте `.editorconfig`:

```ini
[*.{kt,kts}]
ktlint_code_style = intellij_idea
ktlint_max_line_length = 120
```

## Связанные файлы

- Корневой `build.gradle.kts` - применение плагинов
- `build-logic/detekt-configuration.gradle.kts` - convention plugin
- `build-logic/ktlint-configuration.gradle.kts` - convention plugin

# Code Quality Guide: Detekt + Ktlint

Этот гайд объясняет как пользоваться инструментами проверки качества кода в проекте.

## 📋 Содержание

- [Обзор инструментов](#обзор-инструментов)
- [Detekt](#detekt)
  - [Запуск проверки](#запуск-проверки)
  - [Конфигурация](#конфигурация)
  - [Baseline для существующего кода](#baseline-для-существующего-кода)
  - [Игнорирование предупреждений](#игнорирование-предупреждений)
- [Ktlint](#ktlint)
  - [Запуск проверки](#запуск-проверки-1)
  - [Автоформатирование](#автоформатирование)
  - [Конфигурация](#конфигурация-1)
- [Интеграция с IDE](#интеграция-с-ide)
- [CI/CD интеграция](#cicd-интеграция)
- [Чеклист перед коммитом](#чеклист-перед-коммитом)

---

## Обзор инструментов

### Detekt
**Назначение**: Static analysis для Kotlin кода
**Что проверяет**:
- Сложность кода (cyclomatic complexity)
- Потенциальные баги (null pointer exceptions, race conditions)
- Дублирование кода
- Нарушения code style
- Проблемы с корутинами
- Performance issues

**Философия проекта**: Zero tolerance — `maxIssues: 0`, build не должен проходить с предупреждениями.

### Ktlint
**Назначение**: Linter и formatter для Kotlin кода
**Что проверяет**:
- Отступы и пробелы
- Длину строк
- Именование переменных/функций/классов
- Импорты (unused imports, wildcard imports)
- Стиль написания кода

**Философия проекта**: Автоисправление максимально возможного количества нарушений.

---

## Detekt

### Запуск проверки

#### Проверить все модули
```bash
./gradlew detektAll
```

#### Проверить конкретный модуль
```bash
./gradlew :core:network:detekt
./gradlew :feature:auth:detekt
```

#### Проверить с генерацией baseline
```bash
./gradlew detektBaseline
```

### Конфигурация

Файл конфигурации: `.detekt/config.yml`

#### Ключевые настройки

```yaml
build:
  maxIssues: 0  # Zero tolerance — ни одного предупреждения

console-reports:
  active: true

output-reports:
  active: true
  exclude:
  - 'TxtOutputReport'
  - 'MdOutputReport'
```

#### Правила разделены на группы:

**complexity** — Сложность кода
```yaml
complexity:
  active: true
  LongMethod:
    threshold: 60  # Максимальная длина метода
  LongParameterList:
    threshold: 6   # Максимальное количество параметров
```

**coroutines** — Работа с корутинами
```yaml
coroutines:
  active: true
  GlobalCoroutineUsage:
    active: true  # Запрет GlobalScope.launch
```

**potential-bugs** — Потенциальные баги
```yaml
potential-bugs:
  active: true
  DontDowncastCollectionTypes:
    active: true
  ImplicitDefaultLocale:
    active: true
```

**style** — Стиль кода
```yaml
style:
  active: true
  MagicNumber:
    active: true
    excludes: ['**/test/**']  # Исключить тесты
```

### Baseline для существующего кода

При первом запуске Detekt найдет много проблем в существующем коде. Вместо фикса всех проблем сразу:

1. Сгенерируйте baseline:
```bash
./gradlew detektBaseline
```

2. Файл `.detekt/baseline.xml` создастся автоматически с текущими проблемами
3. Последующие запуски будут сообщать только о **новых** проблемах
4. Постепенно фиксите проблемы и удаляйте их из baseline

#### Отключить baseline
```yaml
# .detekt/config.yml
build:
  baseline: false
```

### Игнорирование предупреждений

#### На уровне файла

```kotlin
@file:Suppress("ClassName", "FunctionMaxLength")

class MyVeryLongClassName {
    // ...
}
```

#### На уровня выражения

```kotlin
@Suppress("MagicNumber")
val size = 42 //.MagicNumber не будет сообщаться
```

#### На уровне проекта через конфиг

```yaml
# .detekt/config.yml
potential-bugs:
  ImplicitDefaultLocale:
    active: false  # Отключить правило полностью
```

#### Исключить файлы/папки

```yaml
# .detekt/config.yml
processing:
  exclude:
    - '**/generated/**'
    - '**/build/**'
    - '**/test/**'
```

### Отчеты

После запуска Detekt генерирует отчеты:

- **HTML**: `build/reports/detekt/detekt.html` — для людей
- **SARIF**: `build/reports/detekt/detekt.sarif` — для GitHub Actions
- **XML**: `build/reports/detekt/detekt.xml` — для CI/CD

```bash
# Открыть HTML отчет в браузере
open build/reports/detekt/detekt.html
```

---

## Ktlint

### Запуск проверки

#### Проверить все модули
```bash
./gradlew ktlintCheckAll
```

#### Проверить конкретный модуль
```bash
./gradlew :core:network:ktlintCheck
./gradlew :feature:auth:ktlintCheck
```

### Автоформатирование

**ВАЖНО**: Ktlint может автоматически исправить большинство нарушений.

#### Отформатировать все модули
```bash
./gradlew ktlintFormatAll
```

#### Отформатировать конкретный модуль
```bash
./gradlew :core:network:ktlintFormat
./gradlew :feature:auth:ktlintFormat
```

#### Алгоритм работы с ktlint:
1. Пишете код
2. Запускаете `./gradlew ktlintCheckAll`
3. Если есть ошибки — запускаете `./gradlew ktlintFormatAll`
4. Повторяете пункт 2

### Конфигурация

Файл конфигурации: `.editorconfig`

#### Ключевые настройки

```ini
[*.{kt,kts}]
charset = utf-8
end_of_line = lf
indent_size = 4
indent_style = space
insert_final_newline = true
max_line_length = off  # Отключить проверку длины строк
tab_width = 4
trim_trailing_whitespace = true
```

#### Отключение правил для KMP

Compose Multiplatform генерирует код, который не соответствует ktlint правилам. Отключаем проблемные правила:

```ini
[*.{kt,kts}]
ktlint_standard_function-signature = disabled
ktlint_standard_class-naming = disabled
ktlint_standard_function-expression-body = disabled
```

### Игнорирование предупреждений

#### На уровне файла

```kotlin
@file:Suppress("ktlint:standard:class-naming")

class bad_class_name {  // ktlint не будет жаловаться
    // ...
}
```

#### На уровня выражения

```kotlin
val items =
    listOf(1, 2, 3) // @ktlint disable
```

#### На уровне проекта через .editorconfig

```ini
[*.kt]
ktlint_standard_no-wildcard-imports = disabled
```

#### Исключить файлы через Gradle

В ktlint-configuration.gradle.kts:

```kotlin
ktlint {
    filter {
        exclude("**/generated/**")
    }
}
```

---

## Интеграция с IDE

### IntelliJ IDEA / Android Studio

#### Detekt
1. Установите плагин: **Settings → Plugins → Detekt**
2. Настройте путь к конфигу: **Settings → Tools → Detekt → Configuration path**
3. Запуск через: **Tools → Detekt → Inspect**

#### Ktlint
1. Установите плагин: **Settings → Plugins → Ktlint**
2. Форматирование: **Code → Reformat Code** (Ctrl+Alt+L / Cmd+Opt+L)
3. Настройте服从 `.editorconfig`: **Settings → Editor → Code Style → Kotlin → Enable EditorConfig support**

### VS Code

#### Detekt
Установите расширение: **Kotlin Language** (встроена поддержка detekt)

#### Ktlint
1. Установите расширение: **Kotlin Language**
2. Настройте format on save:
```json
{
  "editor.formatOnSave": true,
  "[kotlin]": {
    "editor.defaultFormatter": "fwcd.kotlin"
  }
}
```

---

## CI/CD интеграция

### GitHub Actions

Детект и ktlint автоматически запускаются в CI через `.github/workflows/ci.yml`:

```yaml
name: CI

on: [push, pull_request]

jobs:
  quality:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Run Detekt
        run: ./gradlew detektAll

      - name: Run Ktlint
        run: ./gradlew ktlintCheckAll

      - name: Upload Detekt Report
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: detekt-report
          path: build/reports/detekt/
```

### Pre-commit hooks (Lefthook)

Автоматическая проверка перед коммитом через `lefthook.yml`:

```yaml
pre-commit:
  parallel: true
  commands:
    detekt:
      run: ./gradlew detektAll --daemon
    ktlint:
      run: ./gradlew ktlintCheckAll --daemon
```

---

## Чеклист перед коммитом

### Перед созданием Pull Request

1. **Запустите проверки**:
```bash
./gradlew detektAll
./gradlew ktlintCheckAll
```

2. **Если ktlint находит ошибки**:
```bash
./gradlew ktlintFormatAll
```

3. **Если detekt находит ошибки**:
   - Посмотрите HTML отчет: `build/reports/detekt/detekt.html`
   - Исправьте проблемы или добавьте `@Suppress` с объяснением

4. **Проверьте локально**:
```bash
./gradlew build
```

5. **Создайте PR** — CI автоматически запустит все проверки

### Ежедневная работа

1. Включите auto-format на save в IDE
2. Следите за warnings в IDE
3. Читайте Detekt отчеты хотя бы раз в неделю
4. Постепенно убирайте issues из baseline

### Политика zero tolerance

**Что это значит**:
- Build с предупреждениями = failed build
- PR с предупреждениями не может быть смержен
- Используйте `@Suppress` только с обоснованием в комментарии

**Пример правильного Suppress**:
```kotlin
// Suppress:MagicNumber — значение 42 взято из требований к API
@Suppress("MagicNumber")
val timeout = 42
```

---

## Дополнительные ресурсы

### Detekt
- [Официальная документация](https://detekt.dev/)
- [Правила Detekt](https://detekt.dev/rules/)
- [Конфигурация](https://detekt.dev/configurations/)

### Ktlint
- [Официальная документация](https://pinterest.github.io/ktlint/)
- [EditorConfig](https://editorconfig.org/)
- [Ktlint Gradle Plugin](https://github.com/JLLeitschuh/ktlint-gradle)

### Полезные команды

```bash
# Показать все доступные задачи
./gradlew tasks --group=verification

# Очистить baseline
rm .detekt/baseline.xml

# Показать детали Detekt правила
./gradlew detekt --info

#Dry run для Ktlint (без изменений)
./gradlew ktlintCheckAll --dry-run
```

---

## Troubleshooting

### Detekt: "Too many issues"

**Проблема**: Build падает с тысячами ошибок на существующем коде

**Решение**:
1. Сгенерируйте baseline: `./gradlew detektBaseline`
2. Зафиксируйте `.detekt/baseline.xml` в git
3. Постепенно фиксите проблемы

### Ktlint:生成的 код не соответствует правилам

**Проблема**: Compose Resources генерирует `Res.kt` с нарушениями

**Решение**: Уже настроено в `.editorconfig`:
```ini
ktlint_standard_class-naming = disabled
ktlint_standard_function-expression-body = disabled
```

### Detekt: OutOfMemoryError

**Решение**: Увеличьте heap size в `gradle.properties`:
```properties
org.gradle.jvmargs=-Xmx4096m -XX:MaxMetaspaceSize=512m
```

### Ktlint: Slow on large projects

**Решение**: Используйте `--parallel` (Gradle 8+):
```bash
./gradlew ktlintCheckAll --parallel
```

---

## Часто задаваемые вопросы

**Q: Нельзя ли просто игнорировать все предупреждения?**

A: Можно, но тогда смысл инструментов теряется. Мы используем политику zero tolerance для поддержания качества кода на высоком уровне.

**Q: Почему baseline не создается автоматически?**

A: Baseline нужно создать один раз вручную: `./gradlew detektBaseline`. После этого он будет обновляться автоматически.

**Q: Можно ли использовать Detekt и Ktlint одновременно?**

A: Да, они дополняют друг друга: Detekt ищет логические ошибки, Ktlint — стиль violations.

**Q: Что делать если Detekt считает правильный код ошибочным?**

A:
1. Проверьте что это не false positive в [issue tracker Detekt](https://github.com/detekt/detekt/issues)
2. Если это действительно false positive — добавьте `@Suppress` с комментарием
3. Рассмотрите возможность отключения правила в конфиге если оно часто дает false positives

**Q: Как часто нужно запускать проверки?**

A:
- **Minimum**: Перед каждым PR (в CI автоматически)
- **Recommended**: Перед каждым коммитом (через pre-commit hooks)
- **Ideal**: Continuous checking в IDE (плагины показывают проблемы в реальном времени)

---

## Резюме

| Инструмент | Назначение | Команда проверки | Команда фикса |
|-----------|-----------|-----------------|---------------|
| **Detekt** | Static analysis | `./gradlew detektAll` | Исправить вручную или `@Suppress` |
| **Ktlint** | Linter + Formatter | `./gradlew ktlintCheckAll` | `./gradlew ktlintFormatAll` |

**Zero tolerance** — это не жестокость, а забота о качестве кода и будущем поддержке проекта.

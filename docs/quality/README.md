# ✅ Quality Documentation

Качество кода и тестирование.

---

## Документы

| Документ | Описание |
|----------|----------|
| [CODE_QUALITY_GUIDE.md](CODE_QUALITY_GUIDE.md) | Гайд по Detekt и Ktlint |
| [TESTING_INFRASTRUCTURE.md](TESTING_INFRASTRUCTURE.md) | Инфраструктура тестирования |
| [TESTING_QUICK_START.md](TESTING_QUICK_START.md) | Быстрый старт по тестированию |

---

## Инструменты качества

| Инструмент | Версия | Назначение | Статус |
|------------|--------|------------|--------|
| **Detekt** | 1.23.8 | Static analysis | ✅ 0 warnings |
| **Ktlint** | 13.1.0 | Linter + Formatter | ✅ 100% compliance |
| **Kover** | 0.9.7 | Test coverage | 🔄 25% (target: 60%+) |

## Zero Tolerance Policy

```yaml
# .detekt/config.yml
config:
  maxIssues: 0  # Zero tolerance
```

## Запуск проверок

```bash
# Detekt
./gradlew detektAll

# Ktlint
./gradlew ktlintCheckAll
./gradlew ktlintFormatAll

# Coverage
./gradlew koverReportAll
```

---

**Назад:** [← Индекс документации](../00_INDEX.md)

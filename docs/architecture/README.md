# 🏗️ Architecture Documentation

Технологический стек и архитектура проекта.

---

## Документы

| Документ | Описание |
|----------|----------|
| [01_KMP_CMP_ANALYSIS.md](01_KMP_CMP_ANALYSIS.md) | Анализ Kotlin Multiplatform + Compose Multiplatform |
| [TECHNOLOGY_STACK_ANALYSIS.md](TECHNOLOGY_STACK_ANALYSIS.md) | Анализ технологического стека (плюсы/минусы) |
| [NETWORK_LAYER.md](NETWORK_LAYER.md) | Network Layer архитектура (Ktor 3.4.1) |
| [BUILD_LOGIC.md](BUILD_LOGIC.md) | Build Logic & Convention Plugins |
| [CONFIG_MANAGEMENT.md](CONFIG_MANAGEMENT.md) | Централизованное управление конфигурацией |
| [CONFIG_IMPLEMENTATION_SUMMARY.md](CONFIG_IMPLEMENTATION_SUMMARY.md) | Сводка по конфигурации |
| [Анализ_подходов_к_разработке_интерфейсов.md](Анализ_подходов_к_разработке_интерфейсов.md) | Анализ подходов к разработке UI |

---

## Ключевые решения

### Архитектурный стиль

**Feature-First + Clean Architecture**

```
feature/
├── domain/      # Entities, UseCases, Repository interfaces
├── data/        # Repository implementations, DTOs, API
└── presentation/ # ScreenModels, UI State, Compose Screens
```

### Технологический стек

| Компонент | Технология | Версия |
|-----------|------------|--------|
| Language | Kotlin | 2.2.20 |
| UI | Compose Multiplatform | 1.10.2 |
| Network | Ktor | 3.4.1 |
| DI | Koin | 4.2.0 |
| Navigation | Voyager | 1.1.0-beta02 |

---

**Назад:** [← Индекс документации](../00_INDEX.md)

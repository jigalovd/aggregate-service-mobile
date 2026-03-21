# 🎨 Design Documentation

UI/UX дизайн и Design System.

---

## Документы

| Документ | Описание |
|----------|----------|
| [04_DESIGN_SYSTEM.md](04_DESIGN_SYSTEM.md) | Design System базового уровня |
| [05_UX_GUIDELINES.md](05_UX_GUIDELINES.md) | UX Guidelines |
| [02_MAP_PROVIDERS_ANALYSIS.md](02_MAP_PROVIDERS_ANALYSIS.md) | Сравнительный анализ поставщиков карт |

---

## Ключевые принципы

### Material 3 Design

- **Color Scheme**: Light/Dark theme с 77+ цветов
- **Typography**: 15 стилей типографики
- **Shapes**: 5 категорий форм компонентов
- **Spacing**: 8dp grid system

### RTL Support

Поддержка RTL языков (иврит, арабский):

```kotlin
appTheme(
    languageCode = "he"  // Автоматический RTL
) {
    // Content
}
```

---

**Назад:** [← Индекс документации](../00_INDEX.md)

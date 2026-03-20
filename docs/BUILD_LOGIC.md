# 🔧 Build Logic & Convention Plugins - Gradle KMP

**Дата создания**: 2026-03-19
**Последнее обновление**: 2026-03-20
**Статус**: ✅ Complete (100%)
**Gradle Version**: 8.14.4 (Kotlin DSL)

---

## 📋 Обзор

Build Logic организован через **Convention Plugins** для обеспечения консистентности конфигурации всех модулей KMP проекта.

### 🎯 Преимущества Convention Plugins

| Преимущество | Описание |
|--------------|----------|
| **DRY Principle** | Конфигурация пишется один раз |
| **Type-Safety** | Kotlin DSL обеспечивает compile-time проверку |
| **Consistency** | Все модули используют единые стандарты |
| **Maintainability** | Изменения в одном месте применяются ко всем модулям |
| **Team Scalability** | Новые разработчики быстро понимают структуру |

---

## 🏗️ Структура Build Logic

```
build-logic/
├── build.gradle.kts                    # Конфигурация build-logic модуля
├── settings.gradle.kts                 # Gradle Plugin Portal
└── src/main/kotlin/
    ├── kmp-base.gradle.kts             # Базовая KMP конфигурация
    ├── kmp-android.gradle.kts          # Android target конфигурация
    ├── kmp-compose.gradle.kts          # Compose Multiplatform настройка
    ├── core-module.gradle.kts          # Convention для core модулей
    ├── feature-module.gradle.kts       # Convention для feature модулей
    ├── app-module.gradle.kts           # Convention для app модулей
    ├── detekt-configuration.gradle.kts # Detekt static analysis
    └── ktlint-configuration.gradle.kts # Ktlint formatter
```

---

## 📦 Convention Plugins

### 1. kmp-base.gradle.kts

**Назначение**: Базовая конфигурация для всех KMP модулей

```kotlin
plugins {
    kotlin("multiplatform")
}

configure<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension> {
    // iOS targets только на macOS
    if(System.getProperty("os.name").contains("Mac", ignoreCase = true)) {
        listOf(
            iosX64(),
            iosArm64(),
            iosSimulatorArm64()
        ).forEach { iosTarget ->
            iosTarget.binaries.framework {
                baseName = project.name
                isStatic = true
            }
        }
    }
}
```

**Что настраивает:**
- ✅ iOS targets (x64, arm64, simulator)
- ✅ Framework configuration
- ✅ macOS detection

---

### 2. kmp-android.gradle.kts

**Назначение**: Android target конфигурация

```kotlin
plugins {
    id("com.android.library")
}

android {
    compileSdk = 36
    namespace = "com.aggregateservice.${project.name}"

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "21"
            }
        }
    }
}
```

**Что настраивает:**
- ✅ Compile SDK: 36
- ✅ Min SDK: 24
- ✅ JVM Target: 21
- ✅ Namespace convention

---

### 3. kmp-compose.gradle.kts

**Назначение**: Compose Multiplatform настройка

```kotlin
plugins {
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

compose {
    desktop {}
}
```

**Что настраивает:**
- ✅ Compose Compiler plugin
- ✅ Desktop target (опционально)

---

### 4. core-module.gradle.kts

**Назначение**: Convention для core модулей (:core:*)

```kotlin
plugins {
    id("kmp-base")
    id("kmp-android")
    id("kmp-compose")
}
```

**Применяется к:**
- ✅ :core:network
- ✅ :core:storage
- ✅ :core:theme
- ✅ :core:i18n
- ✅ :core:utils
- ✅ :core:navigation
- ✅ :core:di

---

### 5. feature-module.gradle.kts

**Назначение**: Convention для feature модулей (Clean Architecture)

```kotlin
plugins {
    id("kmp-base")
    id("kmp-android")
    id("kmp-compose")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                // Core dependencies
                implementation(libs.koin.core)
                implementation(libs.voyager.navigator)
                implementation(libs.voyager.screenModel)
            }
        }
    }
}
```

**Применяется к:**
- ✅ :feature:auth
- ✅ :feature:catalog
- ✅ :feature:booking
- ✅ :feature:services
- ✅ :feature:profile
- ✅ :feature:favorites
- ✅ :feature:reviews

**Clean Architecture слои:**
```
feature/{name}/
├── domain/          # Бизнес-логика (100% shared)
├── data/            # Репозитории + API (100% shared)
└── presentation/    # UI + ViewModels (100% shared)
```

---

### 6. app-module.gradle.kts

**Назначение**: Convention для app модулей (:androidApp, :iosApp)

```kotlin
plugins {
    id("kmp-base")
    id("com.android.application")
}

android {
    compileSdk = 36
    namespace = "com.aggregateservice"

    defaultConfig {
        applicationId = "com.aggregateservice.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

**Что настраивает:**
- ✅ Application ID
- ✅ Versioning
- ✅ Build types (debug/release)
- ✅ ProGuard/R8

---

### 7. detekt-configuration.gradle.kts

**Назначение**: Detekt static analysis

```kotlin
plugins {
    id("io.gitlab.arturbosch.detekt")
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom(files("$rootDir/.detekt/config.yml"))
    baseline = null // Zero tolerance policy
    source.setFrom(
        "src/commonMain/kotlin",
        "src/androidMain/kotlin",
        "src/iosMain/kotlin"
    )
}

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.6")
}
```

**Что настраивает:**
- ✅ Custom Detekt config
- ✅ Source sets (commonMain, androidMain, iosMain)
- ✅ Formatting plugin
- ✅ Zero tolerance (no baseline)

**Aggregate task**: `detektAll` (в `build.gradle.kts`)

---

### 8. ktlint-configuration.gradle.kts

**Назначение**: Ktlint formatter

```kotlin
plugins {
    id("org.jlleitschuh.gradle.ktlint")
}

ktlint {
    version.set("13.1.0") // ⬆️ Updated 2026-03-20
    debug.set(false)
    verbose.set(true)
    android.set(true)
    outputToConsole.set(true)
    outputColorName.set("RED")
    ignoreFailures.set(true) // Lenient mode for generated code
}
```

**Что настраивает:**
- ✅ Version 13.1.0 (обновлено)
- ✅ Android mode
- ✅ Console output
- ✅ Lenient mode (игнорирует generated code)

**Aggregate tasks**: `ktlintCheckAll`, `ktlintFormatAll` (в `build.gradle.kts`)

---

## 🔗 Применение Convention Plugins

### В модуле

```kotlin
// core/network/build.gradle.kts
plugins {
    id("core-module") // Применяет все core conventions
}

android {
    namespace = "com.aggregateservice.core.network"
}

kotlin {
    sourceSets {
        maybeCreate("commonMain").dependencies {
            implementation(libs.ktor.client.core)
        }
    }
}
```

### В корневом build.gradle.kts

```kotlin
// Apply quality plugins to all subprojects
subprojects {
    apply(plugin = "detekt-configuration")
    apply(plugin = "ktlint-configuration")

    // Aggregate tasks
    tasks.register("detektAll") {
        dependsOn(subprojects.map { "${it.path}:detekt" })
    }

    tasks.register("ktlintCheckAll") {
        dependsOn(subprojects.map { "${it.path}:ktlintCheck" })
    }
}
```

---

## 📦 Зависимости Build Logic

### build-logic/build.gradle.kts

```kotlin
plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    // Android Gradle Plugin
    implementation("com.android.tools.build:gradle:8.12.3") // ⬆️ Updated

    // Kotlin Gradle Plugin
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.20") // ⬆️ Updated

    // Compose Gradle Plugin
    implementation("org.jetbrains.compose:compose-gradle-plugin:1.10.2") // ⬆️ Updated

    // Detekt Gradle Plugin
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.23.8") // ⬆️ Updated

    // Ktlint Gradle Plugin
    implementation("org.jlleitschuh.gradle:ktlint-gradle:13.1.0") // ⬆️ Updated
}
```

### settings.gradle.kts

```kotlin
dependencyResolutionManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

include(":build-logic")
```

---

## 🚀 Aggregate Tasks

| Задача | Описание | Команда |
|--------|----------|---------|
| **detektAll** | Запуск Detekt для всех модулей | `./gradlew detektAll` |
| **ktlintCheckAll** | Проверка стиля всех модулей | `./gradlew ktlintCheckAll` |
| **ktlintFormatAll** | Форматирование всех модулей | `./gradlew ktlintFormatAll` |
| **koverReportAll** | Coverage report всех модулей | `./gradlew koverReportAll` |
| **koverVerifyAll** | Проверка coverage thresholds | `./gradlew koverVerifyAll` |

---

## 📊 Статус реализации

| Компонент | Статус | Прогресс |
|-----------|--------|----------|
| **kmp-base** | ✅ Complete | 100% |
| **kmp-android** | ✅ Complete | 100% |
| **kmp-compose** | ✅ Complete | 100% |
| **core-module** | ✅ Complete | 100% |
| **feature-module** | ✅ Complete | 100% |
| **app-module** | ✅ Complete | 100% |
| **detekt-configuration** | ✅ Complete | 100% |
| **ktlint-configuration** | ✅ Complete | 100% |

---

## 🔮 TODO: Будущие улучшения

### Phase 1: Enhancements

1. **Custom KtLint Rules**
   - Project-specific style rules
   - Compose-specific formatting

2. **Detekt Baseline для Legacy Code**
   - Временный baseline для миграции
   - Постепенное снижение issues

3. **Kotlin Script для New Feature**
   - Скрипт для генерации feature template
   - Auto-создание domain/data/presentation слоев

### Phase 2: CI/CD Integration

1. **GitHub Actions**
   - Automatic Detekt/Ktlint checks
   - Coverage reporting
   - Build warnings

2. **Pre-commit Hooks**
   - Local KtLint formatting
   - Detekt checking

---

## 🔗 Связанные документы

- [NETWORK_LAYER.md](NETWORK_LAYER.md) - Конфигурация Ktor в build.gradle.kts
- [CODE_QUALITY_GUIDE.md](CODE_QUALITY_GUIDE.md) - Detekt и KtLint детали
- [IMPLEMENTATION_STATUS.md](IMPLEMENTATION_STATUS.md) - Статус build-logic
- [libs.versions.toml](../gradle/libs.versions.toml) - Version catalog

---

**Версия документа**: 1.1
**Last Updated**: 2026-03-20
**Maintainer**: Development Team

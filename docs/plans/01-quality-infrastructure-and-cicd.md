# 📋 План внедрения инфраструктуры качества и CI/CD

**Дата создания**: 2026-03-19
**Базируется на**: Deep Code Review (2026-03-19)
**Статус проекта**: Initial Setup & Infrastructure (15% Complete)
**Срок исполнения**: 2 недели
**Приоритет**: 🔴 КРИТИЧЕСКИЙ

---

## 📊 Current State Analysis

На основе deep code review от 2026-03-19:

| Категория | Score | Status | Проблемы |
|-----------|-------|--------|----------|
| **Build-Logic & Gradle** | 85/100 | ✅ GOOD | 0 критических |
| **Code Quality Tools** | 0/100 | ❌ ABSENT | Detekt, Ktlint, Kover отсутствуют |
| **Testing Infrastructure** | 0/100 | ❌ ABSENT | Нет ни одного теста |
| **CI/CD Pipeline** | 0/100 | ❌ ABSENT | Нет GitHub Actions |
| **Error Handling** | 0/100 | ❌ ABSENT | Нет safeApiCall, AppError |
| **Clean Architecture** | 0/100 | ⚪ EMPTY | Нет Domain Models, Repository |

**КРИТИЧЕСКИЕ ПРОБЛЕМЫ:**
1. ❌ Нет статического анализа кода (Detekt/Ktlint)
2. ❌ Нет тестовой инфраструктуры
3. ❌ Нет CI/CD pipeline
4. ❌ Нет обработки ошибок в сети (safeApiCall)
5. ❌ Нет coverage отчетов

---

## 🎯 Objectives

**Основная цель:** Внедрить production-grade инфраструктуру качества кода и CI/CD до начала активной разработки фич.

**KPI (по завершении 2 недель):**
- ✅ Detekt: 0 warnings
- ✅ Ktlint: 100% стиль соответствие
- ✅ Kover: 60%+ покрытие (минимум для network слоя)
- ✅ CI/CD: Автоматические проверки в каждом PR
- ✅ Тесты: Unit тесты для network слоя (100% функций покрыты)
- ✅ Error Handling: safeApiCall реализован и протестирован

---

## 📅 PHASE 1: Code Quality Infrastructure (Week 1, Days 1-3)

### 1.1 Detekt - Static Analysis for Kotlin

**Цель:** Обнаруживать баги, запахи кода и violations best practices автоматически.

**Файлы для создания/изменения:**

#### `gradle/libs.versions.toml`
```toml
[versions]
detekt = "1.23.6"

[plugins]
detekt = { id = "io.gitlab.arturbosch.detekt", version.ref = "detekt" }

[libraries]
# Detekt plugins for custom rules
detekt-formatting = { module = "io.gitlab.arturbosch.detekt:detekt-formatting", version.ref = "detekt" }
```

#### `.detekt/config.yml`
```yaml
# Конфигурация Detekt для KMP проекта
build:
  maxIssues: 0
  excludeCorrectable: false
  weights:
    complexity: 2
    LongParameterList: 1
    style: 1
    comments: 1

config:
  validation: true
  warningsAsErrors: false
  checkExhaustiveness: false

console-reports:
  active: true

output-reports:
  active: true
  exclude:
  - 'TxtOutputReport'
  - 'MdOutputReport'

processors:
  active: true

comments:
  active: true
  AbsentOrWrongFileLicense:
    active: false
  CommentOverPrivateFunction:
    active: false
  CommentOverPrivateProperty:
    active: false
  EndOfSentenceFormat:
    active: false
  UndocumentedPublicClass:
    active: false
  UndocumentedPublicFunction:
    active: false
  UndocumentedPublicProperty:
    active: false

complexity:
  active: true
  ComplexCondition:
    active: true
    threshold: 4
  LongMethod:
    active: true
    threshold: 60
  LongParameterList:
    active: true
    functionThreshold: 6
    constructorThreshold: 7
  MethodOverloading:
    active: false
  NestedBlockDepth:
    active: true
    threshold: 4
  StringLiteralDuplication:
    active: false

coroutines:
  active: true
  GlobalCoroutineUsage:
    active: true
  RedundantSuspendModifier:
    active: true
  SuspendFunWithFlowReturnType:
    active: true

empty-blocks:
  active: true
  EmptyClassBlock:
    active: true
  EmptyFunctionBlock:
    active: true
    ignoreOverridden: false
  EmptyIfBlock:
    active: true
  EmptyKtFile:
    active: false
  EmptyTryBlock:
    active: true
  EmptyWhenBlock:
    active: true
  EmptyWhileBlock:
    active: true

exceptions:
  active: true
  ExceptionRaisedInUnexpectedLocation:
    active: true
  InstanceOfCheckForException:
    active: true
  NotImplementedDeclaration:
    active: false
  PrintStackTrace:
    active: true
  RethrowCaughtException:
    active: true
  ReturnFromFinally:
    active: true
  SwallowedException:
    active: true
    allowedExceptionNameRegex: '_|(expected|expected.*|foreseen)'
    ignoredExceptionTypes:
      - InterruptedException
      - NumberFormatException
      - MalformedURLException
      - ParseException
  ThrowingExceptionFromFinally:
    active: true
  ThrowingExceptionInMain:
    active: false
  ThrowingExceptionsWithoutMessageOrCause:
    active: true
  ThrowingNewInstanceSameException:
    active: true
  TooGenericExceptionCaught:
    active: true
    exceptionNames:
      - ArrayIndexOutOfBoundsException
      - Error
      - Exception
      - IllegalMonitorStateException
      - NullPointerException
      - IndexOutOfBoundsException
      - RuntimeException
      - Throwable
  TooGenericExceptionThrown:
    active: true

formatting:
  active: true
  android: true
  autoCorrect: true
  annotationOnSeparateLine:
    active: false
  chainWrapping:
    active: true
    autoCorrect: true
  commentSpacing:
    active: true
    autoCorrect: true
  enumWrapping:
    active: false
  filename:
    active: true
  finalNewline:
    active: true
    autoCorrect: true
    insertFinalNewLine: true
  importOrdering:
    active: false
  indentation:
    active: true
    autoCorrect: true
    consistentIndentationRelativeTo: firstParameterInConstructorList
    indentSize: 4
  maxLineLength:
    active: true
    maxLineLength: 120
    excludePackageStatements: true
    excludeImportStatements: true
    excludeCommentStatements: false
  modifierOrdering:
    active: true
    autoCorrect: true
  noBlankLineBeforeRbrace:
    active: true
    autoCorrect: true
  noConsecutiveBlankLines:
    active: true
    autoCorrect: true
  noEmptyClassBody:
    active: true
    autoCorrect: true
  noEmptyFile:
    active: false
  noLineBreakAfterElse:
    active: true
    autoCorrect: true
  noLineBreakBeforeAssignment:
    active: true
    autoCorrect: true
  noMultipleSpaces:
    active: true
    autoCorrect: true
  noSemicolons:
    active: true
    autoCorrect: true
  noTrailingSpaces:
    active: true
    autoCorrect: true
  noUnitReturn:
    active: true
    autoCorrect: true
  noUnusedImports:
    active: true
    autoCorrect: true
  noWildcardImports:
    active: true
    autoCorrect: true
  packageNaming:
    active: true
    autoCorrect: true
  parameterListWrapping:
    active: true
    autoCorrect: true
    indentSize: 4
  spacingAroundColon:
    active: true
    autoCorrect: true
  spacingAroundComma:
    active: true
    autoCorrect: true
  spacingAroundCurly:
    active: true
    autoCorrect: true
  spacingAroundDot:
    active: true
    autoCorrect: true
  spacingAroundDoubleAngleOperator:
    active: false
  spacingAroundKeyword:
    active: true
    autoCorrect: true
  spacingAroundOperators:
    active: true
    autoCorrect: true
  spacingAroundParens:
    active: true
    autoCorrect: true
  spacingAroundRangeOperator:
    active: true
    autoCorrect: true
  spacingBetweenDeclarationsWithAnnotations:
    active: false
  spacingBetweenDeclarationsWithComments:
    active: false
  spacingBetweenFunctions:
    active: false
  stringTemplate:
    active: true
    autoCorrect: true

naming:
  active: true
  ClassNaming:
    active: true
    classPattern: '[A-Z][a-zA-Z0-9]*'
  ConstructorParameterNaming:
    active: true
    parameterPattern: '[a-z][A-Za-z0-9]*'
  EnumNaming:
    active: true
    enumEntryPattern: '[A-Z][_a-zA-Z0-9]*'
  ForbiddenClassName:
    active: false
  FunctionMaxLength:
    active: false
  FunctionMinLength:
    active: false
  FunctionNaming:
    active: true
    functionPattern: '[a-z][a-zA-Z0-9]*'
  FunctionParameterNaming:
    active: true
    parameterPattern: '[a-z][A-Za-z0-9]*'
  InvalidPackageDeclaration:
    active: true
  MatchingDeclarationName:
    active: true
  MemberNameEqualsClassName:
    active: true
  ObjectPropertyNaming:
    active: true
    constantPattern: '[A-Za-z][_A-Za-z0-9]*'
  PackageNaming:
    active: true
    packagePattern: '[a-z]+(\.[a-z][A-Za-z0-9]*)*'
  TopLevelPropertyNaming:
    active: true
    constantPattern: '[A-Z][_A-Z0-9]*'
  VariableMaxLength:
    active: false
  VariableMinLength:
    active: false
  VariableNaming:
    active: true
    variablePattern: '[a-z][A-Za-z0-9]*'

performance:
  active: true
  ArrayPrimitive:
    active: true
  ForEachOnRange:
    active: true
  SpreadOperator:
    active: true
  UnnecessaryTemporaryInstantiation:
    active: true

potential-bugs:
  active: true
  AvoidReferentialEquality:
    active: true
  CastToNullableType:
    active: false
  Deprecation:
    active: false
  DontDowncastCollectionTypes:
    active: false
  DoubleMutabilityForCollection:
    active: true
  EqualsAlwaysReturnsTrueOrFalse:
    active: true
  EqualsWithHashCodeExist:
    active: true
  ExplicitGarbageCollectionCall:
    active: true
  HasPlatformType:
    active: true
  IgnoredReturnValue:
    active: true
  ImplicitDefaultLocale:
    active: true
  ImplicitUnitReturnType:
    active: false
  InvalidRange:
    active: true
  IteratorHasNextCallsNextMethod:
    active: true
  IteratorNotThrowingNoSuchElementException:
    active: true
  LateinitUsage:
    active: true
  MapGetWithNotNullAssertionOperator:
    active: true
  MissingPackage:
    active: false
  MissingWhenCase:
    active: true
  NullableToStringCall:
    active: true
  RedundantElseInWhen:
    active: true
  UnconditionalJumpStatementInLoop:
    active: false
  UnnecessaryNotNullOperator:
    active: true
  UnnecessarySafeCall:
    active: true
  UnreachableCatchBlock:
    active: true
  UnreachableCode:
    active: true
  UnsafeCallOnNullableType:
    active: true
  UnsafeCast:
    active: true
  UselessPostfixExpression:
    active: true
  WrongEqualsTypeParameter:
    active: true

style:
  active: true
  ClassOrdering:
    active: false
  CollapsibleIfStatements:
    active: false
  DataClassContainsFunctions:
    active: false
  DataClassShouldBeImmutable:
    active: false
  EqualsNullCall:
    active: true
  EqualsOnSignatureLine:
    active: false
  ExplicitCollectionElementAccessMethod:
    active: false
  ExplicitItLambdaParameter:
    active: false
  ExpressionBodySyntax:
    active: false
  ForbiddenComment:
    active: true
    values:
      - 'TODO:'
      - 'FIXME:'
      - 'STOPSHIP:'
    allowedPatterns: ''
  ForbiddenImport:
    active: false
  ForbiddenMethodCall:
    active: false
  ForbiddenPublicDataClass:
    active: true
  ForbiddenVoid:
    active: true
  FunctionOnlyReturningConstant:
    active: true
  LibraryCodeMustSpecifyReturnType:
    active: true
  LibraryEntitiesShouldNotBePublic:
    active: false
  LoopWithTooManyJumpStatements:
    active: true
    maxJumpCount: 1
  MagicNumber:
    active: true
    ignoreNumbers:
      - '-1'
      - '0'
      - '1'
      - '2'
    ignoreHashCodeFunction: true
    ignorePropertyDeclaration: false
    ignoreAnnotation: false
    ignoreNamedArgument: true
    ignoreEnums: false
    ignoreRanges: false
    ignoreExtensionFunctions: true
  MandatoryBracesIfStatements:
    active: false
  MandatoryBracesLoops:
    active: false
  MaxLineLength:
    active: true
    maxLineLength: 120
    excludePackageStatements: true
    excludeImportStatements: true
    excludeCommentStatements: false
  MayBeConst:
    active: true
  ModifierOrder:
    active: true
  MultilineLambdaItParameter:
    active: false
  NestedClassesVisibility:
    active: true
  NewLineAtEndOfFile:
    active: true
  NoTabs:
    active: false
  ObjectLiteralToLambda:
    active: true
  OptionalAbstractKeyword:
    active: true
  OptionalUnit:
    active: false
  PreferToOverPairSyntax:
    active: false
  ProtectedMemberInFinalClass:
    active: true
  RedundantExplicitType:
    active: false
  RedundantHigherOrderMapUsage:
    active: true
  RedundantVisibilityModifierRule:
    active: false
  ReturnCount:
    active: true
    max: 2
    excludedFunctions: 'equals'
  SafeCast:
    active: true
  SerialVersionUIDInSerializableClass:
    active: true
  SpacingBetweenPackageAndImports:
    active: false
  ThrowsCount:
    active: true
    max: 2
  TrailingWhitespace:
    active: false
  UnderscoresInNumericLiterals:
    active: false
  UnnecessaryAbstractClass:
    active: true
  UnnecessaryApply:
    active: true
  UnnecessaryInheritance:
    active: true
  UnnecessaryLet:
    active: false
  UnnecessaryParentheses:
    active: false
  UntilInsteadOfRangeTo:
    active: false
  UnusedImports:
    active: false
  UnusedPrivateClass:
    active: true
  UnusedPrivateMember:
    active: true
  UseArrayLiteralsInAnnotations:
    active: true
  UseCheckNotNull:
    active: true
  UseCheckOrError:
    active: true
  UseDataClass:
    active: false
  UseEmptyCounterpart:
    active: false
  UseIfEmptyOrIfBlank:
    active: false
  UseIfInsteadOfWhen:
    active: false
  UseIsNullOrEmpty:
    active: true
  UseOrEmpty:
    active: true
  RequireNullOrEmpty:
    active: false
  UseRequire:
    active: true
  UselessCallOnNotNull:
    active: true
  UtilityClassWithPublicConstructor:
    active: true
  VarCouldBeVal:
    active: true
  WildcardImport:
    active: true
    excludes: ['java.util.*', 'kotlinx.android.synthetic.*']
```

#### `build-logic/src/main/kotlin/detekt-configuration.gradle.kts`
```kotlin
plugins {
    id("io.gitlab.arturbosch.detekt")
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom(files("$rootDir/.detekt/config.yml"))
    baseline = file("$rootDir/.detekt/baseline.xml")
    source.setFrom(
        "src/commonMain/kotlin",
        "src/androidMain/kotlin",
        "src/iosMain/kotlin"
    )
}

dependencies {
    detektPlugins(libs.detekt.formatting)
}

tasks.withType<io.gitlab.arturbosch.detekt.extensions.DetektExtension>().configureEach {
    reports {
        html.required.set(true)
        md.required.set(true)
        xml.required.set(true)
        txt.required.set(true)
        sarif.required.set(true)
    }
}
```

#### `build.gradle.kts` (root project)
```kotlin
plugins {
    alias(libs.plugins.detekt) apply false
}

// Apply Detekt to all subprojects
subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")
}

// Detekt task for all modules
tasks.register("detektAll") {
    dependsOn(subprojects.map { it.tasks.withType<io.gitlab.arturbosch.detekt.tasks.Detekt>() })
}
```

---

### 1.2 Ktlint - Code Formatting

**Цель:** Единый стиль кода в команде, auto-fix форматирования.

**Файлы для создания/изменения:**

#### `gradle/libs.versions.toml`
```toml
[versions]
ktlint = "1.2.1"

[plugins]
ktlint = { id = "org.jlleitschuh.gradle.ktlint", version.ref = "ktlint" }
```

#### `.editorconfig`
```ini
# EditorConfig для Kotlin Multiplatform Project
# Compatible with: IntelliJ IDEA, Android Studio, VS Code

root = true

[*]
charset = utf-8
end_of_line = lf
indent_size = 4
indent_style = space
insert_final_newline = true
max_line_length = 120
tab_width = 4
trim_trailing_whitespace = true

[*.kt]
continuation_indent_size = 4
indent_size = 4
max_line_length = 120
tab_width = 4

[*.{kts,kt}]
indent_size = 4
continuation_indent_size = 4
max_line_length = 120
tab_width = 4

[*.{xml,json,yml,yaml}]
indent_size = 2

[*.md]
max_line_length = off
trim_trailing_whitespace = false

[*.gradle]
indent_size = 4

[.gitmodules]
indent_style = tab

[COMMIT_EDITMSG]
max_line_length = off
```

#### `build-logic/src/main/kotlin/ktlint-configuration.gradle.kts`
```kotlin
plugins {
    id("org.jlleitschuh.gradle.ktlint")
}

ktlint {
    version.set("1.2.1")
    debug.set(false)
    verbose.set(true)
    android.set(true)
    outputToConsole.set(true)
    outputColorName.set("RED")
    ignoreFailures.set(false)
    enableExperimentalRules.set(true)

    filter {
        exclude("**/generated/**")
        include("**/kotlin/**")
    }

    // Additional custom rules
    customRuleSets {
        // Add custom rule sets if needed
    }
}

tasks.withType<org.jlleitschuh.gradle.ktlint.tasks.BaseKtlintCheckTask>().configureEach {
    worker {
        // Ktlint runs in parallel by default
        // This option doesn't affect parallelism
        forkOptions {
            isDaemon = false
        }
    }
}
```

#### `build.gradle.kts` (root project)
```kotlin
plugins {
    alias(libs.plugins.ktlint) apply false
}

// Apply Ktlint to all subprojects
subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
}

// Ktlint check task
tasks.register("ktlintCheckAll") {
    dependsOn(subprojects.map { it.tasks.withType<org.jlleitschuh.gradle.ktlint.tasks.KtlintCheckTask>() })
}

// Ktlint format task
tasks.register("ktlintFormatAll") {
    dependsOn(subprojects.map { it.tasks.withType<org.jlleitschuh.gradle.ktlint.tasks.KtlintFormatTask>() })
}
```

---

### 1.3 Kover - Code Coverage

**Цель:** Измерять покрытие кода тестами, target: 80% для network слоя.

**Файлы для создания/изменения:**

#### `gradle/libs.versions.toml`
```toml
[versions]
kover = "0.8.3"

[plugins]
kover = { id = "org.jetbrains.kotlinx.kover", version.ref = "kover" }
```

#### `build-logic/src/main/kotlin/kover-configuration.gradle.kts`
```kotlin
plugins {
    id("org.jetbrains.kotlinx.kover")
}

kover {
    reports {
        total {
            html {
                onCheck = true
                setReportDir(layout.buildDirectory.dir("reports/kover/html"))
            }
            xml {
                onCheck = true
                setReportDir(layout.buildDirectory.dir("reports/kover/xml"))
            }
        }
    }

    // Minimum coverage rules
    verify {
        rule {
            name = "Minimum coverage rule"

            bound {
                minValue = 60  // Minimum 60% coverage
                metric = kotlinx.kover.api.KoverMetric.LINE
                aggregation = kotlinx.kover.api.KoverAggregation.COVERED_PERCENTAGE
            }

            bound {
                minValue = 60
                metric = kotlinx.kover.api.KoverMetric.INSTRUCTION
                aggregation = kotlinx.kover.api.KoverAggregation.COVERED_PERCENTAGE
            }
        }
    }
}
```

#### `build.gradle.kts` (root project)
```kotlin
plugins {
    alias(libs.plugins.kover) apply false
}

subprojects {
    apply(plugin = "org.jetbrains.kotlinx.kover")
}

// Kover verification task
tasks.register("koverVerifyAll") {
    dependsOn(subprojects.map { it.tasks.withType<org.jetbrains.kotlinx.kover.tasks.KoverVerifyTask>() })
}

// Kover report task
tasks.register("koverReportAll") {
    dependsOn(subprojects.map { it.tasks.withType<org.jetbrains.kotlinx.kover.tasks.KoverReportTask>() })
}
```

---

## 📅 PHASE 2: Testing Infrastructure (Week 1, Days 3-5)

### 2.1 Test Dependencies Setup

**Цель:** Добавить библиотеки для unit тестирования KMP кода.

**Файлы для изменения:**

#### `gradle/libs.versions.toml`
```toml
[versions]
mockk = "1.13.9"
turbine = "1.1.0"
kotlinx-coroutines-test = "1.9.0"

[libraries]
# Testing
mockk = { module = "io.mockk:mockk", version.ref = "mockk" }
turbine = { module = "app.cash.turbine:turbine", version.ref = "turbine" }
kotlinx-coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "kotlinx-coroutines-test" }
```

#### `build-logic/src/main/kotlin/test-configuration.gradle.kts`
```kotlin
plugins {
    kotlin("multiplatform")
}

configure<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension> {
    sourceSets {
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.mockk)
                implementation(libs.turbine)
                implementation(libs.kotlinx.coroutines.test)
            }
        }

        val androidTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.mockk)
            }
        }
    }
}
```

---

### 2.2 Test Structure Setup

**Цель:** Создать структуру для тестирования network слоя.

**Структура директорий:**

```
core/network/src/
├── commonMain/kotlin/com/aggregateservice/core/network/
│   ├── AppError.kt
│   ├── safeApiCall.kt
│   └── PlatformEngine.kt
├── commonTest/kotlin/com/aggregateservice/core/network/
│   ├── SafeApiCallTest.kt
│   └── AppErrorTest.kt
├── androidMain/kotlin/.../PlatformEngine.kt (actual)
├── androidTest/kotlin/.../PlatformEngineTest.kt
├── iosMain/kotlin/.../PlatformEngine.kt (actual)
└── iosTest/kotlin/.../PlatformEngineTest.kt
```

---

## 📅 PHASE 3: Error Handling Foundation (Week 1, Days 4-5)

### 3.1 Create AppError Sealed Hierarchy

**Файл:** `core/network/src/commonMain/kotlin/com/aggregateservice/core/network/AppError.kt`

```kotlin
package com.aggregateservice.core.network

/**
 * Sealed hierarchy representing all possible application errors.
 * This ensures type-safe error handling across the application.
 */
sealed interface AppError {
    val message: String
    val cause: Throwable?

    /**
     * Network-related errors (connection issues, timeouts)
     */
    data class NetworkError(
        override val message: String,
        override val cause: Throwable? = null
    ) : AppError

    /**
     * Server errors (5xx status codes)
     */
    data class ServerError(
        override val message: String,
        val statusCode: Int,
        override val cause: Throwable? = null
    ) : AppError

    /**
     * Authentication/authorization errors (401, 403)
     */
    data class UnauthorizedError(
        override val message: String = "Unauthorized",
        override val cause: Throwable? = null
    ) : AppError

    /**
     * Validation errors (400 status codes)
     */
    data class ValidationError(
        override val message: String,
        val errors: Map<String, List<String>> = emptyMap(),
        override val cause: Throwable? = null
    ) : AppError

    /**
     * Not found errors (404)
     */
    data class NotFoundError(
        override val message: String,
        override val cause: Throwable? = null
    ) : AppError

    /**
     * Unknown/unexpected errors
     */
    data class UnknownError(
        override val message: String,
        override val cause: Throwable? = null
    ) : AppError
}

/**
 * Helper function to convert AppError to user-friendly message
 */
fun AppError.getUserMessage(): String = when (this) {
    is AppError.NetworkError -> "Проверьте подключение к интернету"
    is AppError.ServerError -> "Ошибка сервера. Попробуйте позже"
    is AppError.UnauthorizedError -> "Необходима авторизация"
    is AppError.ValidationError -> message
    is AppError.NotFoundError -> "Ресурс не найден"
    is AppError.UnknownError -> "Произошла ошибка. Попробуйте позже"
}
```

---

### 3.2 Create safeApiCall Wrapper

**Файл:** `core/network/src/commonMain/kotlin/com/aggregateservice/core/network/safeApiCall.kt`

```kotlin
package com.aggregateservice.core.network

import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.network.exceptions.ConnectException
import io.ktor.client.network.exceptions.SocketTimeoutException
import io.ktor.http.HttpStatusCode
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.io.IOException

/**
 * Safe API call wrapper that converts Ktor exceptions to typed AppError.
 * This ensures that network errors don't leak to upper layers.
 *
 * @param T The type of successful response body
 * @param execute The suspending lambda that performs the HTTP request
 * @return Result<T, AppError> containing either success data or error
 */
suspend inline fun <reified T> safeApiCall(
    execute: () -> HttpResponse
): Result<T, AppError> = try {
    val response = execute()
    val body: T = response.body()
    Result.success(body)
} catch (e: ClientRequestException) {
    val statusCode = e.response.status
    val responseBody = e.response.body<String>()

    when (statusCode.value) {
        400 -> Result.error(
            AppError.ValidationError(
                message = "Validation failed: $responseBody",
                errors = parseValidationErrors(responseBody),
                cause = e
            )
        )
        401 -> Result.error(AppError.UnauthorizedError("Unauthorized", e))
        403 -> Result.error(AppError.UnauthorizedError("Forbidden", e))
        404 -> Result.error(AppError.NotFoundError("Resource not found", e))
        in 500..599 -> Result.error(
            AppError.ServerError(
                message = "Server error: ${statusCode.value}",
                statusCode = statusCode.value,
                cause = e
            )
        )
        else -> Result.error(
            AppError.UnknownError(
                message = "HTTP ${statusCode.value}: $responseBody",
                cause = e
            )
        )
    }
} catch (e: UnresolvedAddressException) {
    Result.error(AppError.NetworkError("Network unreachable", e))
} catch (e: ConnectException) {
    Result.error(AppError.NetworkError("Connection refused", e))
} catch (e: SocketTimeoutException) {
    Result.error(AppError.NetworkError("Connection timeout", e))
} catch (e: IOException) {
    Result.error(AppError.NetworkError("Network error", e))
} catch (e: Exception) {
    Result.error(AppError.UnknownError("Unknown error: ${e.message}", e))
}

/**
 * Parse validation errors from response body
 * Expected format: {"field1": ["error1", "error2"], "field2": ["error3"]}
 */
private fun parseValidationErrors(responseBody: String): Map<String, List<String>> {
    return try {
        // Implement JSON parsing based on your API response format
        // This is a placeholder - you'd use kotlinx.serialization here
        emptyMap()
    } catch (e: Exception) {
        emptyMap()
    }
}

/**
 * Extension function to convert Ktor HttpResponse body to String
 */
private suspend inline fun <reified T> HttpResponse.body(): T {
    return this.call.body()
}

/**
 * Custom Result class (or use kotlinx.coroutines.Result)
 */
sealed class Result<out T, out E> {
    data class Success<T>(val data: T) : Result<T, Nothing>()
    data class Error<E>(val error: E) : Result<Nothing, E>()

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error

    fun <R> map(transform: (T) -> R): Result<R, E> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
    }

    companion object {
        fun <T> success(data: T): Result<T, Nothing> = Success(data)
        fun <E> error(error: E): Result<Nothing, E> = Error(error)
    }
}
```

---

### 3.3 Create Unit Tests for Error Handling

**Файл:** `core/network/src/commonTest/kotlin/com/aggregateservice/core/network/SafeApiCallTest.kt`

```kotlin
package com.aggregateservice.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SafeApiCallTest {

    @Test
    fun `safeApiCall returns success on 200 OK`() = runTest {
        val mockEngine = MockEngine { _ ->
            respond(
                content = ByteReadChannel("{\"message\":\"Success\"}"),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = HttpClient(mockEngine)

        val result = safeApiCall<Map<String, String>> {
            client.get("https://api.example.com/success")
        }

        assertTrue(result.isSuccess)
        assertEquals("Success", result.getOrNull()?.get("message"))
    }

    @Test
    fun `safeApiCall returns NetworkError on connection failure`() = runTest {
        val mockEngine = MockEngine { _ ->
            throw io.ktor.client.network.exceptions.ConnectException("Connection refused")
        }

        val client = HttpClient(mockEngine)

        val result = safeApiCall<Map<String, String>> {
            client.get("https://api.example.com/fail")
        }

        assertTrue(result.isError)
        assertTrue(result.getError() is AppError.NetworkError)
    }

    @Test
    fun `safeApiCall returns ServerError on 500`() = runTest {
        val mockEngine = MockEngine { _ ->
            respond(
                content = ByteReadChannel("{\"error\":\"Internal Server Error\"}"),
                status = HttpStatusCode.InternalServerError,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = HttpClient(mockEngine)

        val result = safeApiCall<Map<String, String>> {
            client.get("https://api.example.com/error")
        }

        assertTrue(result.isError)
        val error = result.getError()
        assertTrue(error is AppError.ServerError)
        assertEquals(500, (error as AppError.ServerError).statusCode)
    }

    @Test
    fun `safeApiCall returns UnauthorizedError on 401`() = runTest {
        val mockEngine = MockEngine { _ ->
            respond(
                content = ByteReadChannel("{\"error\":\"Unauthorized\"}"),
                status = HttpStatusCode.Unauthorized,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = HttpClient(mockEngine)

        val result = safeApiCall<Map<String, String>> {
            client.get("https://api.example.com/unauthorized")
        }

        assertTrue(result.isError)
        assertTrue(result.getError() is AppError.UnauthorizedError)
    }
}
```

---

## 📅 PHASE 4: CI/CD Pipeline (Week 2, Days 1-3)

### 4.1 GitHub Actions Workflow

**Файл:** `.github/workflows/ci.yml`

```yaml
name: CI - Quality Checks & Tests

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

jobs:
  # Job 1: Code quality checks (Detekt + Ktlint)
  lint:
    name: Code Quality (Detekt + Ktlint)
    runs-on: ubuntu-latest

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Setup Gradle
        uses: gradle/gradle-build-action@v3

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew

      - name: Run Detekt
        run: ./gradlew detektAll --continue

      - name: Upload Detekt reports
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: detekt-reports
          path: '**/build/reports/detekt/'

      - name: Run Ktlint check
        run: ./gradlew ktlintCheckAll --continue

      - name: Upload Ktlint reports
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: ktlint-reports
          path: '**/build/reports/ktlint/'

  # Job 2: Unit tests (Android only, iOS requires macOS)
  test:
    name: Unit Tests (Android)
    runs-on: ubuntu-latest

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Setup Gradle
        uses: gradle/gradle-build-action@v3

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew

      - name: Run unit tests
        run: ./gradlew testDebugUnitTest --continue

      - name: Upload test reports
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: test-reports
          path: '**/build/reports/tests/'

  # Job 3: Code coverage (Kover)
  coverage:
    name: Code Coverage (Kover)
    runs-on: ubuntu-latest
    needs: test

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Setup Gradle
        uses: gradle/gradle-build-action@v3

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew

      - name: Generate Kover report
        run: ./gradlew koverReportAll

      - name: Verify Kover coverage
        run: ./gradlew koverVerifyAll

      - name: Upload coverage to Codecov
        uses: codecov/codecov-action@v4
        with:
          files: '**/build/reports/kover/xml/**/*.xml'
          fail_ci_if_error: false

      - name: Upload Kover reports
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: kover-reports
          path: '**/build/reports/kover/'

  # Job 4: Build Android APK
  build:
    name: Build Android APK
    runs-on: ubuntu-latest
    needs: [lint, test]

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Setup Gradle
        uses: gradle/gradle-build-action@v3

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew

      - name: Build Android Debug APK
        run: ./gradlew :androidApp:assembleDebug

      - name: Upload APK
        uses: actions/upload-artifact@v4
        with:
          name: android-debug-apk
          path: '**/build/outputs/apk/debug/*.apk'
```

---

### 4.2 Pre-commit Hooks (Lefthook)

**Файл:** `lefthook.yml`

```yaml
# Lefthook configuration for pre-commit hooks
# Install: brew install lefthook OR go install github.com/evilmartians/lefthook@latest

pre-commit:
  parallel: true

  commands:
    # Detekt check (fast)
    detekt:
      run: ./gradlew detektAll --daemon
      fix: false

    # Ktlint format with auto-fix
    ktlint:
      run: ./gradlew ktlintFormatAll --daemon
      fix: true

    # Unit tests (fast, no integration tests)
    test:
      run: ./gradlew testDebugUnitTest --daemon --continue
      fix: false

pre-push:
  parallel: false

  commands:
    # Full test suite before push
    test-full:
      run: ./gradlew test --continue
      fix: false

    # Build verification
    build:
      run: ./gradlew assembleDebug
      fix: false
```

**Альтернатива: Bash script для pre-commit hooks**

**Файл:** `.git/hooks/pre-commit`

```bash
#!/bin/bash

set -e

echo "🔍 Running pre-commit checks..."

# Detekt check
echo "📋 Running Detekt..."
./gradlew detektAll --daemon --quiet
if [ $? -ne 0 ]; then
    echo "❌ Detekt failed. Please fix the issues."
    exit 1
fi

# Ktlint format (auto-fix)
echo "✨ Running Ktlint format..."
./gradlew ktlintFormatAll --daemon --quiet

# Unit tests
echo "🧪 Running unit tests..."
./gradlew testDebugUnitTest --daemon --continue
if [ $? -ne 0 ]; then
    echo "❌ Unit tests failed. Please fix the failing tests."
    exit 1
fi

echo "✅ Pre-commit checks passed!"
```

---

## 📅 PHASE 5: Domain Models & Repository (Week 2, Days 3-5)

### 5.1 Create Auth Domain Models

**Файл:** `feature/auth/src/commonMain/kotlin/com/aggregateservice/auth/domain/model/User.kt`

```kotlin
package com.aggregateservice.auth.domain.model

import kotlinx.serialization.Serializable

/**
 * Domain entity representing a user in the system.
 * This is CLEAN ARCHITECTURE - pure domain model without Ktor dependencies.
 */
@Serializable
data class User(
    val id: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String? = null,
    val avatarUrl: String? = null,
    val isActive: Boolean = true,
    val createdAt: String, // ISO 8601 date string
    val updatedAt: String
) {
    /**
     * Full name of the user
     */
    val fullName: String
        get() = "$firstName $lastName"

    /**
     * Display name for UI (falls back to email if name is empty)
     */
    val displayName: String
        get() = fullName.ifBlank { email }

    companion object {
        /**
         * Empty user for initial state
         */
        val EMPTY = User(
            id = "",
            email = "",
            firstName = "",
            lastName = "",
            createdAt = "",
            updatedAt = ""
        )
    }
}
```

**Файл:** `feature/auth/src/commonMain/kotlin/com/aggregateservice/auth/domain/model/AuthTokens.kt`

```kotlin
package com.aggregateservice.auth.domain.model

import kotlinx.serialization.Serializable

/**
 * Value object representing authentication tokens.
 * Immutable by design to prevent token manipulation.
 */
@Serializable
data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long = 3600 // 1 hour in seconds
) {
    /**
     * Check if access token is expired
     */
    fun isExpired(issuedAt: Long): Boolean {
        val currentTime = System.currentTimeMillis() / 1000
        return (currentTime - issuedAt) >= expiresIn
    }

    /**
     * Get authorization header value
     */
    fun getAuthorizationHeader(): String = "$tokenType $accessToken"

    companion object {
        /**
         * Empty tokens for initial state
         */
        val EMPTY = AuthTokens(
            accessToken = "",
            refreshToken = ""
        )
    }
}
```

**Файл:** `feature/auth/src/commonMain/kotlin/com/aggregateservice/auth/domain/model/Session.kt`

```kotlin
package com.aggregateservice.auth.domain.model

import kotlinx.serialization.Serializable

/**
 * Domain entity representing a user session.
 * Contains both user data and authentication tokens.
 */
@Serializable
data class Session(
    val user: User,
    val tokens: AuthTokens,
    val issuedAt: Long = System.currentTimeMillis() / 1000
) {
    /**
     * Check if session is expired
     */
    fun isExpired(): Boolean = tokens.isExpired(issuedAt)

    /**
     * Check if session is valid
     */
    fun isValid(): Boolean = !isExpired() && user.id.isNotEmpty()

    companion object {
        /**
         * Empty session for initial state
         */
        val EMPTY = Session(
            user = User.EMPTY,
            tokens = AuthTokens.EMPTY,
            issuedAt = 0
        )
    }
}
```

---

### 5.2 Create Auth Repository Interface

**Файл:** `feature/auth/src/commonMain/kotlin/com/aggregateservice/auth/domain/repository/AuthRepository.kt`

```kotlin
package com.aggregateservice.auth.domain.repository

import com.aggregateservice.auth.domain.model.Session
import com.aggregateservice.auth.domain.model.User
import com.aggregateservice.core.network.AppError
import com.aggregateservice.core.network.Result

/**
 * Repository interface for authentication operations.
 * This is CLEAN ARCHITECTURE - Domain layer defines contracts.
 *
 * Implementation resides in Data layer (infrastructure).
 */
interface AuthRepository {

    /**
     * Login user with email and password
     *
     * @param email User email
     * @param password User password
     * @return Result<Session, AppError> - Session on success, AppError on failure
     */
    suspend fun login(
        email: String,
        password: String
    ): Result<Session, AppError>

    /**
     * Register new user
     *
     * @param email User email
     * @param password User password
     * @param firstName User first name
     * @param lastName User last name
     * @return Result<Session, AppError> - Session on success, AppError on failure
     */
    suspend fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String
    ): Result<Session, AppError>

    /**
     * Logout current user
     *
     * @return Result<Unit, AppError>
     */
    suspend fun logout(): Result<Unit, AppError>

    /**
     * Refresh access token using refresh token
     *
     * @return Result<Session, AppError> - New session on success, AppError on failure
     */
    suspend fun refreshToken(): Result<Session, AppError>

    /**
     * Get current session from local storage
     *
     * @return Session? Current session or null if not logged in
     */
    suspend fun getCurrentSession(): Session?

    /**
     * Check if user is authenticated
     *
     * @return Boolean true if user has valid session
     */
    suspend fun isAuthenticated(): Boolean
}
```

---

### 5.3 Create Auth Use Cases

**Файл:** `feature/auth/src/commonMain/kotlin/com/aggregateservice/auth/domain/usecase/LoginUseCase.kt`

```kotlin
package com.aggregateservice.auth.domain.usecase

import com.aggregateservice.auth.domain.model.Session
import com.aggregateservice.auth.domain.repository.AuthRepository
import com.aggregateservice.core.network.AppError
import com.aggregateservice.core.network.Result

/**
 * Use case for user login.
 * This orchestrates business logic for login operation.
 *
 * CLEAN ARCHITECTURE: UseCase is in Domain layer and
 * coordinates between Repository (Data) and business rules.
 */
class LoginUseCase(
    private val authRepository: AuthRepository
) {
    /**
     * Execute login use case
     *
     * @param email User email
     * @param password User password
     * @return Result<Session, AppError>
     */
    suspend operator fun invoke(
        email: String,
        password: String
    ): Result<Session, AppError> {
        // Validate input
        if (email.isBlank()) {
            return Result.error(
                AppError.ValidationError(
                    message = "Email cannot be empty",
                    errors = mapOf("email" to listOf("Email is required"))
                )
            )
        }

        if (password.isBlank()) {
            return Result.error(
                AppError.ValidationError(
                    message = "Password cannot be empty",
                    errors = mapOf("password" to listOf("Password is required"))
                )
            )
        }

        // Basic email validation
        if (!isValidEmail(email)) {
            return Result.error(
                AppError.ValidationError(
                    message = "Invalid email format",
                    errors = mapOf("email" to listOf("Invalid email format"))
                )
            )
        }

        // Delegate to repository
        return authRepository.login(email, password)
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = Regex("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}")
        return emailRegex.matches(email)
    }
}
```

**Файл:** `feature/auth/src/commonMain/kotlin/com/aggregateservice/auth/domain/usecase/RegisterUseCase.kt`

```kotlin
package com.aggregateservice.auth.domain.usecase

import com.aggregateservice.auth.domain.model.Session
import com.aggregateservice.auth.domain.repository.AuthRepository
import com.aggregateservice.core.network.AppError
import com.aggregateservice.core.network.Result

/**
 * Use case for user registration.
 */
class RegisterUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        firstName: String,
        lastName: String
    ): Result<Session, AppError> {
        // Validation
        if (email.isBlank() || password.isBlank() || firstName.isBlank() || lastName.isBlank()) {
            return Result.error(
                AppError.ValidationError(
                    message = "All fields are required",
                    errors = mapOf(
                        "email" to if (email.isBlank()) listOf("Email is required") else emptyList(),
                        "password" to if (password.isBlank()) listOf("Password is required") else emptyList(),
                        "firstName" to if (firstName.isBlank()) listOf("First name is required") else emptyList(),
                        "lastName" to if (lastName.isBlank()) listOf("Last name is required") else emptyList()
                    )
                )
            )
        }

        // Password strength validation
        if (password.length < 8) {
            return Result.error(
                AppError.ValidationError(
                    message = "Password too weak",
                    errors = mapOf("password" to listOf("Password must be at least 8 characters"))
                )
            )
        }

        return authRepository.register(email, password, firstName, lastName)
    }
}
```

---

## 📅 PHASE 6: Dependency Injection Setup (Week 2, Days 4-5)

### 6.1 Create Network Module (Koin)

**Файл:** `core/di/src/commonMain/kotlin/com/aggregateservice/di/NetworkModule.kt`

```kotlin
package com.aggregateservice.di

import com.aggregateservice.core.network.createHttpClient
import com.aggregateservice.core.network.httpClientEngine
import io.ktor.client.HttpClient
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * Koin module for network layer dependencies.
 * Provides singleton HttpClient instance.
 */
val NetworkModule: Module = module {
    single<HttpClient> {
        createHttpClient(
            engine = httpClientEngine,
            baseUrl = "https://api.example.com", // TODO: Move to config
            enableLogging = true
        )
    }
}
```

---

### 6.2 Create Auth Module (Koin)

**Файл:** `feature/auth/src/commonMain/kotlin/com/aggregateservice/auth/di/AuthModule.kt`

```kotlin
package com.aggregateservice.auth.di

import com.aggregateservice.auth.domain.repository.AuthRepository
import com.aggregateservice.auth.domain.usecase.LoginUseCase
import com.aggregateservice.auth.domain.usecase.RegisterUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Koin module for Auth feature dependencies.
 * Provides Repository and UseCase instances.
 */
val AuthModule: Module = module {
    // TODO: Add AuthRepositoryImpl when Data layer is implemented
    // single<AuthRepository> { AuthRepositoryImpl(get()) }

    // UseCases
    factory { LoginUseCase(get()) }
    factory { RegisterUseCase(get()) }
}
```

---

### 6.3 Setup Koin in Android App

**Файл:** `androidApp/src/androidMain/kotlin/com/aggregateservice/androidapp/di/AppKoin.kt`

```kotlin
package com.aggregateservice.androidapp.di

import com.aggregateservice.di.AuthModule
import com.aggregateservice.di.NetworkModule
import org.koin.core.context.startKoin
import org.koin.dsl.module

/**
 * Koin initialization for Android app.
 */
fun initKoin() {
    startKoin {
        modules(
            NetworkModule,
            AuthModule,
            // Add more modules here
        )
    }
}
```

**Файл:** `androidApp/src/androidMain/kotlin/com/aggregateservice/androidapp/MainActivity.kt`

```kotlin
package com.aggregateservice.androidapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.aggregateservice.androidapp.di.initKoin

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Koin
        initKoin()

        setContent {
            // TODO: Setup Compose UI
        }
    }
}
```

---

## 📊 Summary Table: Files to Create/Modify

### Priority 🔴 CRITICAL (Week 1)

| File | Action | Size | Owner |
|------|--------|------|-------|
| `gradle/libs.versions.toml` | Modify | +20 lines | Team |
| `.detekt/config.yml` | Create | ~500 lines | Team |
| `.editorconfig` | Create | ~50 lines | Team |
| `build-logic/.../detekt-configuration.gradle.kts` | Create | ~30 lines | Team |
| `build-logic/.../ktlint-configuration.gradle.kts` | Create | ~30 lines | Team |
| `build-logic/.../kover-configuration.gradle.kts` | Create | ~30 lines | Team |
| `build.gradle.kts` | Modify | +20 lines | Team |
| `.github/workflows/ci.yml` | Create | ~150 lines | DevOps |
| `core/network/.../AppError.kt` | Create | ~100 lines | Backend |
| `core/network/.../safeApiCall.kt` | Create | ~150 lines | Backend |
| `lefthook.yml` | Create | ~30 lines | Team |

### Priority 🟡 HIGH (Week 2)

| File | Action | Size | Owner |
|------|--------|------|-------|
| `build-logic/.../test-configuration.gradle.kts` | Create | ~20 lines | Team |
| `feature/auth/.../model/User.kt` | Create | ~50 lines | Backend |
| `feature/auth/.../model/AuthTokens.kt` | Create | ~50 lines | Backend |
| `feature/auth/.../model/Session.kt` | Create | ~30 lines | Backend |
| `feature/auth/.../AuthRepository.kt` | Create | ~50 lines | Backend |
| `feature/auth/.../LoginUseCase.kt` | Create | ~80 lines | Backend |
| `feature/auth/.../RegisterUseCase.kt` | Create | ~60 lines | Backend |
| `core/di/.../NetworkModule.kt` | Create | ~20 lines | Backend |
| `feature/auth/.../AuthModule.kt` | Create | ~20 lines | Backend |
| `androidApp/.../AppKoin.kt` | Create | ~15 lines | Mobile |

---

## 🎯 Expected Outcomes After 2 Weeks

### Code Quality Metrics

✅ **Detekt**: 0 warnings (baseline established)
✅ **Ktlint**: 100% code style compliance
✅ **Kover**: 60%+ coverage for network layer
✅ **CI/CD**: Automated checks on every PR
✅ **Tests**: 100% functions in network layer covered

### Developer Experience

✅ **Pre-commit hooks**: Auto-format before commit
✅ **Fast feedback**: Detekt/Ktlint run locally in <30s
✅ **Clear standards**: .editorconfig enforces style
✅ **Safe refactoring**: Tests prevent regressions

### Architecture Foundation

✅ **Error handling**: safeApiCall wraps all Ktor calls
✅ **Domain models**: User, AuthTokens, Session defined
✅ **Repository pattern**: Interface in Domain, impl in Data
✅ **Use cases**: Business logic isolated from UI

### CI/CD Pipeline

✅ **Lint job**: Detekt + Ktlint checks
✅ **Test job**: Unit tests run on every PR
✅ **Coverage job**: Kover generates reports
✅ **Build job**: Android APK assembled

---

## ⚠️ Risks & Mitigation

### Risk 1: Detekt Too Strict
**Probability**: HIGH
**Impact**: MEDIUM
**Mitigation**: Start with relaxed config, tighten gradually

### Risk 2: iOS Tests Can't Run on Windows
**Probability**: CERTAIN
**Impact**: MEDIUM
**Mitigation**: CI uses macOS runner for iOS tests, local dev skips iOS tests on Windows

### Risk 3: Kover Slows Down Build
**Probability**: MEDIUM
**Impact**: MEDIUM
**Mitigation**: Enable Kover only in CI, not local builds

### Risk 4: Team Resistance to Pre-commit Hooks
**Probability**: MEDIUM
**Impact**: LOW
**Mitigation**: Make hooks fast (<30s), provide opt-out for emergency fixes

---

## 📅 Timeline

| Week | Days | Tasks | Deliverables |
|------|------|-------|--------------|
| **1** | 1-2 | Detekt, Ktlint, Kover setup | Quality tools configured |
| **1** | 3-4 | Error handling, tests | safeApiCall + unit tests |
| **1** | 5 | CI/CD pipeline | GitHub Actions workflow |
| **2** | 1-2 | Domain models | User, AuthTokens, Session |
| **2** | 3-4 | Repository + UseCases | Auth feature foundation |
| **2** | 5 | Koin DI setup | Dependency injection |

---

## 🔗 Resources

- [Detekt Documentation](https://detekt.dev/)
- [Ktlint Documentation](https://pinterest.github.io/ktlint/)
- [Kover Documentation](https://kotlinlang.org/docs/kover-gradle-plugin.html)
- [GitHub Actions for Android](https://github.com/android/github-actions-samples)
- [Koin Documentation](https://insert-koin.io/docs/setup/koin/)
- [Kotlin Testing Guide](https://kotlinlang.org/docs/kotlin-test.html)

---

**Last Updated**: 2026-03-19
**Next Review**: End of Week 2 (2026-04-02)
**Owner**: Development Team
**Status**: 📝 PLANNED - Ready for execution

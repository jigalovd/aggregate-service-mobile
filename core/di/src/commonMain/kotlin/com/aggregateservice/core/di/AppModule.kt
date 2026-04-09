package com.aggregateservice.core.di

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

/**
 * Главный DI модуль приложения.
 *
 * **IMPORTANT:** Этот модуль содержит ТОЛЬКО core зависимости.
 * Feature модули (auth, catalog, etc.) должны быть подключены отдельно
 * через initializeKoin(appDeclaration) с modules{}.
 *
 * **Why?** Чтобы избежать циклических зависимостей:
 * - core:di НЕ должен зависеть от feature:*
 * - feature:* МОЖЕТ зависеть от core:di
 */
val appModule =
    module {
        // Только core модули
        includes(
            coreModule,
            // ❌ НЕ добавлять feature модули сюда! Вызывает циклическую зависимость
            // authModule,
            // catalogModule,
            // etc.
        )
    }

/**
 * Инициализация Koin DI.
 *
 * **IMPORTANT:** Feature модули должны быть добавлены через appDeclaration.
 *
 * **Usage:**
 * ```kotlin
 * // В Application.onCreate() (Android)
 * class MyApplication : Application() {
 *     override fun onCreate() {
 *         super.onCreate()
 *
 *         // Initialize Config
 *         val config = AppConfig(...)
 *         Config.initialize(config)
 *
 *         // Initialize Koin с feature модулями
 *         initializeKoin {
 *             // ✅ Добавляем feature модули ЗДЕСЬ
 *             modules(authModule)
 *         }
 *     }
 * }
 * ```
 */
fun initializeKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(appModule)
    }

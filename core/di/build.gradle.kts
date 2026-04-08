plugins {
    id("core-module")
}

android {
    namespace = "com.aggregateservice.core.di"
}

kotlin {
    sourceSets {
        maybeCreate("commonMain").dependencies {
            implementation(libs.koin.core)
            implementation(project(":core:config"))
            implementation(project(":core:storage"))
            implementation(project(":core:network"))
            // ❌ НЕ добавлять зависимости на feature модули!
            // Это вызывает циклическую зависимость:
            // core:di → feature:auth → Koin → core:di
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.auth)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.serialization.kotlinx.json)
        }

        maybeCreate("androidMain").dependencies {
            implementation(libs.koin.android)
            implementation(libs.ktor.client.okhttp)
        }
    }
}

plugins {
    id("core-module")
    id("kmp-compose")
}

android {
    namespace = "com.aggregateservice.core.navigation"
}

kotlin {
    sourceSets {
        maybeCreate("commonMain").dependencies {
            implementation(libs.voyager.navigator)
            implementation(libs.voyager.transitions)
            implementation(libs.voyager.screenModel)
            implementation(project(":core:auth-api"))
            // ❌ НЕ добавляем зависимость на feature:auth
            // Это вызовет циклическую зависимость:
            // core:di → core:navigation → feature:auth → core:di
        }
    }
}

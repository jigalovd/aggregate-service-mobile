plugins {
    id("feature-module")
}

android {
    namespace = "com.aggregateservice.feature.auth"
}

kotlin {
    sourceSets {
        maybeCreate("commonMain").dependencies {
            // Core modules
            implementation(project(":core:navigation"))
            implementation(project(":core:i18n"))

            // Network (для Repository)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)

            // DI
            implementation(libs.koin.core)
            implementation(libs.koin.compose)

            // Navigation
            implementation(libs.voyager.navigator)
            implementation(libs.voyager.screenModel)
            implementation(libs.voyager.koin)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}

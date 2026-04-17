plugins {
    id("feature-module")
}

android {
    namespace = "com.aggregateservice.feature.favorites"
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-opt-in=kotlin.time.ExperimentalTime")
    }

    sourceSets {
        maybeCreate("commonMain").dependencies {
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)

            // Logging
            implementation(libs.kermit)

            // Koin Compose for koinInject() in @Composable functions
            implementation(libs.koin.compose)

            // i18n for localized strings
            implementation(project(":core:i18n"))

            // CatalogNavigator interface (implementation in feature:catalog)
            implementation(project(":core:navigation"))

            // Auth contracts (AuthStateProvider, AuthNavigator)
            implementation(project(":core:auth-api"))

            // Favorites API (FavoritesToggle interface)
            implementation(project(":core:favorites-api"))

            // API models (generated from OpenAPI spec)
            implementation(project(":core:api-models"))
        }

        maybeCreate("androidMain").dependencies {
            implementation(libs.ktor.client.okhttp)
        }

        maybeCreate("iosMain").dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

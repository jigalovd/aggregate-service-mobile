plugins {
    id("feature-module")
}

android {
    namespace = "com.aggregateservice.feature.provider.bookings"
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

            // Logging
            implementation(libs.kermit)

            // Koin Compose for koinInject() in @Composable functions
            implementation(libs.koin.compose)

            // i18n for localized strings
            implementation(project(":core:i18n"))

            // Theme
            implementation(project(":core:theme"))

            // Generated API models (DTOs)
            implementation(project(":core:api-models"))

            // Auth contracts (AuthStateProvider, AuthNavigator, LogoutUseCase)
            implementation(project(":core:auth-api"))

            // Navigation (RoleGuard)
            implementation(project(":core:navigation"))

            // Core network (AppError, safeApiCall)
            implementation(project(":core:network"))
        }

        maybeCreate("androidMain").dependencies {
            implementation(libs.ktor.client.okhttp)
        }

        maybeCreate("iosMain").dependencies {
            implementation(libs.ktor.client.darwin)
        }

        maybeCreate("commonTest").dependencies {
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
            implementation(libs.mockk)
            implementation(libs.ktor.client.mock)
        }
    }
}
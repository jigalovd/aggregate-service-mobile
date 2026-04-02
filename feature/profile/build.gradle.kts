plugins {
    id("feature-module")
}

android {
    namespace = "com.aggregateservice.feature.profile"
}

kotlin {
    sourceSets {
        maybeCreate("commonMain").dependencies {
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)

            // Koin Compose for koinInject() in @Composable functions
            implementation(libs.koin.compose)

            // i18n for localized strings
            implementation(project(":core:i18n"))

            // Auth state access via core:navigation abstraction
            // AuthStateProvider is implemented by feature:auth and injected via Koin
            implementation(project(":core:navigation"))

            // Firebase Auth API types (FirebaseToken) for auth callback
            implementation(project(":core:firebase-auth"))

            // AuthRepository and AuthPromptDialog for auth flow
            implementation(project(":feature:auth"))
        }

        maybeCreate("androidMain").dependencies {
            implementation(libs.ktor.client.okhttp)
        }

        maybeCreate("iosMain").dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

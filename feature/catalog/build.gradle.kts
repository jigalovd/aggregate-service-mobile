plugins {
    id("feature-module")
}

android {
    namespace = "com.aggregateservice.feature.catalog"
}

kotlin {
    sourceSets {
        maybeCreate("commonMain").dependencies {
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.coroutines.core)

            // Koin Compose for koinInject() in @Composable functions
            implementation(libs.koin.compose)

            // i18n for localized strings
            implementation(project(":core:i18n"))

            // Auth state access via core:navigation abstraction
            // AuthStateProvider is implemented by feature:auth and injected via Koin

            // Firebase Auth API types (FirebaseToken) for auth callback
            implementation(project(":core:firebase-auth"))

            // LocationProvider for geo-based provider search
            implementation(project(":core:location"))

            // Shared Location type
            implementation(project(":core:common"))

            // AuthRepository for verifyFirebaseToken after Firebase Auth completes
            // This is needed because ProviderDetailScreen triggers auth flow via AuthPromptDialog
            // and needs to complete the backend verification
            implementation(project(":feature:auth"))

            // Favorites use cases for checking/managing favorite status on provider detail
            implementation(project(":feature:favorites"))
        }
    }
}

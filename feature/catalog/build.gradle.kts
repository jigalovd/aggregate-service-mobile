plugins {
    id("feature-module")
}

android {
    namespace = "com.aggregateservice.feature.catalog"
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
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.coroutines.core)

            // Logging
            implementation(libs.kermit)

            // Koin Compose for koinInject() in @Composable functions
            implementation(libs.koin.compose)

            // i18n for localized strings
            implementation(project(":core:i18n"))

            // Auth state access via core:auth-api contracts
            implementation(project(":core:auth-api"))

            // LocationProvider for geo-based provider search
            implementation(project(":core:location"))

            // Location persistence via DataStore
            implementation(project(":core:storage"))
            implementation(libs.androidx.datastore.preferences)

            // Shared Location type
            implementation(project(":core:common"))

            // Generated API models (DTOs with @Serializable)
            implementation(project(":core:api-models"))

            // Favorites API for checking/managing favorite status on provider detail
            implementation(project(":core:favorites-api"))
        }

        maybeCreate("commonTest").dependencies {
            implementation(kotlin("test"))
            implementation(libs.mockk)
            implementation(libs.koin.test)
            implementation(libs.ktor.client.mock)
        }
    }
}

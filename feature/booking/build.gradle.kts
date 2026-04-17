plugins {
    id("feature-module")
}

android {
    namespace = "com.aggregateservice.feature.booking"
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

            // Generated API models from OpenAPI spec
            implementation(project(":core:api-models"))

            // NOTE: feature:booking is isolated from feature:catalog
            // See docs/architecture/FEATURE_ISOLATION.md for details
        }
    }
}

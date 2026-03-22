plugins {
    id("feature-module")
}

android {
    namespace = "com.aggregateservice.feature.booking"
}

kotlin {
    sourceSets {
        maybeCreate("commonMain").dependencies {
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.datetime)

            // NOTE: feature:booking is isolated from feature:catalog
            // See docs/architecture/FEATURE_ISOLATION.md for details
        }
    }
}

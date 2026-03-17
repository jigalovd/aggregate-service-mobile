plugins {
    id("core-module")
    id("org.jetbrains.kotlin.plugin.serialization") // [cite: 16]
}

android {
    namespace = "com.aggregateservice.core.network" // [cite: 16]
}

kotlin {
    sourceSets {
        maybeCreate("commonMain").dependencies {
            implementation(libs.ktor.client.core) // [cite: 16]
            implementation(libs.ktor.client.content.negotiation) // [cite: 16]
            implementation(libs.ktor.serialization.kotlinx.json) // [cite: 16]
            implementation(libs.kotlinx.serialization.json) // [cite: 17]
        }

        maybeCreate("androidMain").dependencies {
            implementation(libs.ktor.client.android) // [cite: 17]
        }

        maybeCreate("iosMain").dependencies {
            implementation(libs.ktor.client.darwin) //
        }
    }
}
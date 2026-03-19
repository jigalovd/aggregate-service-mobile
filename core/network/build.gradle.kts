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
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.auth)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.serialization.json)
        }

        maybeCreate("androidMain").dependencies {
            implementation(libs.ktor.client.okhttp)
        }

        maybeCreate("iosMain").dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

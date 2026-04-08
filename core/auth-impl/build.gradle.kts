plugins {
    id("core-module")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.aggregateservice.core.authImpl"
}

kotlin {
    sourceSets {
        maybeCreate("commonMain").dependencies {
            implementation(project(":core:auth-api"))
            implementation(project(":core:network"))
            implementation(project(":core:storage"))
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.auth)
            implementation(libs.ktor.serialization.kotlinx.json)
        }

        maybeCreate("commonTest").dependencies {
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
            implementation(libs.mockk)
            implementation(libs.ktor.client.mock)
        }
    }
}

plugins {
    id("feature-module")
}

android {
    namespace = "com.aggregateservice.feature.auth"
}

kotlin {
    sourceSets {
        maybeCreate("commonMain").dependencies {
            // Core modules - NOW DEPENDS ON core:auth-api and core:auth-impl
            implementation(project(":core:auth-api"))
            implementation(project(":core:auth-impl"))
            implementation(project(":core:firebase-auth"))
            implementation(project(":core:i18n"))
            implementation(project(":core:config"))
            implementation(project(":core:storage"))
            implementation(project(":core:network"))

            // Network
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.auth)
            implementation(libs.ktor.serialization.kotlinx.json)

            // DI
            implementation(libs.koin.core)
            implementation(libs.koin.compose)

            // Navigation
            implementation(libs.voyager.navigator)
            implementation(libs.voyager.screenModel)
            implementation(libs.voyager.koin)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)
        }
        maybeCreate("commonTest").dependencies {
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
            implementation(libs.mockk)
            implementation(libs.ktor.client.mock)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
        }
    }
}

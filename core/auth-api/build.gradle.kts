plugins {
    id("core-module")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.aggregateservice.core.authApi"
}

kotlin {
    sourceSets {
        maybeCreate("commonMain").dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.voyager.navigator)
            // NOTE: No dependency on core:network — AuthError is defined locally
            // to keep core:auth-api as pure Kotlin with zero framework deps
        }

        maybeCreate("commonTest").dependencies {
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
        }
    }
}

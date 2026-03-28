plugins {
    id("core-module")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.aggregateservice.core.firebaseAuth"
}

kotlin {
    sourceSets {
        maybeCreate("commonMain").dependencies {
            implementation(project(":core:config"))
            implementation(libs.ktor.client.core)
            implementation(libs.kotlinx.serialization.json)
        }

        maybeCreate("androidMain").dependencies {
            implementation(libs.firebase.bom)
            implementation(libs.firebase.auth.ktx)
            implementation(libs.google.play.services.auth)
        }

        maybeCreate("iosMain").dependencies {
            // Stub - KMP-only implementation
        }
    }
}
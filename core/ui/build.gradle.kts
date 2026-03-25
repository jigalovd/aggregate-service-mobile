plugins {
    id("core-module")
    id("kmp-compose")
}

android {
    namespace = "com.aggregateservice.core.ui"
}

kotlin {
    sourceSets {
        maybeCreate("commonMain").dependencies {
            // Theme module for design tokens
            api(project(":core:theme"))

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)
        }

        maybeCreate("androidMain").dependencies {
            // Android-specific compose preview
            implementation(libs.androidx.compose.ui.tooling.preview)
        }
    }
}

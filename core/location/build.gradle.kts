plugins {
    id("core-module")
}

android {
    namespace = "com.aggregateservice.core.location"
}

kotlin {
    sourceSets {
        maybeCreate("commonMain").dependencies {
            implementation(project(":core:config"))
        }

        maybeCreate("androidMain").dependencies {
            implementation(libs.google.play.services.location)
            implementation(libs.androidx.activity.compose)
        }

        maybeCreate("iosMain").dependencies {
            // Stub - no dependencies needed
        }
    }
}
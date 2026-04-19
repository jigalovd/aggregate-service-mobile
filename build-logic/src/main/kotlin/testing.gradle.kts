import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform")
}

val libs = the<org.gradle.api.artifacts.VersionCatalogsExtension>().named("libs")

kotlin {
    // Common tests - shared across all platforms
    sourceSets {
        maybeCreate("commonTest").dependencies {
            implementation(kotlin("test"))
            implementation(libs.findLibrary("kotlinx.coroutines.test").get())
            implementation(libs.findLibrary("ktor-client-mock").get())
        }

        // Android unit tests
        maybeCreate("androidUnitTest").dependencies {
            implementation(libs.findLibrary("androidx.test.ext.junit").get())
            implementation(libs.findLibrary("androidx.test.runner").get())
            implementation(libs.findLibrary("robolectric").get())
            implementation(libs.findLibrary("compose.ui.test.junit4").get())
        }

        // Android instrumented tests (run on device/emulator or with Robolectric)
        maybeCreate("androidInstrumentedTest").dependencies {
            implementation(libs.findLibrary("androidx.test.ext.junit").get())
            implementation(libs.findLibrary("androidx.test.runner").get())
            implementation(libs.findLibrary("robolectric").get())
            implementation(libs.findLibrary("compose.ui.test.junit4").get())
            implementation(libs.findLibrary("compose.ui.test.manifest").get())
        }
    }
}

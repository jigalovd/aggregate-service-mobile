val libs = the<org.gradle.api.artifacts.VersionCatalogsExtension>().named("libs")

plugins {
    id("core-module")
}

android {
    namespace = "com.aggregateservice.core.test.utils"
}

kotlin {
    sourceSets {
        maybeCreate("commonMain").dependencies {
            implementation(libs.findLibrary("kotlinx.coroutines.core").get())
            implementation(libs.findLibrary("kotlinx.coroutines.test").get())
            implementation(libs.findLibrary("koin.core").get())
            api(libs.findLibrary("turbine").get())
        }

        maybeCreate("commonTest").dependencies {
            implementation(libs.findLibrary("koin.test").get())
        }
    }
}

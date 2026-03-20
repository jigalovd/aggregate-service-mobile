plugins {
    id("core-module")
    kotlin("plugin.serialization")
}

android {
    namespace = "com.aggregateservice.core.i18n"
}

kotlin {
    sourceSets {
        maybeCreate("commonMain").dependencies {
            implementation(project(":core:config"))
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.koin.core)
        }
    }
}

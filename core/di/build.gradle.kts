plugins {
    id("core-module")
}

android {
    namespace = "com.aggregateservice.core.di"
}

kotlin {
    sourceSets {
        maybeCreate("commonMain").dependencies {
            implementation(libs.koin.core)
        }
    }
}

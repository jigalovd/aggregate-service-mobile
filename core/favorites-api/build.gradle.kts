plugins {
    id("core-module")
}

android {
    namespace = "com.aggregateservice.core.favoritesApi"
}

kotlin {
    sourceSets {
        maybeCreate("commonMain").dependencies {
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}

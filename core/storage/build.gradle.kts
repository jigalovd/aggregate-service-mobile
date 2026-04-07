plugins {
    id("core-module")
}

android {
    namespace = "com.aggregateservice.core.storage"
}

kotlin {
    sourceSets {
        maybeCreate("commonMain").dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.androidx.datastore.preferences)
        }

        maybeCreate("androidMain").dependencies {
            implementation(libs.androidx.datastore.preferences.android)
            implementation(libs.koin.android) // Для androidContext()
        }

        maybeCreate("commonTest").dependencies {
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.mockk)
        }
    }
}

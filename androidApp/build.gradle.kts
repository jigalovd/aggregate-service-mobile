plugins {
    id("app-module")
}

android {
    namespace = "com.aggregateservice.androidApp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.aggregateservice"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

kotlin {
    sourceSets {
        maybeCreate("androidMain").dependencies {
            implementation(project(":app"))
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(libs.androidx.activity.compose)
            implementation(libs.koin.android)
        }
    }
}

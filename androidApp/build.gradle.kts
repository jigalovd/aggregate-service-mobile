plugins {
    id("app-module")
}

android {
    namespace = "com.aggregateservice.androidApp"
    compileSdk = 36

    // Enable BuildConfig generation
    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.aggregateservice"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        // API Configuration (DEV by default)
        buildConfigField("String", "API_BASE_URL", "\"https://api.dev.aggregateservice.com\"")
        buildConfigField("String", "API_KEY", "\"${project.findProperty("api.key") ?: System.getenv("API_KEY") ?: "\""}")
        buildConfigField("String", "ENVIRONMENT", "\"DEV\"")
        buildConfigField("boolean", "DEBUG", "true")
        buildConfigField("boolean", "ENABLE_LOGGING", "true")
        buildConfigField("long", "NETWORK_TIMEOUT_MS", "30000L")
        buildConfigField("String", "API_VERSION", "\"v1\"")
    }

    buildTypes {
        debug {
            // Debug configuration
            buildConfigField("String", "API_BASE_URL", "\"https://api.dev.aggregateservice.com\"")
            buildConfigField("String", "ENVIRONMENT", "\"DEV\"")
            buildConfigField("boolean", "DEBUG", "true")
            buildConfigField("boolean", "ENABLE_LOGGING", "true")
        }

        release {
            // Release configuration
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            buildConfigField("String", "API_BASE_URL", "\"https://api.staging.aggregateservice.com\"")
            buildConfigField("String", "ENVIRONMENT", "\"STAGING\"")
            buildConfigField("boolean", "DEBUG", "false")
            buildConfigField("boolean", "ENABLE_LOGGING", "false")
        }
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
            implementation(project(":core:di"))
            implementation(project(":core:config"))
            implementation(project(":core:navigation"))
            implementation(project(":feature:auth"))
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(libs.androidx.activity.compose)
            implementation(libs.koin.android)
            implementation(libs.voyager.navigator)
            implementation(libs.voyager.screenModel)
            implementation(libs.voyager.transitions)
        }
    }
}

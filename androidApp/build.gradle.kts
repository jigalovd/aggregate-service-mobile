plugins {
    id("app-module")
    alias(libs.plugins.googleServices)
}

// ============================================================
// Environment Configuration (centralized)
// ============================================================
val envConfig = mapOf(
    "debug" to mapOf(
        "API_BASE_URL" to "https://api.dev.aggregateservice.com",
        "ENVIRONMENT" to "DEV",
        "DEBUG" to "true",
        "ENABLE_LOGGING" to "true"
    ),
    "release" to mapOf(
        "API_BASE_URL" to "https://api.staging.aggregateservice.com",
        "ENVIRONMENT" to "STAGING",
        "DEBUG" to "false",
        "ENABLE_LOGGING" to "false"
    )
)

// Helper function to apply BuildConfig fields
fun com.android.build.api.dsl.DefaultConfig.buildConfigFromMap(config: Map<String, String>) {
    buildConfigField("String", "API_BASE_URL", "\"${config["API_BASE_URL"]}\"")
    buildConfigField("String", "API_KEY", "\"${project.findProperty("api.key") ?: System.getenv("API_KEY") ?: ""}\"")
    buildConfigField("String", "ENVIRONMENT", "\"${config["ENVIRONMENT"]}\"")
    buildConfigField("boolean", "DEBUG", config["DEBUG"]!!)
    buildConfigField("boolean", "ENABLE_LOGGING", config["ENABLE_LOGGING"]!!)
    buildConfigField("long", "NETWORK_TIMEOUT_MS", "30000L")
    buildConfigField("String", "API_VERSION", "\"v1\"")
}

fun com.android.build.api.dsl.BuildType.buildConfigFromMap(config: Map<String, String>) {
    buildConfigField("String", "API_BASE_URL", "\"${config["API_BASE_URL"]}\"")
    buildConfigField("String", "ENVIRONMENT", "\"${config["ENVIRONMENT"]}\"")
    buildConfigField("boolean", "DEBUG", config["DEBUG"]!!)
    buildConfigField("boolean", "ENABLE_LOGGING", config["ENABLE_LOGGING"]!!)
}

android {
    namespace = "com.aggregateservice.androidApp"
    compileSdk = 36

    // Enable BuildConfig generation
    buildFeatures {
        buildConfig = true
    }

    // Add centralized assets directory (logback.xml)
    sourceSets {
        getByName("main") {
            assets.srcDirs("../config/logging")
        }
    }

    defaultConfig {
        applicationId = "com.aggregateservice"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        // Apply debug config as default
        buildConfigFromMap(envConfig["debug"]!!)
    }

    buildTypes {
        debug {
            // Debug configuration (uses default)
            buildConfigFromMap(envConfig["debug"]!!)
        }

        release {
            // Release configuration
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            buildConfigFromMap(envConfig["release"]!!)
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

// ============================================================
// Logback configuration from centralized location
// Using Android sourceSets instead of Copy task for configuration cache compatibility
// ============================================================

kotlin {
    sourceSets {
        maybeCreate("androidMain").dependencies {
            implementation(project(":app"))
            implementation(project(":core:di"))
            implementation(project(":core:config"))
            implementation(project(":core:navigation"))
            implementation(project(":core:theme"))
            implementation(project(":core:i18n"))
            implementation(project(":feature:auth"))
            implementation(project(":feature:services"))
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(libs.androidx.activity.compose)
            implementation(libs.koin.android)
            implementation(libs.voyager.navigator)
            implementation(libs.voyager.screenModel)
            implementation(libs.voyager.transitions)

            // Logging - SLF4J + Logback for Android
            implementation(libs.slf4j.api)
            implementation(libs.logback.android)
        }
    }
}

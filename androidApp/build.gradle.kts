plugins {
    id("app-module")
    alias(libs.plugins.googleServices)
}

// ============================================================
// Environment Configuration (centralized)
// API_BASE_URL can be overridden via:
//   - gradle.properties: api.base.url=http://localhost:8080
//   - local.properties: api.base.url=http://10.0.2.2:8080 (for emulator)
// ============================================================
val apiBaseUrl = project.findProperty("api.base.url") as String? ?: "https://api.dev.aggregateservice.com"

val envConfig =
    mapOf(
        "debug" to
            mapOf(
                "API_BASE_URL" to apiBaseUrl,
                "ENVIRONMENT" to "DEV",
                "DEBUG" to "true",
                "ENABLE_LOGGING" to "true",
            ),
        "release" to
            mapOf(
                "API_BASE_URL" to apiBaseUrl,
                "ENVIRONMENT" to "STAGING",
                "DEBUG" to "false",
                "ENABLE_LOGGING" to "false",
            ),
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

kotlin {
    sourceSets {
        maybeCreate("androidMain").dependencies {
            implementation(project(":app"))
            implementation(project(":core:di"))
            implementation(project(":core:config"))
            implementation(project(":core:firebase-auth"))
            implementation(project(":core:location"))
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
            implementation(libs.ktor.client.core)
            implementation(libs.voyager.navigator)
            implementation(libs.voyager.screenModel)
            implementation(libs.voyager.transitions)

            // Logging - Kermit via core:logging module
            implementation(project(":core:logging"))
            implementation("uk.uuid.slf4j:slf4j-android:2.0.16-0")
        }

        // Android unit tests
        maybeCreate("androidUnitTest").dependencies {
            implementation(libs.androidx.test.ext.junit)
            implementation(libs.androidx.test.runner)
            implementation(libs.robolectric)
            implementation(libs.compose.ui.test.junit4)
            implementation(libs.kotlinx.datetime)
            implementation(project(":app"))
            implementation(project(":core:common"))
        }

        // Android instrumented tests
        maybeCreate("androidInstrumentedTest").dependencies {
            implementation(libs.androidx.test.ext.junit)
            implementation(libs.androidx.test.runner)
            implementation(libs.robolectric)
            implementation(libs.compose.ui.test.junit4)
            implementation(libs.kotlinx.datetime)
            implementation(project(":app"))
        }
    }
}

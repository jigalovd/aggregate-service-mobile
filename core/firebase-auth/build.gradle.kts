plugins {
    id("core-module")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.aggregateservice.core.firebaseAuth"
    buildFeatures {
        buildConfig = true
    }
}

val googleClientId = project.findProperty("GOOGLE_CLIENT_ID") as? String ?: ""

kotlin {
    sourceSets {
        maybeCreate("commonMain").dependencies {
            implementation(project(":core:config"))
            implementation(project(":core:auth-api"))
            implementation(libs.ktor.client.core)
            implementation(libs.kotlinx.serialization.json)
        }

        maybeCreate("androidMain").dependencies {
            implementation(libs.firebase.auth.ktx)
            // AndroidX Credentials — replaces deprecated GoogleSignIn API
            implementation("androidx.credentials:credentials:1.5.0")
            implementation("androidx.credentials:credentials-play-services-auth:1.5.0")
            implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
        }

        maybeCreate("iosMain").dependencies {
            // Stub — iOS implementation deferred
        }

        maybeCreate("commonTest").dependencies {
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
            implementation(libs.mockk)
        }
    }
}

android {
    defaultConfig {
        buildConfigField("String", "GOOGLE_SERVER_CLIENT_ID", "\"$googleClientId\"")
    }
}

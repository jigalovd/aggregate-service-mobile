plugins {
    id("com.android.application")
    id("kmp-compose")
}

android {
    namespace = "com.aggregateservice"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.aggregateservice"
        minSdk = 24
        //noinspection OldTargetApi
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
}

configure<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension> {
    // Настраиваем таргет здесь, так как применен 'com.android.application'
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }
    }
}
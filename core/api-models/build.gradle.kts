import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinSerialization)
    id("org.jlleitschuh.gradle.ktlint")
}

ktlint {
    filter {
        // Exclude generated API models from ktlint checks
        exclude("**/com/aggregateservice/core/api/models/**")
    }
}

android {
    namespace = "com.aggregateservice.core.api.models"
    compileSdk = 36
    defaultConfig {
        minSdk = 24
    }
}

kotlin {
    jvmToolchain(21)

    jvm()
    androidTarget {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_21)
                }
            }
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0")
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1-0.6.x-compat")
        }
    }
}

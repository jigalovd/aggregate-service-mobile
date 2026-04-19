plugins {
    id("com.android.library")
    id("kmp-base")
}

android {
    compileSdk = 36
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

configure<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension> {
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }
    }

    // JVM target for Kover coverage measurement — Kover only instruments JVM bytecode
    jvm("jvm") {
        testRuns["test"].executionTask.configure {
            useJUnit()
        }
    }
}
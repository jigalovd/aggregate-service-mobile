plugins {
    kotlin("multiplatform")
}

configure<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension> {
    if(System.getProperty("os.name").contains("Mac", ignoreCase = true)) {
        listOf(
            iosX64(),
            iosArm64(),
            iosSimulatorArm64() // [cite: 9]
        ).forEach { iosTarget ->
            iosTarget.binaries.framework {
                baseName = project.name
                isStatic = true
            }
        }
    }
}
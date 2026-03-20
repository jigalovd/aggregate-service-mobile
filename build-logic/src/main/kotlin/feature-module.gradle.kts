val libs = the<org.gradle.api.artifacts.VersionCatalogsExtension>().named("libs")

plugins {
    id("kmp-android")
    id("kmp-compose")
    id("testing")
    kotlin("plugin.serialization")
}

configure<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension> {
    sourceSets {
        // Используем maybeCreate для защиты от порядка выполнения плагинов
        maybeCreate("commonMain").dependencies {
            api(project(":core:config"))
            api(project(":core:network"))
            api(project(":core:storage"))
            api(project(":core:theme"))
            api(project(":core:i18n"))
            api(project(":core:utils"))
            api(project(":core:navigation"))
            api(project(":core:di"))

            // Библиотеки фичи
            implementation(libs.findLibrary("kotlinx.coroutines.core").get())
            implementation(libs.findLibrary("kotlinx.serialization.json").get())
            implementation(libs.findLibrary("koin.core").get())
            implementation(libs.findLibrary("voyager.navigator").get())
            implementation(libs.findLibrary("voyager.screenModel").get())
            implementation(libs.findLibrary("voyager.koin").get())
        }

        maybeCreate("androidMain").dependencies {
            implementation(libs.findLibrary("ktor-client-okhttp").get())
        }

        maybeCreate("iosMain").dependencies {
            implementation(libs.findLibrary("ktor.client.darwin").get())
        }
    }
}
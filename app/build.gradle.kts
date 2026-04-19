plugins {
    id("feature-module")
}

android {
    namespace = "com.aggregateservice.mobile.app"
}

kotlin {
    sourceSets {
        maybeCreate("commonMain").dependencies {
            api(project(":feature:auth"))
            api(project(":feature:catalog"))
            api(project(":feature:booking"))
            api(project(":feature:profile"))
            api(project(":feature:favorites"))
            api(project(":feature:schedule"))
            api(project(":feature:reviews"))
            implementation(project(":core:auth-api"))
            implementation(project(":core:common"))
            implementation(libs.koin.compose)
        }

        maybeCreate("androidUnitTest").dependencies {
            implementation(libs.kotlinx.datetime)
        }
    }
}

// For KMP library modules, test manifests need to be in the standard location
// and will be automatically picked up by the Android Gradle Plugin

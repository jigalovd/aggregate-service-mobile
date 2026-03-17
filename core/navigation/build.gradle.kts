plugins {
    id("core-module")
    id("kmp-compose")
}

android {
    namespace = "com.aggregateservice.core.navigation"
}

kotlin {
    sourceSets {
        maybeCreate("commonMain").dependencies {
            implementation(libs.voyager.navigator)
            implementation(libs.voyager.screenModel)
        }
    }
}

plugins {
    id("core-module")
}

android {
    namespace = "com.aggregateservice.core.utils"
}

kotlin {
    sourceSets {
        maybeCreate("commonMain").dependencies {
            implementation(project(":core:config"))
        }
    }
}

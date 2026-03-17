plugins {
    id("core-module")
}

android {
    namespace = "com.aggregateservice.core.storage"
}

kotlin {
    sourceSets {
        maybeCreate("commonMain").dependencies {
            implementation(libs.datastore.preferences)
        }
    }
}

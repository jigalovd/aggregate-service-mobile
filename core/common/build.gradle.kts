plugins {
    id("core-module")
}

android {
    namespace = "com.aggregateservice.core.common"
}

kotlin {
    sourceSets {
        maybeCreate("commonMain").dependencies {
            // stdlib only - no external dependencies
        }
    }
}

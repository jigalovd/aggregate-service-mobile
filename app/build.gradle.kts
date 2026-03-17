plugins {
    id("feature-module")
}

android {
    namespace = "com.aggregateservice.app"
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
        }
    }
}

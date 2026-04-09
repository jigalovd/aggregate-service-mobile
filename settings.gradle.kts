rootProject.name = "aggregate-service-mobile"

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/patch")
        maven("https://plugins.gradle.org/m2/")
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/patch")
        maven("https://s01.oss.sonatype.org/content/repositories/releases/")
    }
}

// Core modules
include(":core:network")
include(":core:storage")
include(":core:theme")
include(":core:i18n")
include(":core:utils")
include(":core:navigation")
include(":core:common")
include(":core:config")
include(":core:logging")
include(":core:di")
include(":core:auth-api")
include(":core:auth-impl")
include(":core:firebase-auth")
include(":core:location")
include(":core:test-utils")
include(":core:ui")

// Feature modules
include(":feature:auth")
include(":feature:catalog")
include(":feature:booking")
include(":feature:services")
include(":feature:profile")
include(":feature:favorites")
include(":feature:schedule")
include(":feature:reviews")

// App module (aggregator)
include(":app")

// Platform applications
include(":androidApp")

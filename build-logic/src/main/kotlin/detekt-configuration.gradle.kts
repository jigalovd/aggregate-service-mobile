plugins {
    id("io.gitlab.arturbosch.detekt")
}

detekt {
    // Build upon default Detekt configuration
    buildUponDefaultConfig = true

    // Don't use all Detekt rules (only enabled ones in config.yml)
    allRules = false

    // Use custom Detekt config
    config.setFrom(files("$rootDir/.detekt/config.yml"))

    // Disable baseline (zero tolerance policy)
    baseline = null

    // Source sets to analyze
    source.setFrom(
        "src/commonMain/kotlin",
        "src/androidMain/kotlin",
        "src/iosMain/kotlin"
    )
}

// Add Detekt formatting plugin
dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.6")
}

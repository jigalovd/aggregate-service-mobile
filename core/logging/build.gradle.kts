plugins {
    id("core-module")
}

android {
    namespace = "com.aggregateservice.core.logging"
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(libs.kermit)
                implementation(libs.koin.core)
                implementation(project(":core:config"))
            }
        }
        androidMain {
            dependencies {
                implementation(libs.kermit.io)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.kermit.test)
            }
        }
    }
}

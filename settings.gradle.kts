rootProject.name = "apoML"

plugins {
    // this plugin can auto-provision the required JVM
    id("org.gradle.toolchains.foojay-resolver-convention") version("0.7.0")
}

include("apoML")



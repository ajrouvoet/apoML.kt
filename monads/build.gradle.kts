plugins {
    kotlin("jvm") version "1.9.21"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("io.arrow-kt:arrow-core:1.2.0")

}

tasks.withType(Test::class) {
    useJUnitPlatform()
}

application {
    mainClass.set("ifp.monads.app")
}

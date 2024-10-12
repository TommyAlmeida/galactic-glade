plugins {
    kotlin("jvm") version "2.0.20"
}

group = "io.poin.game"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("net.minestom:minestom-snapshots:1c47dd613f")
    implementation("de.articdive:jnoise-pipeline:4.1.0")
}

tasks.test {
    useJUnitPlatform()
}
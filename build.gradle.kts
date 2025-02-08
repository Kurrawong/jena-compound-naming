val projectVersion: String by project
val log4jVersion: String by project
val kotlinxSerializationJsonVersion: String by project
val jenaVersion: String by project

plugins {
    kotlin("jvm") version "1.8.21"
    kotlin("plugin.serialization") version "1.8.21"
    application
}

group = "ai.kurrawong.jena"
version = projectVersion

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.apache.logging.log4j:log4j:$log4jVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationJsonVersion")
    implementation("org.apache.jena:jena-core:$jenaVersion")
    implementation("org.apache.jena:jena-arq:$jenaVersion")
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<Jar>("uberJar") {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("MainKt")
}
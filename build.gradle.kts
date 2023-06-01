val log4j_version: String by project
val kotlinx_serialization_json_version: String by project
val jena_core_version: String by project
val jena_arq_version: String by project

plugins {
    kotlin("jvm") version "1.8.21"
    kotlin("plugin.serialization") version "1.8.21"
    application
}

group = "com.edmondchuc.jena-playground"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.apache.logging.log4j:log4j:$log4j_version")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinx_serialization_json_version")
    implementation("org.apache.jena:jena-core:$jena_core_version")
    implementation("org.apache.jena:jena-arq:$jena_arq_version")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}

application {
    mainClass.set("MainKt")
}
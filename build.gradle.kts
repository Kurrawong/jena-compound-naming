import java.util.jar.JarFile

val projectVersion: String by project
val jenaVersion: String by project

plugins {
    java
}

group = "ai.kurrawong.jena"
version = projectVersion

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    compileOnly("org.apache.jena:jena-arq:$jenaVersion")

    testImplementation("org.apache.jena:jena-arq:$jenaVersion")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

val inspectJar by tasks.registering {
    group = "verification"
    description = "Fail if the extension JAR bundles Jena, Fuseki, Kotlin, or other host-provided classes."
    dependsOn(tasks.jar)

    doLast {
        val jarFile = tasks.jar.get().archiveFile.get().asFile
        val forbiddenPrefixes =
            listOf(
                "org/apache/jena/",
                "org/apache/kotlin/",
                "kotlin/",
                "kotlinx/",
                "org/jetbrains/kotlin/",
                "org/apache/logging/",
            )
        JarFile(jarFile).use { jar ->
            val violations =
                jar.entries()
                    .asSequence()
                    .map { it.name }
                    .filter { name -> forbiddenPrefixes.any { name.startsWith(it) } }
                    .sorted()
                    .toList()
            if (violations.isNotEmpty()) {
                throw GradleException(
                    "Extension JAR ${jarFile.name} contains host-provided or unintended classes:\n" +
                        violations.joinToString("\n"),
                )
            }
        }
    }
}

tasks.named("check") {
    dependsOn(inspectJar)
}

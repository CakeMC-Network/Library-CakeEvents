import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.net.URI
import java.nio.charset.StandardCharsets
import java.util.*

plugins {
    id("idea")

    id("maven-publish")
    id("com.gradleup.shadow") version "8.3.0"

    kotlin("jvm") version "2.0.0"
}

group = "net.cakemc.lib"
version = "0.0.0-develop"

val repoProperties = Properties()
val repoFile = file("./credentials.properties")
if (repoFile.exists())
    repoProperties.load(repoFile.inputStream())
val repoUsername: String = (repoProperties["username"] ?: System.getenv("REPOSITORY_USERNAME")).toString()
val repoPassword: String = (repoProperties["password"] ?: System.getenv("REPOSITORY_PASSWORD")).toString()

repositories {
    mavenCentral()
    //mavenLocal()
    maven {
        name = "cakemc-nexus"
        url = URI.create("http://cakemc.net:8081/releases")
        credentials {
            username = repoUsername
            password = repoPassword
        }
        isAllowInsecureProtocol = true
    }
}

@Suppress("unchecked_cast")
fun <V> prop(value: String): V {
    return properties.getValue(value) as V
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    shadow(kotlin("stdlib-jdk8"))
}
val jdkVersion = JavaVersion.VERSION_21
val jdkVersionString = jdkVersion.toString()

java {
    toolchain.languageVersion = JavaLanguageVersion.of(jdkVersionString)
    withSourcesJar()
}

tasks.withType<JavaCompile> {
    options.encoding = StandardCharsets.UTF_8.toString()
}

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}

tasks.withType<AbstractArchiveTask> {
    isReproducibleFileOrder = true
    isPreserveFileTimestamps = false
}

tasks.withType<ShadowJar> {
    configurations = listOf(project.configurations.shadow.get())
    isZip64 = true
}

configurations.shadow { isTransitive = false }

publishing {
    publications.create<MavenPublication>(rootProject.name) {
        artifact(tasks.shadowJar)
    }
    repositories {
        maven {
            name = "cakemc"
            url = URI.create("http://cakemc.net:8081/releases")
            credentials {
                username = repoUsername
                password = repoPassword
            }
            isAllowInsecureProtocol = true
        }
    }
}
kotlin {
    jvmToolchain(21)
}
import io.gitlab.arturbosch.detekt.Detekt

object Meta {
    const val artifactId = "schema-kenerator-serialization"
}

val schemaKeneratorVersion: String by project
val schemaKeneratorGroupId: String by project
group = schemaKeneratorGroupId
version = schemaKeneratorVersion

plugins {
    kotlin("jvm")
    id("org.owasp.dependencycheck")
    id("io.gitlab.arturbosch.detekt")
    kotlin("plugin.serialization") version "1.9.21"
    `maven-publish`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":schema-kenerator-core"))
    implementation(kotlin("reflect"))
    val versionKotlinxSerializationJson: String by project
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$versionKotlinxSerializationJson")
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = Meta.artifactId
            from(components["java"])
            pom {
                name.set("schema-kenerator")
                description.set("Kotlin generator for various schemas")
                url.set("https://github.com/SMILEY4/schema-kenerator")
            }
        }
    }
}

tasks.withType<Detekt>().configureEach {
    ignoreFailures = false
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom("$projectDir/../detekt/detekt.yml")
    reports {
        html.required.set(true)
        md.required.set(true)
        xml.required.set(false)
        txt.required.set(false)
        sarif.required.set(false)
    }
}

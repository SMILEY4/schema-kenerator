import io.gitlab.arturbosch.detekt.Detekt

object Meta {
    const val artifactId = "schema-kenerator-swagger"
}

val schemaKeneratorVersion: String by project
val schemaKeneratorGroupId: String by project
group = schemaKeneratorGroupId
version = schemaKeneratorVersion

plugins {
    kotlin("jvm")
    id("org.owasp.dependencycheck")
    id("io.gitlab.arturbosch.detekt")
    `maven-publish`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":schema-kenerator-core"))
    val versionSwaggerParser: String by project
    implementation("io.swagger.parser.v3:swagger-parser:$versionSwaggerParser")
}

java {
    withJavadocJar()
    withSourcesJar()
}

kotlin {
    jvmToolchain(11)
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

detekt {
    ignoreFailures = false
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom("$projectDir/../detekt/detekt.yml")
}
tasks.withType<Detekt>().configureEach {
    reports {
        html.required.set(true)
        md.required.set(true)
        xml.required.set(false)
        txt.required.set(false)
        sarif.required.set(false)
    }
}
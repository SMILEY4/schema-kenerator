import io.gitlab.arturbosch.detekt.Detekt

plugins {
    kotlin("jvm") version "1.9.21"
    id("org.owasp.dependencycheck") version "8.2.1"
    id("io.gitlab.arturbosch.detekt") version "1.23.0"
    kotlin("plugin.serialization") version "1.9.21"
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("io.kotest:kotest-runner-junit5:5.8.0")
    testImplementation("io.kotest:kotest-framework-datatest:5.8.0")
    testImplementation("io.kotest:kotest-assertions-core:5.8.0")
    testImplementation("io.kotest:kotest-assertions-json:5.8.0")
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.3")
    testImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    testImplementation("com.fasterxml.jackson.core:jackson-annotations:2.15.3")
    testImplementation("io.swagger.parser.v3:swagger-parser:2.1.20")
    testImplementation(project(":schema-kenerator-core"))
    testImplementation(project(":schema-kenerator-reflection"))
    testImplementation(project(":schema-kenerator-serialization"))
    testImplementation(project(":schema-kenerator-jsonschema"))
    testImplementation(project(":schema-kenerator-swagger"))
    testImplementation(project(":schema-kenerator-jackson"))
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
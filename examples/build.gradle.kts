plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {

    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotest.assertions.json)

    testImplementation(libs.jackson.module.kotlin)
    testImplementation(libs.jackson.annotations)

    testImplementation(libs.kotlinx.serialization.json)

    testImplementation(libs.swagger.parser)
    constraints {
        testImplementation("commons-codec:commons-codec:1.13") {
            because("Version 1.11 has a known vulnerability (pulled in via 'io.swagger.parser.v3:swagger-parser').")
        }
    }

    testImplementation(libs.javax.validation.api)
    testImplementation(libs.jakarta.validation.api)

    testImplementation(project(":schema-kenerator-core"))
    testImplementation(project(":schema-kenerator-reflection"))
    testImplementation(project(":schema-kenerator-serialization"))
    testImplementation(project(":schema-kenerator-jsonschema"))
    testImplementation(project(":schema-kenerator-swagger"))
    testImplementation(project(":schema-kenerator-jackson"))
    testImplementation(project(":schema-kenerator-jackson-jsonschema"))
    testImplementation(project(":schema-kenerator-jackson-swagger"))
    testImplementation(project(":schema-kenerator-validation-swagger"))
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
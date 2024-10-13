plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

repositories {
    mavenCentral()
}

dependencies {
    val versionKotest: String by project
    val versionJackson: String by project
    val versionKotlinxSerializationJson: String by project
    val versionSwaggerParser: String by project
    val versionJavaxValidation: String by project
    val versionJakartaValidation: String by project
    testImplementation("io.kotest:kotest-runner-junit5:$versionKotest")
    testImplementation("io.kotest:kotest-framework-datatest:$versionKotest")
    testImplementation("io.kotest:kotest-assertions-core:$versionKotest")
    testImplementation("io.kotest:kotest-assertions-json:$versionKotest")
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:$versionJackson")
    testImplementation("com.fasterxml.jackson.core:jackson-annotations:$versionJackson")
    testImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$versionKotlinxSerializationJson")
    testImplementation("io.swagger.parser.v3:swagger-parser:$versionSwaggerParser")
    testImplementation("javax.validation:validation-api:$versionJavaxValidation")
    testImplementation("jakarta.validation:jakarta.validation-api:$versionJakartaValidation")
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
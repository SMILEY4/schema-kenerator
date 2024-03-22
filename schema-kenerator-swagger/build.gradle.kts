object Meta {
    const val artifactId = "schema-kenerator-swagger"
}

plugins {
}

dependencies {
    implementation(project(":schema-kenerator-core"))
    implementation("io.swagger.parser.v3:swagger-parser:2.1.20")

    implementation("io.kotest:kotest-assertions-json:5.8.0")
    testImplementation(project(":schema-kenerator-reflection"))
    testImplementation(project(":schema-kenerator-test-utils"))
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.3")
}

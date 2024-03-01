object Meta {
    const val artifactId = "schema-kenerator-swagger"
}

plugins {
}

dependencies {
    implementation(project(":schema-kenerator-core"))
    implementation("io.swagger.parser.v3:swagger-parser:2.1.20")

    testImplementation(project(":schema-kenerator-test-utils"))
}

object Meta {
    const val artifactId = "schema-kenerator-jsonschema"
}

plugins {
}

dependencies {
    implementation(project(":schema-kenerator-core"))

    implementation("io.kotest:kotest-assertions-json:5.8.0")
    testImplementation(project(":schema-kenerator-reflection"))
    testImplementation(project(":schema-kenerator-test"))
}

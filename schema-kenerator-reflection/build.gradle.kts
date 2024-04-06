object Meta {
    const val artifactId = "schema-kenerator-reflection"
}

plugins {
}

dependencies {
    implementation(project(":schema-kenerator-core"))
    implementation(kotlin("reflect"))

    testImplementation(project(":schema-kenerator-test"))
}

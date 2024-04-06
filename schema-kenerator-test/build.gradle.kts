
plugins {
    kotlin("plugin.serialization") version "1.9.21"
}

dependencies {
    implementation(project(":schema-kenerator-core"))
    implementation("io.kotest:kotest-assertions-core:5.8.0")
    implementation("io.kotest:kotest-assertions-json:5.8.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.3")

    testImplementation(project(":schema-kenerator-reflection"))
    testImplementation(project(":schema-kenerator-serialization"))
    testImplementation(project(":schema-kenerator-jsonschema"))
    testImplementation(project(":schema-kenerator-swagger"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

}

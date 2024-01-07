object Meta {
    const val artifactId = "schema-kenerator-serialization"
}

plugins {
    kotlin("plugin.serialization") version "1.9.21"
}

dependencies {
    implementation(project(":schema-kenerator-core"))
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
}

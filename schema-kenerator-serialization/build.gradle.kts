object Meta {
    const val artifactId = "schema-kenerator-serialization"
}

plugins {
    kotlin("plugin.serialization") version "1.9.21"
    `maven-publish`
}

dependencies {
    implementation(project(":schema-kenerator-core"))
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    testImplementation(project(":schema-kenerator-test"))
}

java {
    withJavadocJar()
    withSourcesJar()
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
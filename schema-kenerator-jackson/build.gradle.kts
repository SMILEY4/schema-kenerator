object Meta {
    const val artifactId = "schema-kenerator-jackson"
}

plugins {
    `maven-publish`
}

dependencies {
    implementation(project(":schema-kenerator-core"))
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.3")

    implementation("io.kotest:kotest-assertions-json:5.8.0")
    testImplementation(project(":schema-kenerator-reflection"))
    testImplementation(project(":schema-kenerator-test"))
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
object Meta {
    const val artifactId = "schema-kenerator-reflection"
}

plugins {
    `maven-publish`
}

dependencies {
    implementation(project(":schema-kenerator-core"))
    implementation(kotlin("reflect"))

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
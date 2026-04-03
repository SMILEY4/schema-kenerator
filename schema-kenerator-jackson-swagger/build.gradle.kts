import io.gitlab.arturbosch.detekt.Detekt

val projectGroupId: String by project
val projectVersion: String by project
group = projectGroupId
version = projectVersion

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.dependencycheck)
    alias(libs.plugins.versions)
    alias(libs.plugins.detekt)
    alias(libs.plugins.dokka)
}

dependencies {
    implementation(project(":schema-kenerator-core"))
    implementation(project(":schema-kenerator-jackson"))
    implementation(project(":schema-kenerator-swagger"))
}

kotlin {
    jvmToolchain(libs.versions.jvm.target.get().toInt())
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

detekt {
    ignoreFailures = false
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom("$rootDir/detekt/detekt.yml")
}
tasks.withType<Detekt>().configureEach {
    reports {
        html.required.set(true)
        md.required.set(true)
        xml.required.set(false)
        txt.required.set(false)
        sarif.required.set(false)
    }
}

dokka {
    dokkaPublications.html {
        outputDirectory = file("$rootDir/docs/dokka/schema-kenerator-jackson-swagger")
    }
}

mavenPublishing {
    val projectGroupId: String by project
    val projectVersion: String by project
    val projectArtifactIdBase: String by project
    val projectNameBase: String by project
    val projectDescriptionBase: String by project
    val projectScmUrl: String by project
    val projectScmConnection: String by project
    val projectLicenseName: String by project
    val projectLicenseUrl: String by project
    val projectDeveloperName: String by project
    val projectDeveloperUrl: String by project

    publishToMavenCentral(automaticRelease = true)
    signAllPublications()
    coordinates(projectGroupId, "$projectArtifactIdBase-jackson-swagger", projectVersion)
    pom {
        name.set("$projectNameBase Jackson Swagger")
        description.set("$projectDescriptionBase - adds support for Jackson specific for swagger-schemas")
        url.set(projectScmUrl)
        licenses {
            license {
                name.set(projectLicenseName)
                url.set(projectLicenseUrl)
                distribution.set(projectLicenseUrl)
            }
        }
        scm {
            url.set(projectScmUrl)
            connection.set(projectScmConnection)
        }
        developers {
            developer {
                id.set(projectDeveloperName)
                name.set(projectDeveloperName)
                url.set(projectDeveloperUrl)
            }
        }
    }
}
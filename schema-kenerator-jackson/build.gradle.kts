import com.vanniktech.maven.publish.SonatypeHost
import io.gitlab.arturbosch.detekt.Detekt

val projectGroupId: String by project
val projectVersion: String by project
group = projectGroupId
version = projectVersion

plugins {
    kotlin("jvm")
    id("org.owasp.dependencycheck")
    id("io.gitlab.arturbosch.detekt")
    id("com.vanniktech.maven.publish")
    id("org.jetbrains.dokka")

}

repositories {
    mavenCentral()
}

dependencies {
    val versionJackson: String by project
    implementation(project(":schema-kenerator-core"))
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$versionJackson")
}

kotlin {
    jvmToolchain(11)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

detekt {
    ignoreFailures = false
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom("$projectDir/../detekt/detekt.yml")
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

    publishToMavenCentral(SonatypeHost.S01)
    signAllPublications()
    coordinates(projectGroupId, "$projectArtifactIdBase-jackson", projectVersion)
    pom {
        name.set("$projectNameBase Jackson")
        description.set("$projectDescriptionBase - adds support for Jackson")
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
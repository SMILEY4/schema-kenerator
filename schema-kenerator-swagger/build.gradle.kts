import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinJvm
import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.dokka.gradle.DokkaTask

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
    api(libs.swagger.parser)
    constraints {
        api("commons-codec:commons-codec:1.13") {
            because("Version 1.11 has a known vulnerability (pulled in via 'io.swagger.parser.v3:swagger-parser').")
        }
    }
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

tasks.withType<DokkaTask>().configureEach {
    outputDirectory.set(file("$rootDir/docs/dokka/schema-kenerator-swagger"))
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

    configure(KotlinJvm(JavadocJar.Dokka("dokkaHtml"), true))
    publishToMavenCentral(automaticRelease = true)
    signAllPublications()
    coordinates(projectGroupId, "$projectArtifactIdBase-swagger", projectVersion)
    pom {
        name.set("$projectNameBase Swagger")
        description.set("$projectDescriptionBase - automatically generates Swagger schemas")
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
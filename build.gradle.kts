import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.dependencycheck) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.maven.publish) apply false
    alias(libs.plugins.versions) apply false
    alias(libs.plugins.mkdocs)
}

subprojects {

    repositories {
        mavenCentral()
    }

    plugins.withId("org.jetbrains.kotlin.jvm") {

        val versionJvmCompile = libs.versions.jvm.compile.get().toInt()
        val versionJvmTarget = libs.versions.jvm.target.get()

        // Kotlin Toolchain
        extensions.configure<KotlinJvmProjectExtension> {
            jvmToolchain(versionJvmCompile)
            compilerOptions {
                jvmTarget.set(JvmTarget.fromTarget(versionJvmTarget))
            }
        }

        // Java Toolchain
        extensions.configure<JavaPluginExtension> {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(versionJvmCompile))
            }
        }

        // JVM Compatibility
        tasks.withType<JavaCompile>().configureEach {
            sourceCompatibility = versionJvmTarget
            targetCompatibility = versionJvmTarget
        }
    }

}

mkdocs {
    sourcesDir = "."
    buildDir = "./build/mkdocs"
    updateSiteUrl = true
    publish {
        branch = "gh-pages"
        version = "2.x"
        rootRedirect = true
        rootRedirectTo = "latest"
        setVersionAliases("latest")
        generateVersionsFile = true
    }
    python {
        minPythonVersion = "3.12"
    }
}

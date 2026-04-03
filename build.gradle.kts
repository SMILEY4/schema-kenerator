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

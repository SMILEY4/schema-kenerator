import okhttp3.internal.platform.android.AndroidLogHandler.publish

plugins {
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.serialization") version "2.0.21" apply false
    id("org.jetbrains.dokka") version "1.9.20" apply false
    id("org.owasp.dependencycheck") version "8.2.1" apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.0" apply false
    id("com.vanniktech.maven.publish") version "0.28.0" apply false
    id("com.github.ben-manes.versions") version "0.51.0" apply false
    id("ru.vyarus.mkdocs") version "4.0.1"
}

mkdocs {
    sourcesDir = "."
    buildDir = "./build/mkdocs"
    updateSiteUrl = true
    publish {
        branch = "gh-pages"
        version = "2.0-test.1"
        rootRedirect = true
        rootRedirectTo = "latest"
        setVersionAliases("latest")
        generateVersionsFile = true
    }
    python {
        minPythonVersion = "3.12"
    }
}

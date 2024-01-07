import io.gitlab.arturbosch.detekt.Detekt

plugins {
    kotlin("jvm") version "1.9.21"
    id("org.owasp.dependencycheck") version "8.2.1"
    id("io.gitlab.arturbosch.detekt") version "1.23.0"
}

subprojects {

    val schemaKeneratorVersion: String by project
    val schemaKeneratorGroupId: String by project
    group = schemaKeneratorGroupId
    version = schemaKeneratorVersion

    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "org.owasp.dependencycheck")

    repositories {
        mavenCentral()
    }

    dependencies {

        val kotlinLoggingVersion = "3.0.5"
        implementation("io.github.microutils:kotlin-logging-jvm:$kotlinLoggingVersion")

        val logbackVersion = "1.4.11"
        testImplementation("ch.qos.logback:logback-classic:$logbackVersion")

        val versionMockk = "1.13.8"
        testImplementation("io.mockk:mockk:$versionMockk")

        val versionKotest = "5.8.0"
        testImplementation("io.kotest:kotest-runner-junit5:$versionKotest")
        testImplementation("io.kotest:kotest-assertions-core:$versionKotest")

        val versionKotlinTest = "1.8.21"
        testImplementation("org.jetbrains.kotlin:kotlin-test:$versionKotlinTest")
    }

    kotlin {
        jvmToolchain(11)
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }

    tasks.withType<Detekt>().configureEach {
        ignoreFailures = true // todo: temporary
        buildUponDefaultConfig = true
        allRules = false
        config.setFrom("$projectDir/../detekt/detekt.yml")
        reports {
            html.required.set(true)
            md.required.set(true)
            xml.required.set(false)
            txt.required.set(false)
            sarif.required.set(false)
        }
    }

}

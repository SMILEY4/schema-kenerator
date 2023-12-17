import io.gitlab.arturbosch.detekt.Detekt

object Meta {
    const val groupId = "io.github.smiley4"
    const val artifactId = "schema-kenerator"
    const val version = "0.1"
}

group = Meta.groupId
version = Meta.version

plugins {
    kotlin("jvm") version "1.8.21"
    id("org.owasp.dependencycheck") version "8.2.1"
    id("io.gitlab.arturbosch.detekt") version "1.23.0"
    kotlin("plugin.serialization") version "1.9.21"
}


repositories {
    mavenCentral()
}

dependencies {

    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

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

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom("$projectDir/config/detekt.yml")
    baseline = file("$projectDir/config/baseline.xml")
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

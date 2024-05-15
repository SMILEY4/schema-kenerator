rootProject.name = "schema-kenerator"

// core
include("schema-kenerator-core")

// parser
include("schema-kenerator-reflection")
include("schema-kenerator-serialization")

// generators
include("schema-kenerator-swagger")
include("schema-kenerator-jsonschema")

// addon-modules
include("schema-kenerator-jackson")

// test
include("schema-kenerator-test")


pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

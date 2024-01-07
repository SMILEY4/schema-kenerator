rootProject.name = "schema-kenerator"
include("schema-kenerator-core")
include("schema-kenerator-reflection")
include("schema-kenerator-serialization")

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}
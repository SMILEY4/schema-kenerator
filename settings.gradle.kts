rootProject.name = "schema-kenerator"

include("schema-kenerator-core")

include("schema-kenerator-reflection")
include("schema-kenerator-serialization")

include("schema-kenerator-swagger")

include("schema-kenerator-test-utils")


pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

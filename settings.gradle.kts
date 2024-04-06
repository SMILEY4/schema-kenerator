rootProject.name = "schema-kenerator"

include("schema-kenerator-core")

include("schema-kenerator-reflection")
include("schema-kenerator-serialization")

include("schema-kenerator-swagger")
include("schema-kenerator-jsonschema")

include("schema-kenerator-test")


pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "schema-kenerator"

// core
include("schema-kenerator-core")


include("schema-kenerator-reflection")
include("schema-kenerator-serialization")

include("schema-kenerator-swagger")
include("schema-kenerator-jsonschema")

include("schema-kenerator-jackson")
include("schema-kenerator-jackson-jsonschema")

include("schema-kenerator-test")


pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

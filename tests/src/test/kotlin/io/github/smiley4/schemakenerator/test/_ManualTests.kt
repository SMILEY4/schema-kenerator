@file:OptIn(ExperimentalSerializationApi::class)
@file:Suppress("ClassName")


package io.github.smiley4.schemakenerator.test

import io.github.smiley4.schemakenerator.core.CoreSteps.addMissingSupertypeSubtypeRelations
import io.github.smiley4.schemakenerator.core.CoreSteps.handleNameAnnotation
import io.github.smiley4.schemakenerator.core.CoreSteps.initial
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaSteps.compileReferencing
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaSteps.generateJsonSchema
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaSteps.handleCoreAnnotations
import io.github.smiley4.schemakenerator.reflection.ReflectionSteps.analyzeTypeUsingReflection
import io.github.smiley4.schemakenerator.reflection.ReflectionSteps.collectSubTypes
import io.github.smiley4.schemakenerator.serialization.SerializationSteps.addJsonClassDiscriminatorProperty
import io.github.smiley4.schemakenerator.serialization.SerializationSteps.analyzeTypeUsingKotlinxSerialization
import io.github.smiley4.schemakenerator.serialization.SerializationSteps.convertToKotlinxSerializationTypes
import io.github.smiley4.schemakenerator.swagger.SwaggerSteps.compileInlining
import io.github.smiley4.schemakenerator.swagger.SwaggerSteps.compileReferencing
import io.github.smiley4.schemakenerator.swagger.SwaggerSteps.compileReferencingRoot
import io.github.smiley4.schemakenerator.swagger.SwaggerSteps.generateSwaggerSchema
import io.github.smiley4.schemakenerator.swagger.SwaggerSteps.handleCoreAnnotations
import io.github.smiley4.schemakenerator.swagger.SwaggerSteps.handleSchemaAnnotations
import io.github.smiley4.schemakenerator.swagger.SwaggerSteps.mergePropertyAttributesIntoType
import io.github.smiley4.schemakenerator.swagger.SwaggerSteps.withTitle
import io.github.smiley4.schemakenerator.swagger.data.CompiledSwaggerSchemaData
import io.github.smiley4.schemakenerator.swagger.data.RefType
import io.github.smiley4.schemakenerator.swagger.data.TitleType
import io.kotest.core.spec.style.StringSpec
import io.swagger.v3.core.util.Json31
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Paths
import io.swagger.v3.oas.models.SpecVersion
import io.swagger.v3.oas.models.info.Info
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlin.time.Duration


/**
 * internal / manual tests only
 */
class _ManualTests : StringSpec({

    "reflection referencing" {
        val schema = initial<MembershipTypeCredits>()
            .collectSubTypes()
            .analyzeTypeUsingReflection()
            .addMissingSupertypeSubtypeRelations()
            .handleNameAnnotation()
            .generateSwaggerSchema()
            .handleCoreAnnotations()
            .withTitle(TitleType.SIMPLE)
            .handleSchemaAnnotations()
            .mergePropertyAttributesIntoType()
            .compileReferencingRoot(pathType = RefType.OPENAPI_SIMPLE)

        println(schema.asOpenApiJson())
    }

    "reflection inlining" {
        val schema = initial<MembershipTypeCredits>()
            .collectSubTypes()
            .analyzeTypeUsingReflection()
            .addMissingSupertypeSubtypeRelations()
            .handleNameAnnotation()
            .generateSwaggerSchema()
            .handleCoreAnnotations()
            .withTitle(TitleType.SIMPLE)
            .handleSchemaAnnotations()
            .mergePropertyAttributesIntoType()
            .compileInlining()

        println(schema.asOpenApiJson())
    }

    "kotlinx" {
        val schema = initial<TestClass>()
            .analyzeTypeUsingKotlinxSerialization()
            .addJsonClassDiscriminatorProperty()
            .handleNameAnnotation()
            .generateJsonSchema()
            .handleCoreAnnotations()
            .compileReferencing(definitionsPath = "definitions")
            .convertToKotlinxSerializationTypes()
        println(schema)
    }

    "test" {
        val schema = initial<JsonArray>()
            .analyzeTypeUsingKotlinxSerialization()
//            .analyzeTypeUsingReflection()
            .generateSwaggerSchema()
            .compileReferencing()
            .asOpenApiJson()
        println(schema)
    }

}) {
    companion object {

        @Serializable
        abstract class ClassWithGenericListSuperclass: List<String>


        @Serializable
        data class TestClass(
            val someData: JsonElement,
        )

        @Serializable
        data class MembershipTypeCredits(
            val amount: Long,
            val duration: Duration,
            val user: User
        )


        @Serializable
        data class User(
            val name: String,
            val joinedTimestamp: Long,
        )


        class SwaggerResult(
            val root: io.swagger.v3.oas.models.media.Schema<*>,
            val componentSchemas: Map<String, io.swagger.v3.oas.models.media.Schema<*>>
        )

        fun CompiledSwaggerSchemaData.asPrintable(): SwaggerResult {
            return SwaggerResult(
                root = this.swagger,
                componentSchemas = this.componentSchemas
            )
        }


        fun CompiledSwaggerSchemaData.asOpenApiJson(): String {
            val openApi = OpenAPI().also { openAPI ->
                openAPI.specVersion = SpecVersion.V31
                openAPI.openapi = "3.1.0"
                openAPI.info = Info().also { info ->
                    info.title = "Test"
                    info.version = "0.0"
                }
                openAPI.paths = Paths()
                openAPI.components = Components().also { components ->
                    components.schemas = buildMap {
                        this["root"] = this@asOpenApiJson.swagger
                        this@asOpenApiJson.componentSchemas.forEach { (name, schema) ->
                            this[name] = schema
                        }
                    }
                }
            }

            return Json31.pretty(openApi)
        }

    }
}
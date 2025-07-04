@file:OptIn(ExperimentalSerializationApi::class)
@file:Suppress("ClassName")


package io.github.smiley4.schemakenerator.test

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.smiley4.schemakenerator.core.CoreSteps.initial
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaSteps.compileReferencing
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaSteps.generateJsonSchema
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaSteps.merge
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaSteps.withTitle
import io.github.smiley4.schemakenerator.jsonschema.data.RefType
import io.github.smiley4.schemakenerator.jsonschema.data.TitleType
import io.github.smiley4.schemakenerator.reflection.ReflectionSteps.analyzeTypeUsingReflection
import io.github.smiley4.schemakenerator.swagger.data.CompiledSwaggerSchemaData
import io.kotest.core.spec.style.StringSpec
import io.swagger.v3.core.util.Json31
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Paths
import io.swagger.v3.oas.models.info.Info
import kotlinx.serialization.ExperimentalSerializationApi

/**
 * internal / manual tests only
 */
class _ManualTests : StringSpec({

    "reflection" {
        val schema = initial<ParentClass>()
            .analyzeTypeUsingReflection()
            .generateJsonSchema()
            .withTitle(TitleType.SIMPLE)
            .compileReferencing(pathType = RefType.SIMPLE)
            .merge()

        println(schema)
    }

//    "reflection" {
//        val schema = initial<ParentClass>()
//            .collectSubTypes()
//            .analyzeTypeUsingReflection()
//            .addMissingSupertypeSubtypeRelations()
//            .handleNameAnnotation()
//            .generateSwaggerSchema()
//            .handleCoreAnnotations()
//            .handleSchemaAnnotations()
//            .mergePropertyAttributesIntoType()
//            .compileReferencingRoot(pathType = RefType.OPENAPI_SIMPLE)
//
//            // println(json.writeValueAsString(schema.asPrintable()))
//            println(schema.asOpenApiJson())
//    }
//
//    "kotlinx" {
//        val schema = initial<ParentClass>()
//            .analyzeTypeUsingKotlinxSerialization()
//            .addJsonClassDiscriminatorProperty()
//            .handleNameAnnotation()
//            .generateSwaggerSchema()
//            .handleCoreAnnotations()
//            .handleSchemaAnnotations()
//            .mergePropertyAttributesIntoType()
//            .compileReferencingRoot()
//            .asPrintable()
//        println(json.writeValueAsString(schema))
//    }

}) {
    companion object {

        class ParentClass(
            val child: ChildClass
        )

        class ChildClass

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

        private val json = jacksonObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL).writerWithDefaultPrettyPrinter()!!

    }
}
@file:OptIn(ExperimentalSerializationApi::class)
@file:Suppress("ClassName")


package io.github.smiley4.schemakenerator.test

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.smiley4.schemakenerator.core.CoreSteps.addMissingSupertypeSubtypeRelations
import io.github.smiley4.schemakenerator.core.CoreSteps.handleNameAnnotation
import io.github.smiley4.schemakenerator.core.CoreSteps.initial
import io.github.smiley4.schemakenerator.core.annotations.ExclusiveMax
import io.github.smiley4.schemakenerator.core.annotations.MaxLength
import io.github.smiley4.schemakenerator.core.annotations.Min
import io.github.smiley4.schemakenerator.core.annotations.MinLength
import io.github.smiley4.schemakenerator.core.data.AnnotationData
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeDataGroup
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.core.data.TypeName
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaSteps.compileInlining
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaSteps.generateJsonSchema
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaSteps.withTitle
import io.github.smiley4.schemakenerator.jsonschema.data.TitleType
import io.github.smiley4.schemakenerator.reflection.ReflectionSteps.analyzeTypeUsingReflection
import io.github.smiley4.schemakenerator.reflection.ReflectionSteps.collectSubTypes
import io.github.smiley4.schemakenerator.serialization.SerializationSteps.addJsonClassDiscriminatorProperty
import io.github.smiley4.schemakenerator.serialization.SerializationSteps.analyzeTypeUsingKotlinxSerialization
import io.github.smiley4.schemakenerator.swagger.SwaggerSteps.compileReferencingRoot
import io.github.smiley4.schemakenerator.swagger.SwaggerSteps.generateSwaggerSchema
import io.github.smiley4.schemakenerator.swagger.SwaggerSteps.handleCoreAnnotations
import io.github.smiley4.schemakenerator.swagger.SwaggerSteps.handleSchemaAnnotations
import io.github.smiley4.schemakenerator.swagger.SwaggerSteps.mergePropertyAttributesIntoType
import io.github.smiley4.schemakenerator.swagger.data.CompiledSwaggerSchemaData
import io.github.smiley4.schemakenerator.swagger.data.RefType
import io.kotest.core.spec.style.StringSpec
import io.swagger.v3.core.util.Json31
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Paths
import io.swagger.v3.oas.models.info.Info
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlin.reflect.full.starProjectedType


/**
 * internal / manual tests only
 */
class _ManualTests : StringSpec({

    "test case" {
        /*
            A
            ├── B (d)
            └── C
                ├── D
                └── E
        */
        val idA = TypeId.create()
        val idB = TypeId.create()
        val idC = TypeId.create()
        val idD = TypeId.create()
        val idE = TypeId.create()

        fun buildTypeData(id: TypeId, name: String, subtypes: List<TypeId>) = TypeData(
            id = id,
            identifyingName = TypeName(
                full = name,
                short = name
            ),
            descriptiveName = TypeName(
                full = name,
                short = name
            ),
            subtypes = subtypes.toMutableList(),
            typeParameters = mutableListOf(),
            annotations = mutableListOf(
                AnnotationData(
                    name = JsonClassDiscriminator::class.qualifiedName!!,
                    values = mutableMapOf("discriminator" to "type")
                )
            ),
            supertypes = mutableListOf(),
            members = mutableListOf(),
            isInlineValue = false,
            enumData = null,
            collectionData = null,
            mapData = null,
        )

        val data = TypeDataGroup(
            rootId = idA,
            data = mapOf(
                idA to buildTypeData(idA, "A", listOf(idB, idC)),
                idB to buildTypeData(idB, "B", listOf()),
                idC to buildTypeData(idC, "C", listOf(idD, idE)),
                idD to buildTypeData(idD, "D", listOf()),
                idE to buildTypeData(idE, "E", listOf()),
            )
        )
            .addMissingSupertypeSubtypeRelations()
            .addJsonClassDiscriminatorProperty()
            .generateJsonSchema()
            .withTitle(TitleType.FULL)
            .compileInlining()
        println(data.json.prettyPrint())
    }

    "test #60" {
        val data = initial(TestSchema::class.starProjectedType)
            .analyzeTypeUsingKotlinxSerialization()
            .addJsonClassDiscriminatorProperty()
            .addMissingSupertypeSubtypeRelations()
            .generateJsonSchema()
            .withTitle(TitleType.FULL)
            .compileInlining()
        println(data.json.prettyPrint())
    }

    "reflection" {
        val schema = initial<ParentClass<ChildClass>>()
            .collectSubTypes()
            .analyzeTypeUsingReflection()
            .addMissingSupertypeSubtypeRelations()
            .handleNameAnnotation()
            .generateSwaggerSchema()
            .handleCoreAnnotations()
            .handleSchemaAnnotations()
            .mergePropertyAttributesIntoType()
            .compileReferencingRoot(pathType = RefType.OPENAPI_SIMPLE)

        // println(json.writeValueAsString(schema.asPrintable()))
        println(schema.asOpenApiJson())
    }

    "kotlinx" {
        val schema = initial<RootClass>()
            .analyzeTypeUsingKotlinxSerialization()
            .addJsonClassDiscriminatorProperty()
            .handleNameAnnotation()
            .generateSwaggerSchema()
            .handleCoreAnnotations()
            .handleSchemaAnnotations()
            .mergePropertyAttributesIntoType()
            .compileReferencingRoot(pathType = RefType.OPENAPI_SIMPLE)
            .asPrintable()
        println(json.writeValueAsString(schema))
    }

}) {
    companion object {

        @Serializable
        @JsonClassDiscriminator("type")
        sealed class TestSchema {
            @Serializable
            @SerialName("a")
            object A : TestSchema()


            @Serializable
            @SerialName("b")
            object B : TestSchema()
        }


        @Serializable
        class TestClass(

            @MinLength(3)
            @MaxLength(10)
            val myText: String,

            @Min(2)
            @ExclusiveMax(5)
            val myNumber: Int,

            @MinLength(1)
            @MaxLength(9)
            val myList: List<Boolean>
        )


        @Serializable
        class RootClass(
            val parent1: ParentClass<String>,
            val parent2: ParentClass<Int>
        )


        @Serializable
        class ParentClass<T>(
            val child: T
        )


        @Serializable
        class ChildClass(val value: String)

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
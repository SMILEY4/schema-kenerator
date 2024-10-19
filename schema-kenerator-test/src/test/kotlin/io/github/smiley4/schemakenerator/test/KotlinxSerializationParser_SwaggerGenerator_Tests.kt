package io.github.smiley4.schemakenerator.test

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.smiley4.schemakenerator.serialization.steps.KotlinxSerializationTypeProcessingStep
import io.github.smiley4.schemakenerator.swagger.OptionalHandling
import io.github.smiley4.schemakenerator.swagger.SwaggerSchemaGenerationStepConfig
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCompileInlineStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCompileReferenceRootStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCompileReferenceStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCoreAnnotationDefaultStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCoreAnnotationDeprecatedStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCoreAnnotationDescriptionStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCoreAnnotationExamplesStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCoreAnnotationFormatStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCoreAnnotationTitleStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaGenerationStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaTitleStep
import io.github.smiley4.schemakenerator.swagger.steps.TitleBuilder
import io.github.smiley4.schemakenerator.test.models.kotlinx.ClassDirectSelfReferencing
import io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithCollections
import io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithDeepGeneric
import io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithGenericField
import io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithNestedClass
import io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithOptionalParameters
import io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithSimpleFields
import io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithValueClass
import io.github.smiley4.schemakenerator.test.models.kotlinx.CoreAnnotatedClass
import io.github.smiley4.schemakenerator.test.models.kotlinx.SealedClass
import io.github.smiley4.schemakenerator.test.models.kotlinx.SubClassA
import io.github.smiley4.schemakenerator.test.models.kotlinx.TestEnum
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.WithDataTestName
import io.kotest.datatest.withData
import io.swagger.v3.oas.models.media.Schema
import kotlin.reflect.KType
import kotlin.reflect.typeOf

@Suppress("ClassName")
class KotlinxSerializationParser_SwaggerGenerator_Tests : FunSpec({

    context("generator: inlining") {
        withData(TEST_DATA) { data ->

            val schema = data.type
                .let { KotlinxSerializationTypeProcessingStep().process(it) }
                .let {
                    SwaggerSchemaGenerationStep(
                        optionalAsNonRequired = SwaggerSchemaGenerationStepConfig().apply(data.generatorConfig).optionalHandling == OptionalHandling.NON_REQUIRED
                    ).generate(it)
                }
                .let { list ->
                    if (data.withAnnotations) {
                        list
                            .let { SwaggerSchemaCoreAnnotationTitleStep().process(it) }
                            .let { SwaggerSchemaCoreAnnotationDescriptionStep().process(it) }
                            .let { SwaggerSchemaCoreAnnotationDefaultStep().process(it) }
                            .let { SwaggerSchemaCoreAnnotationExamplesStep().process(it) }
                            .let { SwaggerSchemaCoreAnnotationDeprecatedStep().process(it) }
                            .let { SwaggerSchemaCoreAnnotationFormatStep().process(it) }
                    } else {
                        list
                    }
                }
                .let { list ->
                    if (data.withAutoTitle) {
                        list
                            .let { SwaggerSchemaTitleStep(TitleBuilder.BUILDER_SIMPLE).process(it) }
                    } else {
                        list
                    }
                }
                .let { SwaggerSchemaCompileInlineStep().compile(it) }


            schema.swagger.shouldEqualJson(data.expectedResultInlining)
        }
    }

    context("generator: referencing") {
        withData(TEST_DATA) { data ->

            val schema = data.type
                .let { KotlinxSerializationTypeProcessingStep().process(it) }
                .let {
                    SwaggerSchemaGenerationStep(
                        optionalAsNonRequired = SwaggerSchemaGenerationStepConfig().apply(data.generatorConfig).optionalHandling == OptionalHandling.NON_REQUIRED
                    ).generate(it)
                }
                .let { list ->
                    if (data.withAnnotations) {
                        list
                            .let { SwaggerSchemaCoreAnnotationTitleStep().process(it) }
                            .let { SwaggerSchemaCoreAnnotationDescriptionStep().process(it) }
                            .let { SwaggerSchemaCoreAnnotationDefaultStep().process(it) }
                            .let { SwaggerSchemaCoreAnnotationExamplesStep().process(it) }
                            .let { SwaggerSchemaCoreAnnotationDeprecatedStep().process(it) }
                            .let { SwaggerSchemaCoreAnnotationFormatStep().process(it) }
                    } else {
                        list
                    }
                }
                .let { list ->
                    if (data.withAutoTitle) {
                        list
                            .let { SwaggerSchemaTitleStep(TitleBuilder.BUILDER_SIMPLE).process(it) }
                    } else {
                        list
                    }
                }
                .let { SwaggerSchemaCompileReferenceStep(TitleBuilder.BUILDER_FULL).compile(it) }
                .let {
                    Result(
                        schema = it.swagger,
                        definitions = it.componentSchemas
                    )
                }

            (schema.schema to schema.definitions).shouldEqualJson(data.expectedResultReferencing)
        }
    }

    context("generator: referencing-root") {
        withData(TEST_DATA) { data ->

            val schema = data.type
                .let { KotlinxSerializationTypeProcessingStep().process(it) }
                .let {
                    SwaggerSchemaGenerationStep(
                        optionalAsNonRequired = SwaggerSchemaGenerationStepConfig().apply(data.generatorConfig).optionalHandling == OptionalHandling.NON_REQUIRED
                    ).generate(it)
                }
                .let { list ->
                    if (data.withAnnotations) {
                        list
                            .let { SwaggerSchemaCoreAnnotationTitleStep().process(it) }
                            .let { SwaggerSchemaCoreAnnotationDescriptionStep().process(it) }
                            .let { SwaggerSchemaCoreAnnotationDefaultStep().process(it) }
                            .let { SwaggerSchemaCoreAnnotationExamplesStep().process(it) }
                            .let { SwaggerSchemaCoreAnnotationDeprecatedStep().process(it) }
                            .let { SwaggerSchemaCoreAnnotationFormatStep().process(it) }
                    } else {
                        list
                    }
                }
                .let { list ->
                    if (data.withAutoTitle) {
                        list
                            .let { SwaggerSchemaTitleStep(TitleBuilder.BUILDER_SIMPLE).process(it) }
                    } else {
                        list
                    }
                }
                .let { SwaggerSchemaCompileReferenceRootStep(TitleBuilder.BUILDER_FULL).compile(it) }
                .let {
                    Result(
                        schema = it.swagger,
                        definitions = it.componentSchemas
                    )
                }

            (schema.schema to schema.definitions).shouldEqualJson(data.expectedResultReferencingRoot)
        }
    }

}) {

    companion object {

        private val json = jacksonObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL).writerWithDefaultPrettyPrinter()!!

        private data class Result(
            val schema: Schema<*>,
            val definitions: Map<String, Schema<*>>
        )

        private class TestData(
            val testName: String,
            val type: KType,
            val generatorConfig: SwaggerSchemaGenerationStepConfig.() -> Unit = {},
            val withAnnotations: Boolean = false,
            val withAutoTitle: Boolean = false,
            val expectedResultInlining: String,
            val expectedResultReferencing: Map<String, String>,
            val expectedResultReferencingRoot: Map<String, String>,
        ) : WithDataTestName {
            override fun dataTestName() = testName
        }

        private val TEST_DATA = listOf(
            TestData(
                type = typeOf<Any>(),
                testName = "any",
                expectedResultInlining = """
                    {
                        "type": "object"
                    }
                """.trimIndent(),
                expectedResultReferencing = mapOf(
                    "." to """
                        {
                            "type": "object"
                        }
                    """.trimIndent()
                ),
                expectedResultReferencingRoot = mapOf(
                    "." to """
                        {
                            "type": "object"
                        }
                    """.trimIndent()
                ),
            ),
            TestData(
                type = typeOf<UByte>(),
                testName = "ubyte",
                expectedResultInlining = """
                    {
                        "type": "integer",
                        "maximum": 255,
                        "minimum": 0
                    }
                """.trimIndent(),
                expectedResultReferencing = mapOf(
                    "." to """
                        {
                            "type": "integer",
                            "maximum": 255,
                            "minimum": 0
                        }
                    """.trimIndent()
                ),
                expectedResultReferencingRoot = mapOf(
                    "." to """
                        {
                            "type": "integer",
                            "maximum": 255,
                            "minimum": 0
                        }
                    """.trimIndent()
                ),
            ),
            TestData(
                type = typeOf<Int>(),
                testName = "int",
                expectedResultInlining = """
                    {
                        "type": "integer",
                        "format": "int32"
                    }
                """.trimIndent(),
                expectedResultReferencing = mapOf(
                    "." to """
                        {
                            "type": "integer",
                            "format": "int32"
                        }
                    """.trimIndent()
                ),
                expectedResultReferencingRoot = mapOf(
                    "." to """
                        {
                            "type": "integer",
                            "format": "int32"
                        }
                    """.trimIndent()
                ),
            ),
            TestData(
                type = typeOf<Float>(),
                testName = "float",
                expectedResultInlining = """
                    {
                        "type": "number",
                        "format": "float"
                    }
                """.trimIndent(),
                expectedResultReferencing = mapOf(
                    "." to """
                        {
                            "type": "number",
                            "format": "float"
                        }
                    """.trimIndent()
                ),
                expectedResultReferencingRoot = mapOf(
                    "." to """
                        {
                            "type": "number",
                            "format": "float"
                        }
                    """.trimIndent()
                )
            ),
            TestData(
                type = typeOf<Boolean>(),
                testName = "boolean",
                expectedResultInlining = """
                    {
                         "type": "boolean"
                    }
                """.trimIndent(),
                expectedResultReferencing = mapOf(
                    "." to """
                        {
                             "type": "boolean"
                        }
                    """.trimIndent()
                ),
                expectedResultReferencingRoot = mapOf(
                    "." to """
                        {
                             "type": "boolean"
                        }
                    """.trimIndent()
                )
            ),
            TestData(
                type = typeOf<String>(),
                testName = "string",
                expectedResultInlining = """
                    {
                        "type": "string"
                    }
                """.trimIndent(),
                expectedResultReferencing = mapOf(
                    "." to """
                        {
                            "type": "string"
                        }
                    """.trimIndent()
                ),
                expectedResultReferencingRoot = mapOf(
                    "." to """
                        {
                            "type": "string"
                        }
                    """.trimIndent()
                )
            ),
            TestData(
                // top-level lists not directly supported: weird result
                type = typeOf<List<String>>(),
                testName = "list of strings",
                expectedResultInlining = """
                    {
                        "type": "object"
                    }
                """.trimIndent(),
                expectedResultReferencing = mapOf(
                    "." to """
                        {
                            "type": "object"
                        }
                    """.trimIndent()
                ),
                expectedResultReferencingRoot = mapOf(
                    "." to """
                        {
                          "type": "object"
                        }
                    """.trimIndent(),
                )
            ),
            // top-level maps not directly supported: weird result
            TestData(
                type = typeOf<Map<String, Int>>(),
                testName = "map of strings to integers",
                expectedResultInlining = """
                    {
                        "type": "object"
                    }
                """.trimIndent(),
                expectedResultReferencing = mapOf(
                    "." to """
                        {
                            "type": "object"
                        }
                    """.trimIndent()
                ),
                expectedResultReferencingRoot = mapOf(
                    "." to """
                        {
                            "type": "object"
                        }
                    """.trimIndent()
                )
            ),
            TestData(
                type = typeOf<ClassWithSimpleFields>(),
                testName = "class with simple fields",
                expectedResultInlining = """
                    {
                        "type": "object",
                        "required": [
                            "someBoolList",
                            "someString"
                        ],
                        "properties": {
                            "someBoolList": {
                                "type": "array",
                                "items": {
                                  "type": "boolean"
                                }
                            },
                            "someNullableInt": {
                                "type": ["integer", "null"],
                                "format": "int32"
                            },
                            "someString": {
                                "type": "string"
                            }
                        }
                    }
                """.trimIndent(),
                expectedResultReferencing = mapOf(
                    "." to """
                        {
                            "required": [
                                "someBoolList",
                                "someString"
                            ],
                            "type": "object",
                            "properties": {
                                "someBoolList": {
                                    "type": "array",
                                    "items": {
                                      "type": "boolean"
                                    }
                                },
                                "someNullableInt": {
                                    "type": ["integer", "null"],
                                    "format": "int32"
                                },
                                "someString": {
                                    "type": "string"
                                }
                            }
                        }
                    """.trimIndent()
                ),
                expectedResultReferencingRoot = mapOf(
                    "." to """
                        {
                            "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithSimpleFields"
                        }
                    """.trimIndent(),
                    "io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithSimpleFields" to """
                        {
                            "type": "object",
                            "required": [
                                "someBoolList",
                                "someString"
                            ],
                            "properties": {
                                "someBoolList": {
                                    "type": "array",
                                    "items": {
                                      "type": "boolean"
                                    }
                                },
                                "someNullableInt": {
                                    "type": ["integer", "null"],
                                    "format": "int32"
                                },
                                "someString": {
                                    "type": "string"
                                }
                            }
                        }
                    """.trimIndent()
                )
            ),
            TestData(
                type = typeOf<TestEnum>(),
                testName = "enum",
                expectedResultInlining = """
                    {
                        "type": "string",
                        "enum": [ "ONE", "TWO", "THREE" ]
                    }
                """.trimIndent(),
                expectedResultReferencing = mapOf(
                    "." to """
                        {
                            "type": "string",
                            "enum": [ "ONE", "TWO", "THREE" ]
                        }
                    """.trimIndent()
                ),
                expectedResultReferencingRoot = mapOf(
                    "." to """
                        {
                            "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.kotlinx.TestEnum"
                        }
                    """.trimIndent(),
                    "io.github.smiley4.schemakenerator.test.models.kotlinx.TestEnum" to """
                        {
                            "type": "string",
                            "enum": [ "ONE", "TWO", "THREE" ]
                        }
                    """.trimIndent()
                )
            ),
            TestData(
                // generics not supported with kotlinx-serialization -> fallback to "any"-schema
                type = typeOf<ClassWithGenericField<String>>(),
                testName = "class with defined generic field",
                expectedResultInlining = """
                    {
                        "type": "object"
                    }
                """.trimIndent(),
                expectedResultReferencing = mapOf(
                    "." to """
                        {
                            "type": "object"
                        }
                    """.trimIndent()
                ),
                expectedResultReferencingRoot = mapOf(
                    "." to """
                        {
                            "type": "object"
                        }
                    """.trimIndent()
                )
            ),
            TestData(
                // generics not supported with kotlinx-serialization -> fallback to "any"-schema
                type = typeOf<ClassWithGenericField<*>>(),
                testName = "class with wildcard generic field",
                expectedResultInlining = """
                    {
                        "type": "object"
                    }
                """.trimIndent(),
                expectedResultReferencing = mapOf(
                    "." to """
                        {
                            "type": "object"
                        }
                    """.trimIndent()
                ),
                expectedResultReferencingRoot = mapOf(
                    "." to """
                        {
                            "type": "object"
                        }
                    """.trimIndent()
                )
            ),
            TestData(
                // generics not supported with kotlinx-serialization -> fallback to "any"-schema
                type = typeOf<ClassWithDeepGeneric<String>>(),
                testName = "class with deep generic field",
                expectedResultInlining = """
                    {
                        "type": "object"
                    }
                """.trimIndent(),
                expectedResultReferencing = mapOf(
                    "." to """
                        {
                            "type": "object"
                        }
                    """.trimIndent()
                ),
                expectedResultReferencingRoot = mapOf(
                    "." to """
                        {
                            "type": "object"
                        }
                    """.trimIndent()
                )
            ),
            TestData(
                type = typeOf<SealedClass>(),
                testName = "sealed class with subtypes",
                expectedResultInlining = """
                    {
                        "anyOf": [
                            {
                                "required": [
                                    "a",
                                    "sealedValue"
                                ],
                                "type": "object",
                                "properties": {
                                    "a": {
                                      "type": "integer",
                                      "format": "int32"
                                    },
                                    "sealedValue": {
                                        "type": "string"
                                    }
                                }
                            },
                            {
                                "required": [
                                    "b",
                                    "sealedValue"
                                ],
                                "type": "object",
                                "properties": {
                                    "b": {
                                        "type": "integer",
                                        "format": "int32"
                                    },
                                    "sealedValue": {
                                        "type": "string"
                                    }
                                }
                            }
                        ]
                    }
                """.trimIndent(),
                expectedResultReferencing = mapOf(
                    "." to """
                        {
                            "anyOf": [
                                {
                                    "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.kotlinx.SubClassA"
                                },
                                {
                                    "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.kotlinx.SubClassB"
                                }
                            ]
                        }
                    """.trimIndent(),
                    "io.github.smiley4.schemakenerator.test.models.kotlinx.SubClassA" to """
                        {
                            "type": "object",
                            "required": [
                                "a",
                                "sealedValue"
                            ],
                            "properties": {
                                "a": {
                                    "type": "integer",
                                    "format": "int32"
                                },
                                "sealedValue": {
                                    "type": "string"
                                }
                            }
                        }
                    """.trimIndent(),
                    "io.github.smiley4.schemakenerator.test.models.kotlinx.SubClassB" to """
                        {
                            "type": "object",
                            "required": [
                                "b",
                                "sealedValue"
                            ],
                            "properties": {
                                "b": {
                                    "type": "integer",
                                    "format": "int32"
                                },
                                "sealedValue": {
                                    "type": "string"
                                }
                            }
                        }
                    """.trimIndent()
                ),
                expectedResultReferencingRoot = mapOf(
                    "." to """
                        {
                            "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.kotlinx.SealedClass"
                        }
                    """.trimIndent(),
                    "io.github.smiley4.schemakenerator.test.models.kotlinx.SealedClass" to """
                        {
                            "anyOf": [
                                {
                                    "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.kotlinx.SubClassA"
                                },
                                {
                                    "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.kotlinx.SubClassB"
                                }
                            ]
                        }
                    """.trimIndent(),
                    "io.github.smiley4.schemakenerator.test.models.kotlinx.SubClassA" to """
                        {
                            "type": "object",
                            "required": [
                                "a",
                                "sealedValue"
                            ],
                            "properties": {
                                "a": {
                                    "type": "integer",
                                    "format": "int32"
                                },
                                "sealedValue": {
                                    "type": "string"
                                }
                            }
                        }
                    """.trimIndent(),
                    "io.github.smiley4.schemakenerator.test.models.kotlinx.SubClassB" to """
                        {
                            "type": "object",
                            "required": [
                                "b",
                                "sealedValue"
                            ],
                            "properties": {
                                "b": {
                                    "type": "integer",
                                    "format": "int32"
                                },
                                "sealedValue": {
                                    "type": "string"
                                }
                            }
                        }
                    """.trimIndent()
                )
            ),
            TestData(
                type = typeOf<SubClassA>(),
                testName = "sub class",
                expectedResultInlining = """
                    {
                        "type": "object",
                        "required": [
                            "a",
                            "sealedValue"
                        ],
                        "properties": {
                            "a": {
                                "type": "integer",
                                "format": "int32"
                            },
                            "sealedValue": {
                                "type": "string"
                            }
                        }
                    }
                """.trimIndent(),
                expectedResultReferencing = mapOf(
                    "." to """
                        {
                            "type": "object",
                            "required": [
                                "a",
                                "sealedValue"
                            ],
                            "properties": {
                                "a": {
                                    "type": "integer",
                                    "format": "int32"
                                },
                                "sealedValue": {
                                    "type": "string"
                                }
                            }
                        }
                    """.trimIndent()
                ),
                expectedResultReferencingRoot = mapOf(
                    "." to """
                        {
                            "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.kotlinx.SubClassA"
                        }
                    """.trimIndent(),
                    "io.github.smiley4.schemakenerator.test.models.kotlinx.SubClassA" to """
                        {
                            "type": "object",
                            "required": [
                                "a",
                                "sealedValue"
                            ],
                            "properties": {
                                "a": {
                                    "type": "integer",
                                    "format": "int32"
                                },
                                "sealedValue": {
                                    "type": "string"
                                }
                            }
                        }
                    """.trimIndent()
                )
            ),
            TestData(
                type = typeOf<CoreAnnotatedClass>(),
                testName = "annotated class (core)",
                withAnnotations = true,
                expectedResultInlining = """
                    {
                        "title": "Annotated Class",
                        "type": "object",
                        "required": [
                            "value"
                        ],
                        "properties": {
                            "value": {
                                "type": "string",
                                "description": "field description",
                                "format": "string"
                            }
                        },
                        "description": "some description",
                        "deprecated": true,
                        "example": "example 1",
                        "default": "default value",
                        "format": "object"
                    }
                """.trimIndent(),
                expectedResultReferencing = mapOf(
                    "." to """
                        {
                            "title": "Annotated Class",
                            "type": "object",
                            "required": [
                                "value"
                            ],
                            "properties": {
                                "value": {
                                    "type": "string",
                                    "description": "field description",
                                    "format": "string"
                                }
                            },
                            "description": "some description",
                            "deprecated": true,
                            "example": "example 1",
                            "default": "default value",
                            "format": "object"
                        }
                    """.trimIndent()
                ),
                expectedResultReferencingRoot = mapOf(
                    "." to """
                        {
                            "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.kotlinx.CoreAnnotatedClass"
                        }
                    """.trimIndent(),
                    "io.github.smiley4.schemakenerator.test.models.kotlinx.CoreAnnotatedClass" to """
                        {
                            "title": "Annotated Class",
                            "type": "object",
                            "required": [
                                "value"
                            ],
                            "properties": {
                                "value": {
                                    "type": "string",
                                    "description": "field description",
                                    "format": "string"
                                }
                            },
                            "description": "some description",
                            "deprecated": true,
                            "example": "example 1",
                            "default": "default value",
                            "format": "object"
                        }
                    """.trimIndent()
                )
            ),
            TestData(
                type = typeOf<ClassWithNestedClass>(),
                testName = "auto title",
                withAutoTitle = true,
                expectedResultInlining = """
                    {
                        "title": "ClassWithNestedClass",
                        "required": [
                            "nested"
                        ],
                        "type": "object",
                        "properties": {
                            "nested": {
                                "title": "NestedClass",
                                "type": "object",
                                "required": [
                                    "text"
                                ],
                                "properties": {
                                    "text": {
                                        "title": "String",
                                        "type": "string"
                                    }
                                }
                            }
                        }
                    }
                """.trimIndent(),
                expectedResultReferencing = mapOf(
                    "." to """
                        {
                            "title": "ClassWithNestedClass",
                            "required": [
                                "nested"
                            ],
                            "type": "object",
                            "properties": {
                                "nested": {
                                    "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.kotlinx.NestedClass"
                                }
                            }
                        }
                    """.trimIndent(),
                    "io.github.smiley4.schemakenerator.test.models.kotlinx.NestedClass" to """
                        {
                            "title": "NestedClass",
                            "required": [
                                "text"
                            ],
                            "type": "object",
                            "properties": {
                                "text": {
                                    "title": "String",
                                    "type": "string"
                                }
                            }
                        }
                    """.trimIndent()
                ),
                expectedResultReferencingRoot = mapOf(
                    "." to """
                        {
                            "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithNestedClass"
                        }
                    """.trimIndent(),
                    "io.github.smiley4.schemakenerator.test.models.kotlinx.NestedClass" to """
                        {
                            "title": "NestedClass",
                            "required": [
                                "text"
                            ],
                            "type": "object",
                            "properties": {
                                "text": {
                                    "title": "String",
                                    "type": "string"
                                }
                            }
                        }
                    """.trimIndent(),
                    "io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithNestedClass" to """
                        {
                            "title": "ClassWithNestedClass",
                            "required": [
                                "nested"
                            ],
                            "type": "object",
                            "properties": {
                                "nested": {
                                    "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.kotlinx.NestedClass"
                                }
                            }
                        }
                    """.trimIndent()
                )
            ),
            TestData(
                type = typeOf<ClassWithCollections>(),
                testName = "class with collections",
                expectedResultInlining = """
                    {
                      "required": [
                        "someArray",
                        "someList",
                        "someMap",
                        "someSet"
                      ],
                      "type": "object",
                      "properties": {
                        "someList": {
                          "type": "array",
                          "items": {
                            "type": "string"
                          }
                        },
                        "someSet": {
                          "type": "array",
                          "items": {
                            "type": "string"
                          }
                        },
                        "someMap": {
                          "type": "object",
                          "additionalProperties": {
                            "type": "integer",
                            "format": "int32"
                          }
                        },
                        "someArray": {
                          "type": "array",
                          "items": {
                            "type": "integer",
                            "format": "int32"
                          }
                        }
                      }
                    }
                """.trimIndent(),
                expectedResultReferencing = mapOf(
                    "." to """
                        {
                          "required": [
                            "someArray",
                            "someList",
                            "someMap",
                            "someSet"
                          ],
                          "type": "object",
                          "properties": {
                            "someList": {
                              "type": "array",
                              "items": {
                                "type": "string"
                              }
                            },
                            "someSet": {
                              "type": "array",
                              "items": {
                                "type": "string"
                              }
                            },
                            "someMap": {
                              "type": "object",
                              "additionalProperties": {
                                "type": "integer",
                                "format": "int32"
                              }
                            },
                            "someArray": {
                              "type": "array",
                              "items": {
                                "type": "integer",
                                "format": "int32"
                              }
                            }
                          }
                        }
                    """.trimIndent()
                ),
                expectedResultReferencingRoot = mapOf(
                    "." to """
                      {
                        "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithCollections"
                      }
                    """.trimIndent(),
                    "io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithCollections" to """
                        {
                          "required": [
                            "someArray",
                            "someList",
                            "someMap",
                            "someSet"
                          ],
                          "type": "object",
                          "properties": {
                            "someList": {
                              "type": "array",
                              "items": {
                                "type": "string"
                              }
                            },
                            "someSet": {
                              "type": "array",
                              "items": {
                                "type": "string"
                              }
                            },
                            "someMap": {
                              "type": "object",
                              "additionalProperties": {
                                "type": "integer",
                                "format": "int32"
                              }
                            },
                            "someArray": {
                              "type": "array",
                              "items": {
                                "type": "integer",
                                "format": "int32"
                              }
                            }
                          }
                        }
                    """.trimIndent()
                )
            ),
            TestData(
                type = typeOf<ClassDirectSelfReferencing>(),
                testName = "class with direct self reference",
                expectedResultInlining = """
                    {
                      "type": "object",
                      "properties": {
                        "self": {
                          "type": ["object", "null"],
                          "properties": {
                            "self": {
                              "type": ["object", "null"],
                              "properties": {
                                "self": {
                                  "type": ["object", "null"],
                                  "properties": {
                                    "self": {
                                      "type": ["object", "null"],
                                      "properties": {
                                        "self": {
                                          "type": ["object", "null"],
                                          "properties": {
                                            "self": {
                                              "type": ["object", "null"],
                                              "properties": {
                                                "self": {
                                                  "type": ["object", "null"],
                                                  "properties": {
                                                    "self": {
                                                      "type": ["object", "null"],
                                                      "properties": {
                                                        "self": {
                                                          "type": ["object", "null"],
                                                          "properties": {
                                                            "self": {
                                                              "type": ["object", "null"],
                                                              "properties": {
                                                                "self": {
                                                                  "type": ["object", "null"],
                                                                  "properties": {
                                                                    "self": {
                                                                      "type": ["object", "null"],
                                                                      "properties": {
                                                                        "self": {
                                                                          "type": ["object", "null"],
                                                                          "properties": {
                                                                            "self": {
                                                                              "type": ["object", "null"],
                                                                              "properties": {
                                                                                "self": {
                                                                                  "type": ["object", "null"],
                                                                                  "properties": {
                                                                                    "self": {
                                                                                      "type": ["object", "null"],
                                                                                      "properties": {
                                                                                        "self": {
                                                                                          "type": ["object", "null"],
                                                                                          "properties": {
                                                                                            "self": {
                                                                                              "type": ["object", "null"],
                                                                                              "properties": {
                                                                                                "self": {
                                                                                                  "type": ["object", "null"],
                                                                                                  "properties": {
                                                                                                    "self": {
                                                                                                      "type": ["object", "null"],
                                                                                                      "properties": {
                                                                                                        "self": {
                                                                                                          "type": ["object", "null"],
                                                                                                          "properties": {
                                                                                                            "self": {
                                                                                                              "type": ["object", "null"],
                                                                                                              "properties": {
                                                                                                                "self": {
                                                                                                                  "type": ["object", "null"],
                                                                                                                  "properties": {
                                                                                                                    "self": {
                                                                                                                      "type": ["object", "null"],
                                                                                                                      "properties": {
                                                                                                                        "self": {
                                                                                                                          "type": ["object", "null"],
                                                                                                                          "properties": {
                                                                                                                            "self": {
                                                                                                                              "type": ["object", "null"],
                                                                                                                              "properties": {
                                                                                                                                "self": {
                                                                                                                                  "type": ["object", "null"],
                                                                                                                                  "properties": {
                                                                                                                                    "self": {
                                                                                                                                      "type": ["object", "null"],
                                                                                                                                      "properties": {
                                                                                                                                        "self": {
                                                                                                                                          "type": ["object", "null"],
                                                                                                                                          "properties": {
                                                                                                                                            "self": {
                                                                                                                                              "type": ["object", "null"],
                                                                                                                                              "properties": {
                                                                                                                                                "self": {
                                                                                                                                                  "type": ["object", "null"],
                                                                                                                                                  "properties": {
                                                                                                                                                    "self": {
                                                                                                                                                      "type": ["object", "null"],
                                                                                                                                                      "properties": {
                                                                                                                                                        "self": {
                                                                                                                                                        }
                                                                                                                                                      }
                                                                                                                                                    }
                                                                                                                                                  }
                                                                                                                                                }
                                                                                                                                              }
                                                                                                                                            }
                                                                                                                                          }
                                                                                                                                        }
                                                                                                                                      }
                                                                                                                                    }
                                                                                                                                  }
                                                                                                                                }
                                                                                                                              }
                                                                                                                            }
                                                                                                                          }
                                                                                                                        }
                                                                                                                      }
                                                                                                                    }
                                                                                                                  }
                                                                                                                }
                                                                                                              }
                                                                                                            }
                                                                                                          }
                                                                                                        }
                                                                                                      }
                                                                                                    }
                                                                                                  }
                                                                                                }
                                                                                              }
                                                                                            }
                                                                                          }
                                                                                        }
                                                                                      }
                                                                                    }
                                                                                  }
                                                                                }
                                                                              }
                                                                            }
                                                                          }
                                                                        }
                                                                      }
                                                                    }
                                                                  }
                                                                }
                                                              }
                                                            }
                                                          }
                                                        }
                                                      }
                                                    }
                                                  }
                                                }
                                              }
                                            }
                                          }
                                        }
                                      }
                                    }
                                  }
                                }
                              }
                            }
                          }
                        }
                      }
                    }
                """.trimIndent(),
                expectedResultReferencing = mapOf(
                    "." to """
                        {
                        "type": "object",
                        "properties": {
                          "self": {
                            "oneOf": [
                              {
                                "type": "null"
                              },
                              {
                                "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.kotlinx.ClassDirectSelfReferencing"
                              }
                            ]
                          }
                        }
                      }
                    """.trimIndent(),
                    "io.github.smiley4.schemakenerator.test.models.kotlinx.ClassDirectSelfReferencing" to """
                        {
                          "type": "object",
                          "properties": {
                            "self": {
                              "oneOf": [
                                {
                                  "type": "null"
                                },
                                {
                                  "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.kotlinx.ClassDirectSelfReferencing"
                                }
                              ]
                            }
                          }
                        }
                    """.trimIndent()
                ),
                expectedResultReferencingRoot = mapOf(
                    "." to """
                      {
                        "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.kotlinx.ClassDirectSelfReferencing"
                      }
                    """.trimIndent(),
                    "io.github.smiley4.schemakenerator.test.models.kotlinx.ClassDirectSelfReferencing" to """
                        {
                          "type": "object",
                          "properties": {
                            "self": {
                              "oneOf": [
                                {
                                  "type": "null"
                                },
                                {
                                  "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.kotlinx.ClassDirectSelfReferencing"
                                }
                              ]
                            }
                          }
                        }
                    """.trimIndent()
                )
            ),
            TestData(
                type = typeOf<ClassWithOptionalParameters>(),
                testName = "optional parameters as required",
                generatorConfig = {
                    optionalHandling = OptionalHandling.REQUIRED
                },
                expectedResultInlining = """
                    {
                      "required": [
                        "ctorOptional",
                        "ctorRequired"
                      ],
                      "type": "object",
                      "properties": {
                        "ctorOptional": {
                          "type": "string"
                        },
                        "ctorOptionalNullable": {
                          "type": ["string", "null"]
                        },
                        "ctorRequired": {
                          "type": "string"
                        },
                        "ctorRequiredNullable": {
                          "type": ["string", "null"]
                        }
                      }
                    }
                """.trimIndent(),
                expectedResultReferencing = mapOf(
                    "." to """
                        {
                          "required": [
                            "ctorOptional",
                            "ctorRequired"
                          ],
                          "type": "object",
                          "properties": {
                            "ctorOptional": {
                              "type": "string"
                            },
                            "ctorOptionalNullable": {
                              "type": ["string", "null"]
                            },
                            "ctorRequired": {
                              "type": "string"
                            },
                            "ctorRequiredNullable": {
                              "type": ["string", "null"]
                            }
                          }
                        }
                    """.trimIndent()
                ),
                expectedResultReferencingRoot = mapOf(
                    "." to """
                      {
                        "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithOptionalParameters"
                      }
                    """.trimIndent(),
                    "io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithOptionalParameters" to """
                        {
                          "required": [
                            "ctorOptional",
                            "ctorRequired"
                          ],
                          "type": "object",
                          "properties": {
                            "ctorOptional": {
                              "type": "string"
                            },
                            "ctorOptionalNullable": {
                              "type": ["string", "null"]
                            },
                            "ctorRequired": {
                              "type": "string"
                            },
                            "ctorRequiredNullable": {
                              "type": ["string", "null"]
                            }
                          }
                        }
                    """.trimIndent()
                )
            ),
            TestData(
                type = typeOf<ClassWithOptionalParameters>(),
                testName = "optional parameters as non-required",
                generatorConfig = {
                    optionalHandling = OptionalHandling.NON_REQUIRED
                },
                expectedResultInlining = """
                    {
                      "required": [
                        "ctorRequired"
                      ],
                      "type": "object",
                      "properties": {
                        "ctorOptional": {
                          "type": "string"
                        },
                        "ctorOptionalNullable": {
                          "type": ["string", "null"]
                        },
                        "ctorRequired": {
                          "type": "string"
                        },
                        "ctorRequiredNullable": {
                          "type": ["string", "null"]
                        }
                      }
                    }
                """.trimIndent(),
                expectedResultReferencing = mapOf(
                    "." to """
                        {
                          "required": [
                            "ctorRequired"
                          ],
                          "type": "object",
                          "properties": {
                            "ctorOptional": {
                              "type": "string"
                            },
                            "ctorOptionalNullable": {
                              "type": ["string", "null"]
                            },
                            "ctorRequired": {
                              "type": "string"
                            },
                            "ctorRequiredNullable": {
                              "type": ["string", "null"]
                            }
                          }
                        }
                    """.trimIndent()
                ),
                expectedResultReferencingRoot = mapOf(
                    "." to """
                      {
                        "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithOptionalParameters"
                      }
                    """.trimIndent(),
                    "io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithOptionalParameters" to """
                        {
                          "required": [
                            "ctorRequired"
                          ],
                          "type": "object",
                          "properties": {
                            "ctorOptional": {
                              "type": "string"
                            },
                            "ctorOptionalNullable": {
                              "type": ["string", "null"]
                            },
                            "ctorRequired": {
                              "type": "string"
                            },
                            "ctorRequiredNullable": {
                              "type": ["string", "null"]
                            }
                          }
                        }
                    """.trimIndent()
                )
            ),
            TestData(
                type = typeOf<ClassWithValueClass>(),
                testName = "inline value class",
                expectedResultInlining = """
                    {
                      "required": [
                        "myValue",
                        "someText"
                      ],
                      "type": "object",
                      "properties": {
                        "myValue": {
                          "type": "integer",
                          "format": "int32"
                        },
                        "someText": {
                          "type": "string"
                        }
                      }
                    }
                """.trimIndent(),
                expectedResultReferencing = mapOf(
                    "." to """
                        {
                          "required": [
                            "myValue",
                            "someText"
                          ],
                          "type": "object",
                          "properties": {
                            "myValue": {
                              "type": "integer",
                              "format": "int32"
                            },
                            "someText": {
                              "type": "string"
                            }
                          }
                        }
                    """.trimIndent()
                ),
                expectedResultReferencingRoot = mapOf(
                    "." to """
                      {
                        "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithValueClass"
                      }
                    """.trimIndent(),
                    "io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithValueClass" to """
                        {
                          "required": [
                            "myValue",
                            "someText"
                          ],
                          "type": "object",
                          "properties": {
                            "myValue": {
                              "type": "integer",
                              "format": "int32"
                            },
                            "someText": {
                              "type": "string"
                            }
                          }
                        }
                    """.trimIndent()
                )
            ),
        )

    }

}
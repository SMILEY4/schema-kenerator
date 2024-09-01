package io.github.smiley4.schemakenerator.test

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.smiley4.schemakenerator.serialization.steps.KotlinxSerializationTypeProcessingStep
import io.github.smiley4.schemakenerator.swagger.OptionalHandling
import io.github.smiley4.schemakenerator.swagger.SwaggerSchemaGenerationStepConfig
import io.github.smiley4.schemakenerator.swagger.data.TitleType
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaAutoTitleStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCompileInlineStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCompileReferenceRootStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCompileReferenceStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCoreAnnotationDefaultStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCoreAnnotationDeprecatedStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCoreAnnotationDescriptionStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCoreAnnotationExamplesStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCoreAnnotationTitleStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaGenerationStep
import io.github.smiley4.schemakenerator.test.models.kotlinx.ClassDirectSelfReferencing
import io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithCollections
import io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithDeepGeneric
import io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithGenericField
import io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithNestedClass
import io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithSimpleFields
import io.github.smiley4.schemakenerator.test.models.kotlinx.SealedClass
import io.github.smiley4.schemakenerator.test.models.kotlinx.SubClassA
import io.github.smiley4.schemakenerator.test.models.kotlinx.TestEnum
import io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithOptionalParameters
import io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithValueClass
import io.github.smiley4.schemakenerator.test.models.kotlinx.CoreAnnotatedClass
import io.kotest.assertions.json.ArrayOrder
import io.kotest.assertions.json.FieldComparison
import io.kotest.assertions.json.NumberFormat
import io.kotest.assertions.json.PropertyOrder
import io.kotest.assertions.json.TypeCoercion
import io.kotest.assertions.json.shouldEqualJson
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
                    } else {
                        list
                    }
                }
                .let { list ->
                    if (data.withAutoTitle) {
                        list
                            .let { SwaggerSchemaAutoTitleStep(TitleType.SIMPLE).process(it) }
                    } else {
                        list
                    }
                }
                .let { SwaggerSchemaCompileInlineStep().compile(it) }

            json.writeValueAsString(schema.swagger).shouldEqualJson {
                propertyOrder = PropertyOrder.Lenient
                arrayOrder = ArrayOrder.Lenient
                fieldComparison = FieldComparison.Strict
                numberFormat = NumberFormat.Lenient
                typeCoercion = TypeCoercion.Disabled
                data.expectedResultInlining
            }
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
                    } else {
                        list
                    }
                }
                .let { list ->
                    if (data.withAutoTitle) {
                        list
                            .let { SwaggerSchemaAutoTitleStep(TitleType.SIMPLE).process(it) }
                    } else {
                        list
                    }
                }
                .let { SwaggerSchemaCompileReferenceStep().compile(it) }
                .let {
                    Result(
                        schema = it.swagger,
                        definitions = it.componentSchemas
                    )
                }

            json.writeValueAsString(schema).shouldEqualJson {
                propertyOrder = PropertyOrder.Lenient
                arrayOrder = ArrayOrder.Lenient
                fieldComparison = FieldComparison.Strict
                numberFormat = NumberFormat.Lenient
                typeCoercion = TypeCoercion.Disabled
                data.expectedResultReferencing
            }
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
                    } else {
                        list
                    }
                }
                .let { list ->
                    if (data.withAutoTitle) {
                        list
                            .let { SwaggerSchemaAutoTitleStep(TitleType.SIMPLE).process(it) }
                    } else {
                        list
                    }
                }
                .let { SwaggerSchemaCompileReferenceRootStep().compile(it) }
                .let {
                    Result(
                        schema = it.swagger,
                        definitions = it.componentSchemas
                    )
                }

            json.writeValueAsString(schema).shouldEqualJson {
                propertyOrder = PropertyOrder.Lenient
                arrayOrder = ArrayOrder.Lenient
                fieldComparison = FieldComparison.Strict
                numberFormat = NumberFormat.Lenient
                typeCoercion = TypeCoercion.Disabled
                data.expectedResultReferencingRoot
            }
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
            val expectedResultReferencing: String,
            val expectedResultReferencingRoot: String,
        ) : WithDataTestName {
            override fun dataTestName() = testName
        }

        private val TEST_DATA = listOf(
            TestData(
                type = typeOf<Any>(),
                testName = "any",
                expectedResultInlining = """
                    {
                        "type": "object",
                        "exampleSetFlag": false
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "schema": {
                            "type": "object",
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "schema": {
                            "type": "object",
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
            ),
            TestData(
                type = typeOf<UByte>(),
                testName = "ubyte",
                expectedResultInlining = """
                    {
                        "type": "integer",
                        "maximum": 255,
                        "minimum": 0,
                        "exampleSetFlag": false
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "schema": {
                            "type": "integer",
                            "maximum": 255,
                            "minimum": 0,
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "schema": {
                            "type": "integer",
                            "maximum": 255,
                            "minimum": 0,
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
            ),
            TestData(
                type = typeOf<Int>(),
                testName = "int",
                expectedResultInlining = """
                    {
                        "type": "integer",
                        "format": "int32",
                        "exampleSetFlag": false
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "schema": {
                            "type": "integer",
                            "format": "int32",
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "schema": {
                            "type": "integer",
                            "format": "int32",
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent()
            ),
            TestData(
                type = typeOf<Float>(),
                testName = "float",
                expectedResultInlining = """
                    {
                        "type": "number",
                        "format": "float",
                        "exampleSetFlag": false
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "schema": {
                        "type": "number",
                        "format": "float",
                        "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "schema": {
                            "type": "number",
                            "format": "float",
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
            ),
            TestData(
                type = typeOf<Boolean>(),
                testName = "boolean",
                expectedResultInlining = """
                    {
                         "type": "boolean",
                         "exampleSetFlag": false
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "schema": {
                            "type": "boolean",
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "schema": {
                            "type": "boolean",
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
            ),
            TestData(
                type = typeOf<String>(),
                testName = "string",
                expectedResultInlining = """
                    {
                        "type": "string",
                        "exampleSetFlag": false
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "schema": {
                            "type": "string",
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "schema": {
                            "type": "string",
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
            ),
            TestData(
                // top-level lists not directly supported: weird result
                type = typeOf<List<String>>(),
                testName = "list of strings",
                expectedResultInlining = """
                    {
                        "type": "object",
                        "properties": {},
                        "exampleSetFlag": false
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "schema": {
                            "type": "object",
                            "properties": {},
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "schema": {
                            "${'$'}ref": "#/components/schemas/kotlinx.serialization.Polymorphic<List>",
                            "exampleSetFlag": false
                        },
                        "definitions": {
                            "kotlinx.serialization.Polymorphic<List>": {
                                "type": "object",
                                "properties": {},
                                "exampleSetFlag": false
                            }
                        }
                    }
                """.trimIndent(),
            ),
            // top-level maps not directly supported: weird result
            TestData(
                type = typeOf<Map<String, Int>>(),
                testName = "map of strings to integers",
                expectedResultInlining = """
                    {
                        "type": "object",
                        "properties": {},
                        "exampleSetFlag": false
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "schema": {
                            "type": "object",
                            "properties": {},
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "schema": {
                            "${'$'}ref": "#/components/schemas/kotlinx.serialization.Polymorphic<Map>",
                            "exampleSetFlag": false
                        },
                        "definitions": {
                            "kotlinx.serialization.Polymorphic<Map>": {
                                "type": "object",
                                "properties": {},
                                "exampleSetFlag": false
                            }
                        }
                    }
                """.trimIndent(),
            ),
            TestData(
                type = typeOf<ClassWithSimpleFields>(),
                testName = "class with simple fields",
                expectedResultInlining = """
                    {
                        "required": [
                            "someBoolList",
                            "someString"
                        ],
                        "type": "object",
                        "properties": {
                            "someBoolList": {
                                "type": "array",
                                "exampleSetFlag": false,
                                "items": {
                                  "type": "boolean",
                                  "exampleSetFlag": false
                                }
                            },
                            "someNullableInt": {
                                "type": "integer",
                                "format": "int32",
                                "exampleSetFlag": false
                            },
                            "someString": {
                                "type": "string",
                                "exampleSetFlag": false
                            }
                        },
                        "exampleSetFlag": false
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "schema": {
                            "required": [
                                "someBoolList",
                                "someString"
                            ],
                            "type": "object",
                            "properties": {
                                "someBoolList": {
                                    "type": "array",
                                    "exampleSetFlag": false,
                                    "items": {
                                      "type": "boolean",
                                      "exampleSetFlag": false
                                    }
                                },
                                "someNullableInt": {
                                    "type": "integer",
                                    "format": "int32",
                                    "exampleSetFlag": false
                                },
                                "someString": {
                                    "type": "string",
                                    "exampleSetFlag": false
                                }
                            },
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "schema": {
                            "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithSimpleFields",
                            "exampleSetFlag": false
                        },
                        "definitions": {
                            "io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithSimpleFields": {
                                "required": [
                                    "someBoolList",
                                    "someString"
                                ],
                                "type": "object",
                                "properties": {
                                    "someBoolList": {
                                        "type": "array",
                                        "exampleSetFlag": false,
                                        "items": {
                                            "type": "boolean",
                                            "exampleSetFlag": false
                                        }
                                    },
                                    "someNullableInt": {
                                        "type": "integer",
                                        "format": "int32",
                                        "exampleSetFlag": false
                                    },
                                    "someString": {
                                        "type": "string",
                                        "exampleSetFlag": false
                                    }
                                },
                                "exampleSetFlag": false
                            }
                        }
                    }
                """.trimIndent(),
            ),
            TestData(
                type = typeOf<TestEnum>(),
                testName = "enum",
                expectedResultInlining = """
                    {
                        "exampleSetFlag": false,
                        "enum": [ "ONE", "TWO", "THREE" ]
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "schema": {
                            "exampleSetFlag": false,
                            "enum": [ "ONE", "TWO", "THREE" ]
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "schema": {
                            "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.kotlinx.TestEnum",
                            "exampleSetFlag": false
                        },
                        "definitions": {
                            "io.github.smiley4.schemakenerator.test.models.kotlinx.TestEnum": {
                                "exampleSetFlag": false,
                                "enum": [ "ONE", "TWO", "THREE" ]
                            }
                        }
                    }
                """.trimIndent(),
            ),
            TestData(
                // generics not supported with kotlinx-serialization -> fallback to "any"-schema
                type = typeOf<ClassWithGenericField<String>>(),
                testName = "class with defined generic field",
                expectedResultInlining = """
                    {
                        "type": "object",
                        "exampleSetFlag": false
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "schema": {
                            "type": "object",
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "schema": {
                            "type": "object",
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
            ),
            TestData(
                // generics not supported with kotlinx-serialization -> fallback to "any"-schema
                type = typeOf<ClassWithGenericField<*>>(),
                testName = "class with wildcard generic field",
                expectedResultInlining = """
                    {
                        "type": "object",
                        "exampleSetFlag": false
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "schema": {
                            "type": "object",
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "schema": {
                            "type": "object",
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
            ),
            TestData(
                // generics not supported with kotlinx-serialization -> fallback to "any"-schema
                type = typeOf<ClassWithDeepGeneric<String>>(),
                testName = "class with deep generic field",
                expectedResultInlining = """
                    {
                        "type": "object",
                        "exampleSetFlag": false
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "schema": {
                            "type": "object",
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "schema": {
                            "type": "object",
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
            ),
            TestData(
                type = typeOf<SealedClass>(),
                testName = "sealed class with subtypes",
                expectedResultInlining = """
                    {
                        "exampleSetFlag": false,
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
                                          "format": "int32",
                                          "exampleSetFlag": false
                                        },
                                    "sealedValue": {
                                        "type": "string",
                                        "exampleSetFlag": false
                                    }
                                },
                                "exampleSetFlag": false
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
                                        "format": "int32",
                                        "exampleSetFlag": false
                                    },
                                    "sealedValue": {
                                        "type": "string",
                                        "exampleSetFlag": false
                                    }
                                },
                                "exampleSetFlag": false
                            }
                        ]
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "schema": {
                            "exampleSetFlag": false,
                            "anyOf": [
                                {
                                    "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.kotlinx.SubClassA",
                                    "exampleSetFlag": false
                                },
                                {
                                    "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.kotlinx.SubClassB",
                                    "exampleSetFlag": false
                                }
                            ]
                        },
                        "definitions": {
                            "io.github.smiley4.schemakenerator.test.models.kotlinx.SubClassA": {
                                "required": [
                                    "a",
                                    "sealedValue"
                                ],
                                "type": "object",
                                "properties": {
                                    "a": {
                                        "type": "integer",
                                        "format": "int32",
                                        "exampleSetFlag": false
                                    },
                                    "sealedValue": {
                                        "type": "string",
                                        "exampleSetFlag": false
                                    }
                                },
                                "exampleSetFlag": false
                            },
                            "io.github.smiley4.schemakenerator.test.models.kotlinx.SubClassB": {
                                "required": [
                                    "b",
                                    "sealedValue"
                                ],
                                "type": "object",
                                "properties": {
                                    "b": {
                                        "type": "integer",
                                        "format": "int32",
                                        "exampleSetFlag": false
                                    },
                                    "sealedValue": {
                                        "type": "string",
                                        "exampleSetFlag": false
                                    }
                                },
                                "exampleSetFlag": false
                            }
                        }
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "schema": {
                            "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.kotlinx.SealedClass",
                            "exampleSetFlag": false
                        },
                        "definitions": {
                            "io.github.smiley4.schemakenerator.test.models.kotlinx.SealedClass": {
                                "exampleSetFlag": false,
                                "anyOf": [
                                    {
                                        "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.kotlinx.SubClassA",
                                        "exampleSetFlag": false
                                    },
                                    {
                                        "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.kotlinx.SubClassB",
                                        "exampleSetFlag": false
                                    }
                                ]
                            },
                            "io.github.smiley4.schemakenerator.test.models.kotlinx.SubClassA": {
                                "required": [
                                    "a",
                                    "sealedValue"
                                ],
                                "type": "object",
                                "properties": {
                                    "a": {
                                        "type": "integer",
                                        "format": "int32",
                                        "exampleSetFlag": false
                                    },
                                    "sealedValue": {
                                        "type": "string",
                                        "exampleSetFlag": false
                                    }
                                },
                                "exampleSetFlag": false
                            },
                            "io.github.smiley4.schemakenerator.test.models.kotlinx.SubClassB": {
                                "required": [
                                    "b",
                                    "sealedValue"
                                ],
                                "type": "object",
                                "properties": {
                                    "b": {
                                        "type": "integer",
                                        "format": "int32",
                                        "exampleSetFlag": false
                                    },
                                    "sealedValue": {
                                        "type": "string",
                                        "exampleSetFlag": false
                                    }
                                },
                                "exampleSetFlag": false
                            }
                        }
                    }
                """.trimIndent(),
            ),
            TestData(
                type = typeOf<SubClassA>(),
                testName = "sub class",
                expectedResultInlining = """
                    {
                        "required": [
                            "a",
                            "sealedValue"
                        ],
                        "type": "object",
                        "properties": {
                            "a": {
                                "type": "integer",
                                "format": "int32",
                                "exampleSetFlag": false
                            },
                            "sealedValue": {
                                "type": "string",
                                "exampleSetFlag": false
                            }
                        },
                        "exampleSetFlag": false
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "schema": {
                            "required": [
                                "a",
                                "sealedValue"
                            ],
                            "type": "object",
                            "properties": {
                                "a": {
                                    "type": "integer",
                                    "format": "int32",
                                    "exampleSetFlag": false
                                },
                                "sealedValue": {
                                    "type": "string",
                                    "exampleSetFlag": false
                                }
                            },
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "schema": {
                            "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.kotlinx.SubClassA",
                            "exampleSetFlag": false
                        },
                        "definitions": {
                            "io.github.smiley4.schemakenerator.test.models.kotlinx.SubClassA": {
                                "required": [
                                    "a",
                                    "sealedValue"
                                ],
                                "type": "object",
                                "properties": {
                                    "a": {
                                        "type": "integer",
                                        "format": "int32",
                                        "exampleSetFlag": false
                                    },
                                    "sealedValue": {
                                        "type": "string",
                                        "exampleSetFlag": false
                                    }
                                },
                                "exampleSetFlag": false
                            }
                        }
                    }
                """.trimIndent(),
            ),
            TestData(
                type = typeOf<CoreAnnotatedClass>(),
                testName = "annotated class (core)",
                withAnnotations = true,
                expectedResultInlining = """
                    {
                        "title": "Annotated Class",
                        "required": [
                            "value"
                        ],
                        "type": "object",
                        "properties": {
                            "value": {
                                "type": "string",
                                "exampleSetFlag": false
                            }
                        },
                        "description": "some description",
                        "deprecated": true,
                        "exampleSetFlag": true,
                        "example": "example 1",
                        "default": "default value"
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "schema": {
                            "title": "Annotated Class",
                            "required": [
                                "value"
                            ],
                            "type": "object",
                            "properties": {
                                "value": {
                                    "type": "string",
                                    "exampleSetFlag": false
                                }
                            },
                            "description": "some description",
                            "deprecated": true,
                            "exampleSetFlag": true,
                            "example": "example 1",
                            "default": "default value"
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "schema": {
                            "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.kotlinx.CoreAnnotatedClass",
                            "exampleSetFlag": false
                        },
                        "definitions": {
                            "io.github.smiley4.schemakenerator.test.models.kotlinx.CoreAnnotatedClass": {
                                "title": "Annotated Class",
                                "required": [
                                    "value"
                                ],
                                "type": "object",
                                "properties": {
                                    "value": {
                                        "type": "string",
                                        "exampleSetFlag": false
                                    }
                                },
                                "description": "some description",
                                "deprecated": true,
                                "exampleSetFlag": true,
                                "example": "example 1",
                                "default": "default value"
                            }
                        }
                    }
                """.trimIndent(),
            ),
//            TestData(
//                type = typeOf<ClassWithLocalDateTime>(),
//                testName = "class with java local-date-time and custom parser",
//                customParsers = mapOf(
//                    LocalDateTime::class to CustomKotlinxSerializationTypeParser { typeId, _ ->
//                        PrimitiveTypeData(
//                            id = typeId,
//                            simpleName = String::class.simpleName!!,
//                            qualifiedName = String::class.qualifiedName!!,
//                            typeParameters = mutableMapOf()
//                        )
//                    }
//                ),
//                expectedResultInlining = """
//                    {
//                        "schema": {
//                            "required": [
//                                "dateTime"
//                            ],
//                            "type": "object",
//                            "properties": {
//                                "dateTime": {
//                                    "type": "string",
//                                    "exampleSetFlag": false
//                                }
//                            },
//                            "exampleSetFlag": false
//                        },
//                        "definitions": {}
//                    }
//                """.trimIndent(),
//                expectedResultReferencing = """
//                    {
//                        "schema": {
//                            "required": [
//                                "dateTime"
//                            ],
//                            "type": "object",
//                            "properties": {
//                                "dateTime": {
//                                    "type": "string",
//                                    "exampleSetFlag": false
//                                }
//                            },
//                            "exampleSetFlag": false
//                        },
//                        "definitions": {}
//                    }
//                """.trimIndent(),
//                expectedResultReferencingRoot = """
//                    {
//                        "schema": {
//                            "${'$'}ref": "#/definitions/ClassWithLocalDateTime",
//                            "exampleSetFlag": false
//                        },
//                        "definitions": {
//                            "ClassWithLocalDateTime": {
//                                "required": [
//                                    "dateTime"
//                                ],
//                                "type": "object",
//                                "properties": {
//                                    "dateTime": {
//                                        "type": "string",
//                                        "exampleSetFlag": false
//                                    }
//                                },
//                                "exampleSetFlag": false
//                            }
//                        }
//                    }
//                """.trimIndent(),
//            ),
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
                                "required": [
                                    "text"
                                ],
                                "type": "object",
                                "properties": {
                                    "text": {
                                        "title": "String",
                                        "type": "string",
                                        "exampleSetFlag": false
                                    }
                                },
                                "exampleSetFlag": false
                            }
                        },
                        "exampleSetFlag": false
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "schema": {
                            "title": "ClassWithNestedClass",
                            "required": [
                                "nested"
                            ],
                            "type": "object",
                            "properties": {
                                "nested": {
                                    "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.kotlinx.NestedClass",
                                    "exampleSetFlag": false
                                }
                            },
                            "exampleSetFlag": false
                        },
                        "definitions": {
                            "io.github.smiley4.schemakenerator.test.models.kotlinx.NestedClass": {
                                "title": "NestedClass",
                                "required": [
                                    "text"
                                ],
                                "type": "object",
                                "properties": {
                                    "text": {
                                        "title": "String",
                                        "type": "string",
                                        "exampleSetFlag": false
                                    }
                                },
                                "exampleSetFlag": false
                            }
                        }
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "schema": {
                            "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithNestedClass",
                            "exampleSetFlag": false
                        },
                        "definitions": {
                            "io.github.smiley4.schemakenerator.test.models.kotlinx.NestedClass": {
                                "title": "NestedClass",
                                "required": [
                                    "text"
                                ],
                                "type": "object",
                                "properties": {
                                    "text": {
                                        "title": "String",
                                        "type": "string",
                                        "exampleSetFlag": false
                                    }
                                },
                                "exampleSetFlag": false
                            },
                            "io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithNestedClass": {
                                "title": "ClassWithNestedClass",
                                "required": [
                                    "nested"
                                ],
                                "type": "object",
                                "properties": {
                                    "nested": {
                                        "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.kotlinx.NestedClass",
                                        "exampleSetFlag": false
                                    }
                                },
                                "exampleSetFlag": false
                            }
                        }
                    }
                """.trimIndent(),
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
                          "exampleSetFlag": false,
                          "items": {
                            "type": "string",
                            "exampleSetFlag": false
                          }
                        },
                        "someSet": {
                          "type": "array",
                          "exampleSetFlag": false,
                          "items": {
                            "type": "string",
                            "exampleSetFlag": false
                          }
                        },
                        "someMap": {
                          "type": "object",
                          "additionalProperties": {
                            "type": "integer",
                            "format": "int32",
                            "exampleSetFlag": false
                          },
                          "exampleSetFlag": false
                        },
                        "someArray": {
                          "type": "array",
                          "exampleSetFlag": false,
                          "items": {
                            "type": "integer",
                            "format": "int32",
                            "exampleSetFlag": false
                          }
                        }
                      },
                      "exampleSetFlag": false
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                      "schema": {
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
                            "exampleSetFlag": false,
                            "items": {
                              "type": "string",
                              "exampleSetFlag": false
                            }
                          },
                          "someSet": {
                            "type": "array",
                            "exampleSetFlag": false,
                            "items": {
                              "type": "string",
                              "exampleSetFlag": false
                            }
                          },
                          "someMap": {
                            "type": "object",
                            "additionalProperties": {
                              "type": "integer",
                              "format": "int32",
                              "exampleSetFlag": false
                            },
                            "exampleSetFlag": false
                          },
                          "someArray": {
                            "type": "array",
                            "exampleSetFlag": false,
                            "items": {
                              "type": "integer",
                              "format": "int32",
                              "exampleSetFlag": false
                            }
                          }
                        },
                        "exampleSetFlag": false
                      },
                      "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                      "schema": {
                        "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithCollections",
                        "exampleSetFlag": false
                      },
                      "definitions": {
                        "io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithCollections": {
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
                              "exampleSetFlag": false,
                              "items": {
                                "type": "string",
                                "exampleSetFlag": false
                              }
                            },
                            "someSet": {
                              "type": "array",
                              "exampleSetFlag": false,
                              "items": {
                                "type": "string",
                                "exampleSetFlag": false
                              }
                            },
                            "someMap": {
                              "type": "object",
                              "additionalProperties": {
                                "type": "integer",
                                "format": "int32",
                                "exampleSetFlag": false
                              },
                              "exampleSetFlag": false
                            },
                            "someArray": {
                              "type": "array",
                              "exampleSetFlag": false,
                              "items": {
                                "type": "integer",
                                "format": "int32",
                                "exampleSetFlag": false
                              }
                            }
                          },
                          "exampleSetFlag": false
                        }
                      }
                    }
                """.trimIndent(),
            ),
            TestData(
                type = typeOf<ClassDirectSelfReferencing>(),
                testName = "class with direct self reference",
                expectedResultInlining = """
                    {
                      "type": "object",
                      "properties": {
                        "self": {
                          "type": "object",
                          "properties": {
                            "self": {
                              "type": "object",
                              "properties": {
                                "self": {
                                  "type": "object",
                                  "properties": {
                                    "self": {
                                      "type": "object",
                                      "properties": {
                                        "self": {
                                          "type": "object",
                                          "properties": {
                                            "self": {
                                              "type": "object",
                                              "properties": {
                                                "self": {
                                                  "type": "object",
                                                  "properties": {
                                                    "self": {
                                                      "type": "object",
                                                      "properties": {
                                                        "self": {
                                                          "type": "object",
                                                          "properties": {
                                                            "self": {
                                                              "type": "object",
                                                              "properties": {
                                                                "self": {
                                                                  "type": "object",
                                                                  "properties": {
                                                                    "self": {
                                                                      "type": "object",
                                                                      "properties": {
                                                                        "self": {
                                                                          "type": "object",
                                                                          "properties": {
                                                                            "self": {
                                                                              "type": "object",
                                                                              "properties": {
                                                                                "self": {
                                                                                  "type": "object",
                                                                                  "properties": {
                                                                                    "self": {
                                                                                      "type": "object",
                                                                                      "properties": {
                                                                                        "self": {
                                                                                          "type": "object",
                                                                                          "properties": {
                                                                                            "self": {
                                                                                              "type": "object",
                                                                                              "properties": {
                                                                                                "self": {
                                                                                                  "type": "object",
                                                                                                  "properties": {
                                                                                                    "self": {
                                                                                                      "type": "object",
                                                                                                      "properties": {
                                                                                                        "self": {
                                                                                                          "type": "object",
                                                                                                          "properties": {
                                                                                                            "self": {
                                                                                                              "type": "object",
                                                                                                              "properties": {
                                                                                                                "self": {
                                                                                                                  "type": "object",
                                                                                                                  "properties": {
                                                                                                                    "self": {
                                                                                                                      "type": "object",
                                                                                                                      "properties": {
                                                                                                                        "self": {
                                                                                                                          "type": "object",
                                                                                                                          "properties": {
                                                                                                                            "self": {
                                                                                                                              "type": "object",
                                                                                                                              "properties": {
                                                                                                                                "self": {
                                                                                                                                  "type": "object",
                                                                                                                                  "properties": {
                                                                                                                                    "self": {
                                                                                                                                      "type": "object",
                                                                                                                                      "properties": {
                                                                                                                                        "self": {
                                                                                                                                          "type": "object",
                                                                                                                                          "properties": {
                                                                                                                                            "self": {
                                                                                                                                              "type": "object",
                                                                                                                                              "properties": {
                                                                                                                                                "self": {
                                                                                                                                                  "type": "object",
                                                                                                                                                  "properties": {
                                                                                                                                                    "self": {
                                                                                                                                                      "type": "object",
                                                                                                                                                      "properties": {
                                                                                                                                                        "self": {
                                                                                                                                                          "exampleSetFlag": false
                                                                                                                                                        }
                                                                                                                                                      },
                                                                                                                                                      "exampleSetFlag": false
                                                                                                                                                    }
                                                                                                                                                  },
                                                                                                                                                  "exampleSetFlag": false
                                                                                                                                                }
                                                                                                                                              },
                                                                                                                                              "exampleSetFlag": false
                                                                                                                                            }
                                                                                                                                          },
                                                                                                                                          "exampleSetFlag": false
                                                                                                                                        }
                                                                                                                                      },
                                                                                                                                      "exampleSetFlag": false
                                                                                                                                    }
                                                                                                                                  },
                                                                                                                                  "exampleSetFlag": false
                                                                                                                                }
                                                                                                                              },
                                                                                                                              "exampleSetFlag": false
                                                                                                                            }
                                                                                                                          },
                                                                                                                          "exampleSetFlag": false
                                                                                                                        }
                                                                                                                      },
                                                                                                                      "exampleSetFlag": false
                                                                                                                    }
                                                                                                                  },
                                                                                                                  "exampleSetFlag": false
                                                                                                                }
                                                                                                              },
                                                                                                              "exampleSetFlag": false
                                                                                                            }
                                                                                                          },
                                                                                                          "exampleSetFlag": false
                                                                                                        }
                                                                                                      },
                                                                                                      "exampleSetFlag": false
                                                                                                    }
                                                                                                  },
                                                                                                  "exampleSetFlag": false
                                                                                                }
                                                                                              },
                                                                                              "exampleSetFlag": false
                                                                                            }
                                                                                          },
                                                                                          "exampleSetFlag": false
                                                                                        }
                                                                                      },
                                                                                      "exampleSetFlag": false
                                                                                    }
                                                                                  },
                                                                                  "exampleSetFlag": false
                                                                                }
                                                                              },
                                                                              "exampleSetFlag": false
                                                                            }
                                                                          },
                                                                          "exampleSetFlag": false
                                                                        }
                                                                      },
                                                                      "exampleSetFlag": false
                                                                    }
                                                                  },
                                                                  "exampleSetFlag": false
                                                                }
                                                              },
                                                              "exampleSetFlag": false
                                                            }
                                                          },
                                                          "exampleSetFlag": false
                                                        }
                                                      },
                                                      "exampleSetFlag": false
                                                    }
                                                  },
                                                  "exampleSetFlag": false
                                                }
                                              },
                                              "exampleSetFlag": false
                                            }
                                          },
                                          "exampleSetFlag": false
                                        }
                                      },
                                      "exampleSetFlag": false
                                    }
                                  },
                                  "exampleSetFlag": false
                                }
                              },
                              "exampleSetFlag": false
                            }
                          },
                          "exampleSetFlag": false
                        }
                      },
                      "exampleSetFlag": false
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                      "schema": {
                        "type": "object",
                        "properties": {
                          "self": {
                            "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.kotlinx.ClassDirectSelfReferencing",
                            "exampleSetFlag": false
                          }
                        },
                        "exampleSetFlag": false
                      },
                      "definitions": {
                        "io.github.smiley4.schemakenerator.test.models.kotlinx.ClassDirectSelfReferencing": {
                          "type": "object",
                          "properties": {
                            "self": {
                              "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.kotlinx.ClassDirectSelfReferencing",
                              "exampleSetFlag": false
                            }
                          },
                          "exampleSetFlag": false
                        }
                      }
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                      "schema": {
                        "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.kotlinx.ClassDirectSelfReferencing",
                        "exampleSetFlag": false
                      },
                      "definitions": {
                        "io.github.smiley4.schemakenerator.test.models.kotlinx.ClassDirectSelfReferencing": {
                          "type": "object",
                          "properties": {
                            "self": {
                              "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.kotlinx.ClassDirectSelfReferencing",
                              "exampleSetFlag": false
                            }
                          },
                          "exampleSetFlag": false
                        }
                      }
                    }
                """.trimIndent(),
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
                          "type": "string",
                          "exampleSetFlag": false
                        },
                        "ctorOptionalNullable": {
                          "type": "string",
                          "exampleSetFlag": false
                        },
                        "ctorRequired": {
                          "type": "string",
                          "exampleSetFlag": false
                        },
                        "ctorRequiredNullable": {
                          "type": "string",
                          "exampleSetFlag": false
                        }
                      },
                      "exampleSetFlag": false
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                      "schema": {
                        "required": [
                          "ctorOptional",
                          "ctorRequired"
                        ],
                        "type": "object",
                        "properties": {
                          "ctorOptional": {
                            "type": "string",
                            "exampleSetFlag": false
                          },
                          "ctorOptionalNullable": {
                            "type": "string",
                            "exampleSetFlag": false
                          },
                          "ctorRequired": {
                            "type": "string",
                            "exampleSetFlag": false
                          },
                          "ctorRequiredNullable": {
                            "type": "string",
                            "exampleSetFlag": false
                          }
                        },
                        "exampleSetFlag": false
                      },
                      "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                      "schema": {
                        "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithOptionalParameters",
                        "exampleSetFlag": false
                      },
                      "definitions": {
                        "io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithOptionalParameters": {
                          "required": [
                            "ctorOptional",
                            "ctorRequired"
                          ],
                          "type": "object",
                          "properties": {
                            "ctorOptional": {
                              "type": "string",
                              "exampleSetFlag": false
                            },
                            "ctorOptionalNullable": {
                              "type": "string",
                              "exampleSetFlag": false
                            },
                            "ctorRequired": {
                              "type": "string",
                              "exampleSetFlag": false
                            },
                            "ctorRequiredNullable": {
                              "type": "string",
                              "exampleSetFlag": false
                            }
                          },
                          "exampleSetFlag": false
                        }
                      }
                    }
                """.trimIndent(),
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
                          "type": "string",
                          "exampleSetFlag": false
                        },
                        "ctorOptionalNullable": {
                          "type": "string",
                          "exampleSetFlag": false
                        },
                        "ctorRequired": {
                          "type": "string",
                          "exampleSetFlag": false
                        },
                        "ctorRequiredNullable": {
                          "type": "string",
                          "exampleSetFlag": false
                        }
                      },
                      "exampleSetFlag": false
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                      "schema": {
                        "required": [
                          "ctorRequired"
                        ],
                        "type": "object",
                        "properties": {
                          "ctorOptional": {
                            "type": "string",
                            "exampleSetFlag": false
                          },
                          "ctorOptionalNullable": {
                            "type": "string",
                            "exampleSetFlag": false
                          },
                          "ctorRequired": {
                            "type": "string",
                            "exampleSetFlag": false
                          },
                          "ctorRequiredNullable": {
                            "type": "string",
                            "exampleSetFlag": false
                          }
                        },
                        "exampleSetFlag": false
                      },
                      "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                      "schema": {
                        "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithOptionalParameters",
                        "exampleSetFlag": false
                      },
                      "definitions": {
                        "io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithOptionalParameters": {
                          "required": [
                            "ctorRequired"
                          ],
                          "type": "object",
                          "properties": {
                            "ctorOptional": {
                              "type": "string",
                              "exampleSetFlag": false
                            },
                            "ctorOptionalNullable": {
                              "type": "string",
                              "exampleSetFlag": false
                            },
                            "ctorRequired": {
                              "type": "string",
                              "exampleSetFlag": false
                            },
                            "ctorRequiredNullable": {
                              "type": "string",
                              "exampleSetFlag": false
                            }
                          },
                          "exampleSetFlag": false
                        }
                      }
                    }
                """.trimIndent(),
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
                          "format": "int32",
                          "exampleSetFlag": false
                        },
                        "someText": {
                          "type": "string",
                          "exampleSetFlag": false
                        }
                      },
                      "exampleSetFlag": false
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                      "schema": {
                        "required": [
                          "myValue",
                          "someText"
                        ],
                        "type": "object",
                        "properties": {
                          "myValue": {
                            "type": "integer",
                            "format": "int32",
                            "exampleSetFlag": false
                          },
                          "someText": {
                            "type": "string",
                            "exampleSetFlag": false
                          }
                        },
                        "exampleSetFlag": false
                      },
                      "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                      "schema": {
                        "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithValueClass",
                        "exampleSetFlag": false
                      },
                      "definitions": {
                        "io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithValueClass": {
                          "required": [
                            "myValue",
                            "someText"
                          ],
                          "type": "object",
                          "properties": {
                            "myValue": {
                              "type": "integer",
                              "format": "int32",
                              "exampleSetFlag": false
                            },
                            "someText": {
                              "type": "string",
                              "exampleSetFlag": false
                            }
                          },
                          "exampleSetFlag": false
                        }
                      }
                    }
                """.trimIndent(),
            ),
        )

    }

}
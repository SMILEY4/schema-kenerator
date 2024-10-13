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
                            .let { SwaggerSchemaTitleStep(TitleBuilder.BUILDER_SIMPLE).process(it) }
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
                        "types": ["object"],
                        "exampleSetFlag": false
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "schema": {
                            "types": ["object"],
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "schema": {
                            "types": ["object"],
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
                        "types": ["integer"],
                        "maximum": 255,
                        "minimum": 0,
                        "exampleSetFlag": false
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "schema": {
                            "types": ["integer"],
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
                            "types": ["integer"],
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
                        "types": ["integer"],
                        "format": "int32",
                        "exampleSetFlag": false
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "schema": {
                            "types": ["integer"],
                            "format": "int32",
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "schema": {
                            "types": ["integer"],
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
                        "types": ["number"],
                        "format": "float",
                        "exampleSetFlag": false
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "schema": {
                        "types": ["number"],
                        "format": "float",
                        "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "schema": {
                            "types": ["number"],
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
                         "types": ["boolean"],
                         "exampleSetFlag": false
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "schema": {
                            "types": ["boolean"],
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "schema": {
                            "types": ["boolean"],
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
                        "types": ["string"],
                        "exampleSetFlag": false
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "schema": {
                            "types": ["string"],
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "schema": {
                            "types": ["string"],
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
                        "types": ["object"],
                        "properties": {},
                        "exampleSetFlag": false
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "schema": {
                            "types": ["object"],
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
                                "types": ["object"],
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
                        "types": ["object"],
                        "properties": {},
                        "exampleSetFlag": false
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "schema": {
                            "types": ["object"],
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
                                "types": ["object"],
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
                        "types": ["object"],
                        "properties": {
                            "someBoolList": {
                                "types": ["array"],
                                "exampleSetFlag": false,
                                "items": {
                                  "types": ["boolean"],
                                  "exampleSetFlag": false
                                }
                            },
                            "someNullableInt": {
                                "types": ["integer", "null"],
                                "format": "int32",
                                "exampleSetFlag": false
                            },
                            "someString": {
                                "types": ["string"],
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
                            "types": ["object"],
                            "properties": {
                                "someBoolList": {
                                    "types": ["array"],
                                    "exampleSetFlag": false,
                                    "items": {
                                      "types": ["boolean"],
                                      "exampleSetFlag": false
                                    }
                                },
                                "someNullableInt": {
                                    "types": ["integer", "null"],
                                    "format": "int32",
                                    "exampleSetFlag": false
                                },
                                "someString": {
                                    "types": ["string"],
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
                                "types": ["object"],
                                "properties": {
                                    "someBoolList": {
                                        "types": ["array"],
                                        "exampleSetFlag": false,
                                        "items": {
                                            "types": ["boolean"],
                                            "exampleSetFlag": false
                                        }
                                    },
                                    "someNullableInt": {
                                        "types": ["integer", "null"],
                                        "format": "int32",
                                        "exampleSetFlag": false
                                    },
                                    "someString": {
                                        "types": ["string"],
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
                        "types": ["object"],
                        "exampleSetFlag": false
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "schema": {
                            "types": ["object"],
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "schema": {
                            "types": ["object"],
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
                        "types": ["object"],
                        "exampleSetFlag": false
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "schema": {
                            "types": ["object"],
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "schema": {
                            "types": ["object"],
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
                        "types": ["object"],
                        "exampleSetFlag": false
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "schema": {
                            "types": ["object"],
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "schema": {
                            "types": ["object"],
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
                                "types": ["object"],
                                "properties": {
                                    "a": {
                                          "types": ["integer"],
                                          "format": "int32",
                                          "exampleSetFlag": false
                                        },
                                    "sealedValue": {
                                        "types": ["string"],
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
                                "types": ["object"],
                                "properties": {
                                    "b": {
                                        "types": ["integer"],
                                        "format": "int32",
                                        "exampleSetFlag": false
                                    },
                                    "sealedValue": {
                                        "types": ["string"],
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
                                "types": ["object"],
                                "properties": {
                                    "a": {
                                        "types": ["integer"],
                                        "format": "int32",
                                        "exampleSetFlag": false
                                    },
                                    "sealedValue": {
                                        "types": ["string"],
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
                                "types": ["object"],
                                "properties": {
                                    "b": {
                                        "types": ["integer"],
                                        "format": "int32",
                                        "exampleSetFlag": false
                                    },
                                    "sealedValue": {
                                        "types": ["string"],
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
                                "types": ["object"],
                                "properties": {
                                    "a": {
                                        "types": ["integer"],
                                        "format": "int32",
                                        "exampleSetFlag": false
                                    },
                                    "sealedValue": {
                                        "types": ["string"],
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
                                "types": ["object"],
                                "properties": {
                                    "b": {
                                        "types": ["integer"],
                                        "format": "int32",
                                        "exampleSetFlag": false
                                    },
                                    "sealedValue": {
                                        "types": ["string"],
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
                        "types": ["object"],
                        "properties": {
                            "a": {
                                "types": ["integer"],
                                "format": "int32",
                                "exampleSetFlag": false
                            },
                            "sealedValue": {
                                "types": ["string"],
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
                            "types": ["object"],
                            "properties": {
                                "a": {
                                    "types": ["integer"],
                                    "format": "int32",
                                    "exampleSetFlag": false
                                },
                                "sealedValue": {
                                    "types": ["string"],
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
                                "types": ["object"],
                                "properties": {
                                    "a": {
                                        "types": ["integer"],
                                        "format": "int32",
                                        "exampleSetFlag": false
                                    },
                                    "sealedValue": {
                                        "types": ["string"],
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
                        "types": ["object"],
                        "properties": {
                            "value": {
                                "types": ["string"],
                                "description": "field description",
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
                            "types": ["object"],
                            "properties": {
                                "value": {
                                    "types": ["string"],
                                    "description": "field description",
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
                                "types": ["object"],
                                "properties": {
                                    "value": {
                                        "types": ["string"],
                                        "description": "field description",
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
                        "types": ["object"],
                        "properties": {
                            "nested": {
                                "title": "NestedClass",
                                "required": [
                                    "text"
                                ],
                                "types": ["object"],
                                "properties": {
                                    "text": {
                                        "title": "String",
                                        "types": ["string"],
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
                            "types": ["object"],
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
                                "types": ["object"],
                                "properties": {
                                    "text": {
                                        "title": "String",
                                        "types": ["string"],
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
                                "types": ["object"],
                                "properties": {
                                    "text": {
                                        "title": "String",
                                        "types": ["string"],
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
                                "types": ["object"],
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
                      "types": ["object"],
                      "properties": {
                        "someList": {
                          "types": ["array"],
                          "exampleSetFlag": false,
                          "items": {
                            "types": ["string"],
                            "exampleSetFlag": false
                          }
                        },
                        "someSet": {
                          "types": ["array"],
                          "exampleSetFlag": false,
                          "items": {
                            "types": ["string"],
                            "exampleSetFlag": false
                          }
                        },
                        "someMap": {
                          "types": ["object"],
                          "additionalProperties": {
                            "types": ["integer"],
                            "format": "int32",
                            "exampleSetFlag": false
                          },
                          "exampleSetFlag": false
                        },
                        "someArray": {
                          "types": ["array"],
                          "exampleSetFlag": false,
                          "items": {
                            "types": ["integer"],
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
                        "types": ["object"],
                        "properties": {
                          "someList": {
                            "types": ["array"],
                            "exampleSetFlag": false,
                            "items": {
                              "types": ["string"],
                              "exampleSetFlag": false
                            }
                          },
                          "someSet": {
                            "types": ["array"],
                            "exampleSetFlag": false,
                            "items": {
                              "types": ["string"],
                              "exampleSetFlag": false
                            }
                          },
                          "someMap": {
                            "types": ["object"],
                            "additionalProperties": {
                              "types": ["integer"],
                              "format": "int32",
                              "exampleSetFlag": false
                            },
                            "exampleSetFlag": false
                          },
                          "someArray": {
                            "types": ["array"],
                            "exampleSetFlag": false,
                            "items": {
                              "types": ["integer"],
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
                          "types": ["object"],
                          "properties": {
                            "someList": {
                              "types": ["array"],
                              "exampleSetFlag": false,
                              "items": {
                                "types": ["string"],
                                "exampleSetFlag": false
                              }
                            },
                            "someSet": {
                              "types": ["array"],
                              "exampleSetFlag": false,
                              "items": {
                                "types": ["string"],
                                "exampleSetFlag": false
                              }
                            },
                            "someMap": {
                              "types": ["object"],
                              "additionalProperties": {
                                "types": ["integer"],
                                "format": "int32",
                                "exampleSetFlag": false
                              },
                              "exampleSetFlag": false
                            },
                            "someArray": {
                              "types": ["array"],
                              "exampleSetFlag": false,
                              "items": {
                                "types": ["integer"],
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
                      "types": ["object"],
                      "properties": {
                        "self": {
                          "types": ["object", "null"],
                          "properties": {
                            "self": {
                              "types": ["object", "null"],
                              "properties": {
                                "self": {
                                  "types": ["object", "null"],
                                  "properties": {
                                    "self": {
                                      "types": ["object", "null"],
                                      "properties": {
                                        "self": {
                                          "types": ["object", "null"],
                                          "properties": {
                                            "self": {
                                              "types": ["object", "null"],
                                              "properties": {
                                                "self": {
                                                  "types": ["object", "null"],
                                                  "properties": {
                                                    "self": {
                                                      "types": ["object", "null"],
                                                      "properties": {
                                                        "self": {
                                                          "types": ["object", "null"],
                                                          "properties": {
                                                            "self": {
                                                              "types": ["object", "null"],
                                                              "properties": {
                                                                "self": {
                                                                  "types": ["object", "null"],
                                                                  "properties": {
                                                                    "self": {
                                                                      "types": ["object", "null"],
                                                                      "properties": {
                                                                        "self": {
                                                                          "types": ["object", "null"],
                                                                          "properties": {
                                                                            "self": {
                                                                              "types": ["object", "null"],
                                                                              "properties": {
                                                                                "self": {
                                                                                  "types": ["object", "null"],
                                                                                  "properties": {
                                                                                    "self": {
                                                                                      "types": ["object", "null"],
                                                                                      "properties": {
                                                                                        "self": {
                                                                                          "types": ["object", "null"],
                                                                                          "properties": {
                                                                                            "self": {
                                                                                              "types": ["object", "null"],
                                                                                              "properties": {
                                                                                                "self": {
                                                                                                  "types": ["object", "null"],
                                                                                                  "properties": {
                                                                                                    "self": {
                                                                                                      "types": ["object", "null"],
                                                                                                      "properties": {
                                                                                                        "self": {
                                                                                                          "types": ["object", "null"],
                                                                                                          "properties": {
                                                                                                            "self": {
                                                                                                              "types": ["object", "null"],
                                                                                                              "properties": {
                                                                                                                "self": {
                                                                                                                  "types": ["object", "null"],
                                                                                                                  "properties": {
                                                                                                                    "self": {
                                                                                                                      "types": ["object", "null"],
                                                                                                                      "properties": {
                                                                                                                        "self": {
                                                                                                                          "types": ["object", "null"],
                                                                                                                          "properties": {
                                                                                                                            "self": {
                                                                                                                              "types": ["object", "null"],
                                                                                                                              "properties": {
                                                                                                                                "self": {
                                                                                                                                  "types": ["object", "null"],
                                                                                                                                  "properties": {
                                                                                                                                    "self": {
                                                                                                                                      "types": ["object", "null"],
                                                                                                                                      "properties": {
                                                                                                                                        "self": {
                                                                                                                                          "types": ["object", "null"],
                                                                                                                                          "properties": {
                                                                                                                                            "self": {
                                                                                                                                              "types": ["object", "null"],
                                                                                                                                              "properties": {
                                                                                                                                                "self": {
                                                                                                                                                  "types": ["object", "null"],
                                                                                                                                                  "properties": {
                                                                                                                                                    "self": {
                                                                                                                                                      "types": ["object", "null"],
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
                        "types": ["object"],
                        "properties": {
                          "self": {
                            "exampleSetFlag": false,
                            "oneOf": [
                              {
                                "types": ["null"],
                                "exampleSetFlag": false
                              },
                              {
                                "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.kotlinx.ClassDirectSelfReferencing",
                                "exampleSetFlag": false
                              }
                            ]
                          }
                        },
                        "exampleSetFlag": false
                      },
                      "definitions": {
                        "io.github.smiley4.schemakenerator.test.models.kotlinx.ClassDirectSelfReferencing": {
                          "types": ["object"],
                          "properties": {
                            "self": {
                              "exampleSetFlag": false,
                              "oneOf": [
                                {
                                  "types": ["null"],
                                  "exampleSetFlag": false
                                },
                                {
                                  "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.kotlinx.ClassDirectSelfReferencing",
                                  "exampleSetFlag": false
                                }
                              ]
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
                          "types": ["object"],
                          "properties": {
                            "self": {
                              "exampleSetFlag": false,
                              "oneOf": [
                                {
                                  "types": ["null"],
                                  "exampleSetFlag": false
                                },
                                {
                                  "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.kotlinx.ClassDirectSelfReferencing",
                                  "exampleSetFlag": false
                                }
                              ]
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
                      "types": ["object"],
                      "properties": {
                        "ctorOptional": {
                          "types": ["string"],
                          "exampleSetFlag": false
                        },
                        "ctorOptionalNullable": {
                          "types": ["string", "null"],
                          "exampleSetFlag": false
                        },
                        "ctorRequired": {
                          "types": ["string"],
                          "exampleSetFlag": false
                        },
                        "ctorRequiredNullable": {
                          "types": ["string", "null"],
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
                        "types": ["object"],
                        "properties": {
                          "ctorOptional": {
                            "types": ["string"],
                            "exampleSetFlag": false
                          },
                          "ctorOptionalNullable": {
                            "types": ["string", "null"],
                            "exampleSetFlag": false
                          },
                          "ctorRequired": {
                            "types": ["string"],
                            "exampleSetFlag": false
                          },
                          "ctorRequiredNullable": {
                            "types": ["string", "null"],
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
                          "types": ["object"],
                          "properties": {
                            "ctorOptional": {
                              "types": ["string"],
                              "exampleSetFlag": false
                            },
                            "ctorOptionalNullable": {
                              "types": ["string", "null"],
                              "exampleSetFlag": false
                            },
                            "ctorRequired": {
                              "types": ["string"],
                              "exampleSetFlag": false
                            },
                            "ctorRequiredNullable": {
                              "types": ["string", "null"],
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
                      "types": ["object"],
                      "properties": {
                        "ctorOptional": {
                          "types": ["string"],
                          "exampleSetFlag": false
                        },
                        "ctorOptionalNullable": {
                          "types": ["string", "null"],
                          "exampleSetFlag": false
                        },
                        "ctorRequired": {
                          "types": ["string"],
                          "exampleSetFlag": false
                        },
                        "ctorRequiredNullable": {
                          "types": ["string", "null"],
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
                        "types": ["object"],
                        "properties": {
                          "ctorOptional": {
                            "types": ["string"],
                            "exampleSetFlag": false
                          },
                          "ctorOptionalNullable": {
                            "types": ["string", "null"],
                            "exampleSetFlag": false
                          },
                          "ctorRequired": {
                            "types": ["string"],
                            "exampleSetFlag": false
                          },
                          "ctorRequiredNullable": {
                            "types": ["string", "null"],
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
                          "types": ["object"],
                          "properties": {
                            "ctorOptional": {
                              "types": ["string"],
                              "exampleSetFlag": false
                            },
                            "ctorOptionalNullable": {
                              "types": ["string", "null"],
                              "exampleSetFlag": false
                            },
                            "ctorRequired": {
                              "types": ["string"],
                              "exampleSetFlag": false
                            },
                            "ctorRequiredNullable": {
                              "types": ["string", "null"],
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
                      "types": ["object"],
                      "properties": {
                        "myValue": {
                          "types": ["integer"],
                          "format": "int32",
                          "exampleSetFlag": false
                        },
                        "someText": {
                          "types": ["string"],
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
                        "types": ["object"],
                        "properties": {
                          "myValue": {
                            "types": ["integer"],
                            "format": "int32",
                            "exampleSetFlag": false
                          },
                          "someText": {
                            "types": ["string"],
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
                          "types": ["object"],
                          "properties": {
                            "myValue": {
                              "types": ["integer"],
                              "format": "int32",
                              "exampleSetFlag": false
                            },
                            "someText": {
                              "types": ["string"],
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
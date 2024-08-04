package io.github.smiley4.schemakenerator.test

import io.github.smiley4.schemakenerator.core.steps.RenameTypesStep
import io.github.smiley4.schemakenerator.jsonschema.data.RefType
import io.github.smiley4.schemakenerator.jsonschema.data.TitleType
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonObject
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.obj
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaAutoTitleStep
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaCompileInlineStep
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaCompileReferenceRootStep
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaGenerationStep
import io.github.smiley4.schemakenerator.reflection.steps.ReflectionTypeProcessingStep
import io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWIthDifferentGenerics
import io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithSimpleFields
import io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithValueClass
import io.github.smiley4.schemakenerator.test.models.kotlinx.SealedClass
import io.github.smiley4.schemakenerator.test.models.kotlinx.TestEnum
import io.github.smiley4.schemakenerator.test.models.reflection.RenamedTestData
import io.kotest.assertions.json.ArrayOrder
import io.kotest.assertions.json.FieldComparison
import io.kotest.assertions.json.NumberFormat
import io.kotest.assertions.json.PropertyOrder
import io.kotest.assertions.json.TypeCoercion
import io.kotest.assertions.json.shouldEqualJson
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.WithDataTestName
import io.kotest.datatest.withData
import kotlin.reflect.KType
import kotlin.reflect.typeOf

@Suppress("ClassName")
class Reflection_JsonSchema_TitleAppender_Tests : FunSpec({

    context("full title") {
        withData(TEST_DATA) { data ->

            val additionalIds = mutableListOf<String>()

            val schema = data.type
                .let { ReflectionTypeProcessingStep().process(it) }
                .let { RenameTypesStep().process(it) }
                .also { schema ->
                    if (schema.data.id.additionalId != null) {
                        additionalIds.add(schema.data.id.additionalId!!)
                    }
                    schema.supporting.forEach {
                        if (it.id.additionalId != null) {
                            additionalIds.add(it.id.additionalId!!)
                        }
                    }
                }
                .let { JsonSchemaGenerationStep().generate(it) }
                .let { JsonSchemaAutoTitleStep(TitleType.FULL).process(it) }
                .let { JsonSchemaCompileInlineStep().compile(it) }

            schema.json.prettyPrint()
                .let {
                    var jsonStr = it
                    additionalIds.forEachIndexed { index, addId ->
                        jsonStr = jsonStr.replace("#$addId", "#$index")
                    }
                    jsonStr
                }
                .shouldEqualJson {
                    propertyOrder = PropertyOrder.Lenient
                    arrayOrder = ArrayOrder.Lenient
                    fieldComparison = FieldComparison.Strict
                    numberFormat = NumberFormat.Lenient
                    typeCoercion = TypeCoercion.Disabled
                    data.expectedResultFullTitle
                }
        }
    }

    context("simple title") {
        withData(TEST_DATA) { data ->

            val additionalIds = mutableListOf<String>()

            val schema = data.type
                .let { ReflectionTypeProcessingStep().process(it) }
                .let { RenameTypesStep().process(it) }
                .also { schema ->
                    if (schema.data.id.additionalId != null) {
                        additionalIds.add(schema.data.id.additionalId!!)
                    }
                    schema.supporting.forEach {
                        if (it.id.additionalId != null) {
                            additionalIds.add(it.id.additionalId!!)
                        }
                    }
                }
                .let { JsonSchemaGenerationStep().generate(it) }
                .let { JsonSchemaAutoTitleStep(TitleType.SIMPLE).process(it) }
                .let { JsonSchemaCompileInlineStep().compile(it) }

            schema.json.prettyPrint()
                .let {
                    var jsonStr = it
                    additionalIds.forEachIndexed { index, addId ->
                        jsonStr = jsonStr.replace("#$addId", "#$index")
                    }
                    jsonStr
                }
                .shouldEqualJson {
                    propertyOrder = PropertyOrder.Lenient
                    arrayOrder = ArrayOrder.Lenient
                    fieldComparison = FieldComparison.Strict
                    numberFormat = NumberFormat.Lenient
                    typeCoercion = TypeCoercion.Disabled
                    data.expectedResultSimpleTitle
                }
        }
    }

    context("simple title, simple references ") {
        withData(TEST_DATA) { data ->

            val additionalIds = mutableListOf<String>()

            val schema = data.type
                .let { ReflectionTypeProcessingStep().process(it) }
                .let { RenameTypesStep().process(it) }
                .also { schema ->
                    if (schema.data.id.additionalId != null) {
                        additionalIds.add(schema.data.id.additionalId!!)
                    }
                    schema.supporting.forEach {
                        if (it.id.additionalId != null) {
                            additionalIds.add(it.id.additionalId!!)
                        }
                    }
                }
                .let { JsonSchemaGenerationStep().generate(it) }
                .let { JsonSchemaAutoTitleStep(TitleType.SIMPLE).process(it) }
                .let { JsonSchemaCompileReferenceRootStep(RefType.SIMPLE).compile(it) }
                .also {
                    if (it.definitions.isNotEmpty()) {
                        (it.json as JsonObject).properties["definitions"] = obj {
                            it.definitions.forEach { (k, v) ->
                                k to v
                            }
                        }
                    }
                }

            schema.json.prettyPrint()
                .let {
                    var jsonStr = it
                    additionalIds.forEachIndexed { index, addId ->
                        jsonStr = jsonStr.replace("#$addId", "#$index")
                    }
                    jsonStr
                }
                .shouldEqualJson {
                    propertyOrder = PropertyOrder.Lenient
                    arrayOrder = ArrayOrder.Lenient
                    fieldComparison = FieldComparison.Strict
                    numberFormat = NumberFormat.Lenient
                    typeCoercion = TypeCoercion.Disabled
                    data.expectedResultSimpleTitleReferencing
                }
        }
    }

}) {

    companion object {

        private class TestData(
            val testName: String,
            val type: KType,
            val expectedResultFullTitle: String,
            val expectedResultSimpleTitle: String,
            val expectedResultSimpleTitleReferencing: String,
        ) : WithDataTestName {
            override fun dataTestName() = testName
        }

        private val TEST_DATA = listOf(
            TestData(
                type = typeOf<Any>(),
                testName = "any",
                expectedResultFullTitle = """
                    {
                        "type": "object",
                        "title": "kotlin.Any"
                    }
                """.trimIndent(),
                expectedResultSimpleTitle = """
                    {
                        "type": "object",
                        "title": "Any"
                    }
                """.trimIndent(),
                expectedResultSimpleTitleReferencing = """
                    {
                        "type": "object",
                        "title": "Any"
                    }
                """.trimIndent(),
            ),
            TestData(
                type = typeOf<UByte>(),
                testName = "ubyte",
                expectedResultFullTitle = """
                    {
                        "type": "integer",
                        "minimum": 0,
                        "maximum": 255,
                        "title": "kotlin.UByte"
                    }
                """.trimIndent(),
                expectedResultSimpleTitle = """
                    {
                        "type": "integer",
                        "minimum": 0,
                        "maximum": 255,
                        "title": "UByte"
                    }
                """.trimIndent(),
                expectedResultSimpleTitleReferencing = """
                      {
                        "type": "integer",
                        "minimum": 0,
                        "maximum": 255,
                        "title": "UByte"
                    }
                """.trimIndent(),
            ),
            TestData(
                type = typeOf<ClassWithSimpleFields>(),
                testName = "class with simple fields",
                expectedResultFullTitle = """
                    {
                        "type": "object",
                        "required": ["someString", "someBoolList"],
                        "properties": {
                            "someString": {
                                "type": "string",
                                "title": "kotlin.String"
                            },
                            "someNullableInt": {
                                "type": "integer",
                                "minimum": -2147483648,
                                "maximum": 2147483647,
                                "title": "kotlin.Int"
                            },
                            "someBoolList": {
                                "type": "array",
                                "items": {
                                    "type": "boolean",
                                    "title": "kotlin.Boolean"
                                },
                                "title": "kotlin.collections.List<kotlin.Boolean>"
                            }
                        },
                        "title": "io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithSimpleFields"
                    }
                """.trimIndent(),
                expectedResultSimpleTitle = """
                    {
                        "type": "object",
                        "required": ["someString", "someBoolList"],
                        "properties": {
                            "someString": {
                                "type": "string",
                                "title": "String"
                            },
                            "someNullableInt": {
                                "type": "integer",
                                "minimum": -2147483648,
                                "maximum": 2147483647,
                                "title": "Int"
                            },
                            "someBoolList": {
                                "type": "array",
                                "items": {
                                    "type": "boolean",
                                    "title": "Boolean"
                                },
                                "title": "List<Boolean>"
                            }
                        },
                        "title": "ClassWithSimpleFields"
                    }
                """.trimIndent(),
                expectedResultSimpleTitleReferencing = """
                    {
                        "${'$'}ref": "#/definitions/ClassWithSimpleFields",
                        "definitions": {
                            "ClassWithSimpleFields": {
                                "type": "object",
                                "required": [
                                    "someString",
                                    "someBoolList"
                                ],
                                "properties": {
                                    "someString": {
                                        "type": "string",
                                        "title": "String"
                                    },
                                    "someNullableInt": {
                                        "type": "integer",
                                        "minimum": -2147483648,
                                        "maximum": 2147483647,
                                        "title": "Int"
                                    },
                                    "someBoolList": {
                                        "type": "array",
                                        "items": {
                                            "type": "boolean",
                                            "title": "Boolean"
                                        },
                                        "title": "List<Boolean>"
                                    }
                                },
                                "title": "ClassWithSimpleFields"
                            }
                        }
                    }
                """.trimIndent(),
            ),
            TestData(
                type = typeOf<TestEnum>(),
                testName = "enum",
                expectedResultFullTitle = """
                    {
                        "enum": [ "ONE", "TWO", "THREE"],
                        "title": "io.github.smiley4.schemakenerator.test.models.kotlinx.TestEnum"
                    }
                """.trimIndent(),
                expectedResultSimpleTitle = """
                    {
                        "enum": [ "ONE", "TWO", "THREE"],
                        "title": "TestEnum"
                    }
                """.trimIndent(),
                expectedResultSimpleTitleReferencing = """
                    {
                        "${'$'}ref": "#/definitions/TestEnum",
                        "definitions": {
                            "TestEnum": {
                                "enum": [
                                    "ONE",
                                    "TWO",
                                    "THREE"
                                ],
                                "title": "TestEnum"
                            }
                        }
                    }
                """.trimIndent(),
            ),
            TestData(
                type = typeOf<SealedClass>(),
                testName = "sealed class with subtypes",
                expectedResultFullTitle = """
                    {
                        "anyOf": [
                            {
                                "type": "object",
                                "required": ["a","sealedValue"],
                                "properties": {
                                    "a": {
                                        "type": "integer",
                                        "minimum": -2147483648,
                                        "maximum": 2147483647,
                                        "title": "kotlin.Int"
                                    },
                                    "sealedValue": {
                                        "type": "string",
                                        "title": "kotlin.String"
                                    }
                                },
                                "title": "io.github.smiley4.schemakenerator.test.models.kotlinx.SubClassA"
                            },
                            {
                                "type": "object",
                                "required": ["b", "sealedValue"],
                                "properties": {
                                    "b": {
                                        "type": "integer",
                                        "minimum": -2147483648,
                                        "maximum": 2147483647,
                                        "title": "kotlin.Int"
                                    },
                                    "sealedValue": {
                                        "type": "string",
                                        "title": "kotlin.String"
                                    }
                                },
                                "title": "io.github.smiley4.schemakenerator.test.models.kotlinx.SubClassB"
                            }
                        ],
                        "title": "io.github.smiley4.schemakenerator.test.models.kotlinx.SealedClass"
                    }
                """.trimIndent(),
                expectedResultSimpleTitle = """
                    {
                        "anyOf": [
                            {
                                "type": "object",
                                "required": ["a","sealedValue"],
                                "properties": {
                                    "a": {
                                        "type": "integer",
                                        "minimum": -2147483648,
                                        "maximum": 2147483647,
                                        "title": "Int"
                                    },
                                    "sealedValue": {
                                        "type": "string",
                                        "title": "String"
                                    }
                                },
                                "title": "SubClassA"
                            },
                            {
                                "type": "object",
                                "required": ["b", "sealedValue"],
                                "properties": {
                                    "b": {
                                        "type": "integer",
                                        "minimum": -2147483648,
                                        "maximum": 2147483647,
                                        "title": "Int"
                                    },
                                    "sealedValue": {
                                        "type": "string",
                                        "title": "String"
                                    }
                                },
                                "title": "SubClassB"
                            }
                        ],
                        "title": "SealedClass"
                    }
                """.trimIndent(),
                expectedResultSimpleTitleReferencing = """
                    {
                        "${'$'}ref": "#/definitions/SealedClass",
                        "definitions": {
                            "SubClassA": {
                                "type": "object",
                                "required": [
                                    "a",
                                    "sealedValue"
                                ],
                                "properties": {
                                    "a": {
                                        "type": "integer",
                                        "minimum": -2147483648,
                                        "maximum": 2147483647,
                                        "title": "Int"
                                    },
                                    "sealedValue": {
                                        "type": "string",
                                        "title": "String"
                                    }
                                },
                                "title": "SubClassA"
                            },
                            "SubClassB": {
                                "type": "object",
                                "required": [
                                    "b",
                                    "sealedValue"
                                ],
                                "properties": {
                                    "b": {
                                        "type": "integer",
                                        "minimum": -2147483648,
                                        "maximum": 2147483647,
                                        "title": "Int"
                                    },
                                    "sealedValue": {
                                        "type": "string",
                                        "title": "String"
                                    }
                                },
                                "title": "SubClassB"
                            },
                            "SealedClass": {
                                "anyOf": [
                                    {
                                        "${'$'}ref": "#/definitions/SubClassA"
                                    },
                                    {
                                        "${'$'}ref": "#/definitions/SubClassB"
                                    }
                                ],
                                "title": "SealedClass"
                            }
                        }
                    }
                """.trimIndent(),
            ),
            TestData(
                type = typeOf<ClassWIthDifferentGenerics>(),
                testName = "class with multiple different generics",
                expectedResultFullTitle = """
                    {
                        "type": "object",
                        "required": [
                            "valueInt",
                            "valueString"
                        ],
                        "properties": {
                            "valueInt": {
                                "type": "object",
                                "required": [
                                    "value"
                                ],
                                "properties": {
                                    "value": {
                                        "type": "integer",
                                        "minimum": -2147483648,
                                        "maximum": 2147483647,
                                        "title": "kotlin.Int"
                                    }
                                },
                                "title": "io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithGenericField<kotlin.Int>"
                            },
                            "valueString": {
                                "type": "object",
                                "required": [
                                    "value"
                                ],
                                "properties": {
                                    "value": {
                                        "type": "string",
                                        "title": "kotlin.String"
                                    }
                                },
                                "title": "io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithGenericField<kotlin.String>"
                            }
                        },
                        "title": "io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWIthDifferentGenerics"
                    }
                """.trimIndent(),
                expectedResultSimpleTitle = """
                    {
                        "type": "object",
                        "required": [
                            "valueInt",
                            "valueString"
                        ],
                        "properties": {
                            "valueInt": {
                                "type": "object",
                                "required": [
                                    "value"
                                ],
                                "properties": {
                                    "value": {
                                        "type": "integer",
                                        "minimum": -2147483648,
                                        "maximum": 2147483647,
                                        "title": "Int"
                                    }
                                },
                                "title": "ClassWithGenericField<Int>"
                            },
                            "valueString": {
                                "type": "object",
                                "required": [
                                    "value"
                                ],
                                "properties": {
                                    "value": {
                                        "type": "string",
                                        "title": "String"
                                    }
                                },
                                "title": "ClassWithGenericField<String>"
                            }
                        },
                        "title": "ClassWIthDifferentGenerics"
                    }
                """.trimIndent(),
                expectedResultSimpleTitleReferencing = """
                    {
                        "${'$'}ref": "#/definitions/ClassWIthDifferentGenerics",
                        "definitions": {
                            "ClassWithGenericField<Int>": {
                                "type": "object",
                                "required": [
                                    "value"
                                ],
                                "properties": {
                                    "value": {
                                        "type": "integer",
                                        "minimum": -2147483648,
                                        "maximum": 2147483647,
                                        "title": "Int"
                                    }
                                },
                                "title": "ClassWithGenericField<Int>"
                            },
                            "ClassWithGenericField<String>": {
                                "type": "object",
                                "required": [
                                    "value"
                                ],
                                "properties": {
                                    "value": {
                                        "type": "string",
                                        "title": "String"
                                    }
                                },
                                "title": "ClassWithGenericField<String>"
                            },
                            "ClassWIthDifferentGenerics": {
                                "type": "object",
                                "required": [
                                    "valueInt",
                                    "valueString"
                                ],
                                "properties": {
                                    "valueInt": {
                                        "${'$'}ref": "#/definitions/ClassWithGenericField<Int>"
                                    },
                                    "valueString": {
                                        "${'$'}ref": "#/definitions/ClassWithGenericField<String>"
                                    }
                                },
                                "title": "ClassWIthDifferentGenerics"
                            }
                        }
                    }
                """.trimIndent(),
            ),
            TestData(
                type = typeOf<RenamedTestData>(),
                testName = "class renamed with @SchemaName",
                expectedResultFullTitle = """
                    {
                        "type": "object",
                        "required": [
                        "nestedValue"
                        ],
                        "properties": {
                            "nestedValue": {
                                "type": "object",
                                "required": [
                                    "someValue"
                                ],
                                "properties": {
                                    "someValue": {
                                        "type": "string",
                                        "title": "kotlin.String"
                                    }
                                },
                                "title": "test.NestedData<kotlin.String>"
                            }
                        },
                        "title": "test.TestData"
                    }
                """.trimIndent(),
                expectedResultSimpleTitle = """
                    {
                        "type": "object",
                        "required": [
                            "nestedValue"
                        ],
                        "properties": {
                            "nestedValue": {
                                "type": "object",
                                "required": [
                                    "someValue"
                                ],
                                "properties": {
                                    "someValue": {
                                        "type": "string",
                                        "title": "String"
                                    }
                                },
                                "title": "NestedData<String>"
                            }
                        },
                        "title": "TestData"
                    }
                """.trimIndent(),
                expectedResultSimpleTitleReferencing = """
                    {
                        "${'$'}ref": "#/definitions/TestData",
                        "definitions": {
                            "NestedData<String>": {
                                "type": "object",
                                "required": [
                                    "someValue"
                                ],
                                "properties": {
                                    "someValue": {
                                        "type": "string",
                                        "title": "String"
                                    }
                                },
                                "title": "NestedData<String>"
                            },
                            "TestData": {
                                "type": "object",
                                "required": [
                                    "nestedValue"
                                ],
                                "properties": {
                                    "nestedValue": {
                                        "${'$'}ref": "#/definitions/NestedData<String>"
                                    }
                                },
                                "title": "TestData"
                            }
                        }
                    }
                """.trimIndent(),
            ),
            TestData(
                type = typeOf<ClassWithValueClass>(),
                testName = "inline value class",
                expectedResultFullTitle = """
                    {
                      "type": "object",
                      "required": [
                        "myValue",
                        "someText"
                      ],
                      "properties": {
                        "myValue": {
                          "type": "integer",
                          "minimum": -2147483648,
                          "maximum": 2147483647,
                          "title": "io.github.smiley4.schemakenerator.test.models.kotlinx.ValueClass"
                        },
                        "someText": {
                          "type": "string",
                          "title": "kotlin.String"
                        }
                      },
                      "title": "io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithValueClass"
                    }
                """.trimIndent(),
                expectedResultSimpleTitle = """
                    {
                      "type": "object",
                      "required": [
                        "myValue",
                        "someText"
                      ],
                      "properties": {
                        "myValue": {
                          "type": "integer",
                          "minimum": -2147483648,
                          "maximum": 2147483647,
                          "title": "ValueClass"
                        },
                        "someText": {
                          "type": "string",
                          "title": "String"
                        }
                      },
                      "title": "ClassWithValueClass"
                    }
                """.trimIndent(),
                expectedResultSimpleTitleReferencing = """
                    {
                      "${'$'}ref": "#/definitions/ClassWithValueClass",
                      "definitions": {
                        "ClassWithValueClass": {
                          "type": "object",
                          "required": [
                            "myValue",
                            "someText"
                          ],
                          "properties": {
                            "myValue": {
                              "type": "integer",
                              "minimum": -2147483648,
                              "maximum": 2147483647,
                              "title": "ValueClass"
                            },
                            "someText": {
                              "type": "string",
                              "title": "String"
                            }
                          },
                          "title": "ClassWithValueClass"
                        }
                      }
                    }
                """.trimIndent(),
            ),
        )

    }

}
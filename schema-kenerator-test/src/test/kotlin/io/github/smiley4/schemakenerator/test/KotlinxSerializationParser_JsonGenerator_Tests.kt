package io.github.smiley4.schemakenerator.test

import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaCompiler
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaGenerator
import io.github.smiley4.schemakenerator.jsonschema.json.JsonObject
import io.github.smiley4.schemakenerator.jsonschema.json.obj
import io.github.smiley4.schemakenerator.reflection.getKType
import io.github.smiley4.schemakenerator.serialization.KotlinxSerializationTypeProcessor
import io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWIthDifferentGenerics
import io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithDeepGeneric
import io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithGenericField
import io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithSimpleFields
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
import kotlin.reflect.KType

@Suppress("ClassName")
class KotlinxSerializationParser_JsonGenerator_Tests : FunSpec({

    context("generator: inlining") {
        withData(TEST_DATA) { data ->

            val schema = listOf(data.type)
                .let { KotlinxSerializationTypeProcessor().process(it) }
                .let { JsonSchemaGenerator().generate(it) }
                .let { JsonSchemaCompiler().compileInlining(it) }
                .first()

            schema.json.prettyPrint().shouldEqualJson {
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

            val additionalIds = mutableListOf<String>()

            val schema = listOf(data.type)
                .let { KotlinxSerializationTypeProcessor().process(it) }
                .onEach { schema ->
                    if (schema.id.additionalId != null) {
                        additionalIds.add(schema.id.additionalId!!)
                    }
                }
                .let { JsonSchemaGenerator().generate(it) }
                .let { JsonSchemaCompiler().compileReferencing(it) }
                .first()
                .also {
                    if (it.definitions.isNotEmpty()) {
                        (it.json as JsonObject).properties["definitions"] = obj {
                            it.definitions.forEach { (k, v) ->
                                k.full() to v
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
                    data.expectedResultReferencing
                }

        }
    }

    context("generator: referencing-root") {
        withData(TEST_DATA) { data ->

            val additionalIds = mutableListOf<String>()

            val schema = listOf(data.type)
                .let { KotlinxSerializationTypeProcessor().process(it) }
                .onEach { schema ->
                    if (schema.id.additionalId != null) {
                        additionalIds.add(schema.id.additionalId!!)
                    }
                }
                .let { JsonSchemaGenerator().generate(it) }
                .let { JsonSchemaCompiler().compileReferencingRoot(it) }
                .first()
                .also {
                    if (it.definitions.isNotEmpty()) {
                        (it.json as JsonObject).properties["definitions"] = obj {
                            it.definitions.forEach { (k, v) ->
                                k.full() to v
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
                    data.expectedResultReferencingRoot
                }

        }
    }

}) {

    companion object {

        private class TestData(
            val testName: String,
            val type: KType,
            val expectedResultInlining: String,
            val expectedResultReferencing: String,
            val expectedResultReferencingRoot: String,
        ) : WithDataTestName {
            override fun dataTestName() = testName
        }

        private val TEST_DATA = listOf(
            TestData(
                type = getKType<Any>(),
                testName = "any",
                expectedResultInlining = """
                    {
                        "type": "object"
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "type": "object"
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "type": "object"
                    }
                """.trimIndent(),
            ),
            TestData(
                type = getKType<UByte>(),
                testName = "ubyte",
                expectedResultInlining = """
                    {
                        "type": "integer",
                        "minimum": 0,
                        "maximum": 255
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "type": "integer",
                        "minimum": 0,
                        "maximum": 255
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "type": "integer",
                        "minimum": 0,
                        "maximum": 255
                    }
                """.trimIndent(),
            ),
            TestData(
                type = getKType<Int>(),
                testName = "int",
                expectedResultInlining = """
                    {
                        "type": "integer",
                        "minimum": -2147483648,
                        "maximum": 2147483647
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "type": "integer",
                        "minimum": -2147483648,
                        "maximum": 2147483647
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "type": "integer",
                        "minimum": -2147483648,
                        "maximum": 2147483647
                    }
                """.trimIndent()
            ),
            TestData(
                type = getKType<Float>(),
                testName = "float",
                expectedResultInlining = """
                    {
                        "type": "number",
                        "minimum": 1.4E-45,
                        "maximum": 3.4028235E38
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "type": "number",
                        "minimum": 1.4E-45,
                        "maximum": 3.4028235E38
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "type": "number",
                        "minimum": 1.4E-45,
                        "maximum": 3.4028235E38
                    }
                """.trimIndent(),
            ),
            TestData(
                type = getKType<Boolean>(),
                testName = "boolean",
                expectedResultInlining = """
                    {
                        "type": "boolean"
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "type": "boolean"
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "type": "boolean"
                    }
                """.trimIndent(),
            ),
            TestData(
                type = getKType<String>(),
                testName = "string",
                expectedResultInlining = """
                    {
                        "type": "string"
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "type": "string"
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "type": "string"
                    }
                """.trimIndent(),
            ),
            TestData(
                // top-level lists not directly supported: weird result
                type = getKType<List<String>>(),
                testName = "list of strings",
                expectedResultInlining = """
                    {
                        "type": "object",
                        "required": [],
                        "properties": {}
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "type": "object",
                        "required": [],
                        "properties": {}
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "${'$'}ref": "#/definitions/kotlinx.serialization.Polymorphic<List>",
                        "definitions": {
                            "kotlinx.serialization.Polymorphic<List>": {
                                "type": "object",
                                "required": [],
                                "properties": {}
                            }
                        }
                    }
                """.trimIndent(),
            ),
            // top-level maps not directly supported: weird result
            TestData(
                type = getKType<Map<String, Int>>(),
                testName = "map of strings to integers",
                expectedResultInlining = """
                    {
                        "type": "object",
                        "required": [],
                        "properties": {}
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "type": "object",
                        "required": [],
                        "properties": {}
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "${'$'}ref": "#/definitions/kotlinx.serialization.Polymorphic<Map>",
                        "definitions": {
                            "kotlinx.serialization.Polymorphic<Map>": {
                                "type": "object",
                                "required": [],
                                "properties": {}
                            }
                        }
                    }
                """.trimIndent(),
            ),
            TestData(
                type = getKType<ClassWithSimpleFields>(),
                testName = "class with simple fields",
                expectedResultInlining = """
                    {
                        "type": "object",
                        "required": ["someString", "someBoolList"],
                        "properties": {
                            "someString": {
                                "type": "string"
                            },
                            "someNullableInt": {
                                "type": "integer",
                                "minimum": -2147483648,
                                "maximum": 2147483647
                            },
                            "someBoolList": {
                                "type": "array",
                                "items": {
                                    "type": "boolean"
                                }
                            }
                        }
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "type": "object",
                        "required": ["someString", "someBoolList"],
                        "properties": {
                            "someString": {
                                "type": "string"
                            },
                            "someNullableInt": {
                                "type": "integer",
                                "minimum": -2147483648,
                                "maximum": 2147483647
                            },
                            "someBoolList": {
                                "type": "array",
                                "items": {
                                    "type": "boolean"
                                }
                            }
                        }
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "${'$'}ref": "#/definitions/io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithSimpleFields",
                        "definitions": {
                            "io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithSimpleFields": {
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
                                        "type": "integer",
                                        "minimum": -2147483648,
                                        "maximum": 2147483647
                                    },
                                    "someString": {
                                        "type": "string"
                                    }
                                }
                            }
                        }
                    }
                """.trimIndent(),
            ),
            TestData(
                type = getKType<TestEnum>(),
                testName = "enum",
                expectedResultInlining = """
                    {
                        "enum": [ "ONE", "TWO", "THREE"]
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "enum": [ "ONE", "TWO", "THREE"]
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                {
                    "${'$'}ref": "#/definitions/io.github.smiley4.schemakenerator.test.models.kotlinx.TestEnum",
                    "definitions": {
                        "io.github.smiley4.schemakenerator.test.models.kotlinx.TestEnum": {
                            "enum": [ "ONE", "TWO", "THREE" ]
                        }
                    }
                }
                """.trimIndent(),
            ),
            TestData(
                // generics not supported with kotlinx-serialization -> fallback to "any"-schema
                type = getKType<ClassWithGenericField<String>>(),
                testName = "class with defined generic field",
                expectedResultInlining = """
                    {
                      "type": "object"
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                      "type": "object"
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "type": "object"
                    }
                """.trimIndent(),
            ),
            TestData(
                // generics not supported with kotlinx-serialization -> fallback to "any"-schema
                type = getKType<ClassWithGenericField<*>>(),
                testName = "class with wildcard generic field",
                expectedResultInlining = """
                    {
                      "type": "object"
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                      "type": "object"
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "type": "object"
                    }
                """.trimIndent(),
            ),
            TestData(
                // generics not supported with kotlinx-serialization -> fallback to "any"-schema
                type = getKType<ClassWithDeepGeneric<String>>(),
                testName = "class with deep generic field",
                expectedResultInlining = """
                    {
                      "type": "object"
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                      "type": "object"
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "type": "object"
                    }
                """.trimIndent(),
            ),
            TestData(
                type = getKType<SealedClass>(),
                testName = "sealed class with subtypes",
                expectedResultInlining = """
                    {
                        "anyOf": [
                            {
                                "type": "object",
                                "required": ["a","sealedValue"],
                                "properties": {
                                    "a": {
                                        "type": "integer",
                                        "minimum": -2147483648,
                                        "maximum": 2147483647
                                    },
                                    "sealedValue": {
                                        "type": "string"
                                    }
                                }
                            },
                            {
                                "type": "object",
                                "required": ["b", "sealedValue"],
                                "properties": {
                                    "b": {
                                        "type": "integer",
                                        "minimum": -2147483648,
                                        "maximum": 2147483647
                                    },
                                    "sealedValue": {
                                        "type": "string"
                                    }
                                }
                            }
                        ]
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "anyOf": [
                            {
                                "${'$'}ref": "#/definitions/io.github.smiley4.schemakenerator.test.models.kotlinx.SubClassA"
                            },
                            {
                                "${'$'}ref": "#/definitions/io.github.smiley4.schemakenerator.test.models.kotlinx.SubClassB"
                            }
                        ],
                        "definitions": {
                            "io.github.smiley4.schemakenerator.test.models.kotlinx.SubClassA": {
                                "type": "object",
                                "required": ["a", "sealedValue"],
                                "properties": {
                                    "a": {
                                        "type": "integer",
                                        "minimum": -2147483648,
                                        "maximum": 2147483647
                                    },
                                    "sealedValue": {
                                        "type": "string"
                                    }
                                }
                            },
                            "io.github.smiley4.schemakenerator.test.models.kotlinx.SubClassB": {
                                "type": "object",
                                "required": ["b", "sealedValue"],
                                "properties": {
                                    "b": {
                                        "type": "integer",
                                        "minimum": -2147483648,
                                        "maximum": 2147483647
                                    },
                                    "sealedValue": {
                                        "type": "string"
                                    }
                                }
                            }
                        }
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "${'$'}ref": "#/definitions/io.github.smiley4.schemakenerator.test.models.kotlinx.SealedClass",
                        "definitions": {
                            "io.github.smiley4.schemakenerator.test.models.kotlinx.SealedClass": {
                                "anyOf": [
                                    {
                                        "${'$'}ref": "#/definitions/io.github.smiley4.schemakenerator.test.models.kotlinx.SubClassA"
                                    },
                                    {
                                        "${'$'}ref": "#/definitions/io.github.smiley4.schemakenerator.test.models.kotlinx.SubClassB"
                                    }
                                ]
                            },
                            "io.github.smiley4.schemakenerator.test.models.kotlinx.SubClassA": {
                                "type": "object",
                                "required": [
                                    "a",
                                    "sealedValue"
                                ],
                                "properties": {
                                    "a": {
                                        "type": "integer",
                                        "minimum": -2147483648,
                                        "maximum": 2147483647
                                    },
                                    "sealedValue": {
                                        "type": "string"
                                    }
                                }
                            },
                            "io.github.smiley4.schemakenerator.test.models.kotlinx.SubClassB": {
                                "type": "object",
                                "required": [
                                    "b",
                                    "sealedValue"
                                ],
                                "properties": {
                                    "b": {
                                        "type": "integer",
                                        "minimum": -2147483648,
                                        "maximum": 2147483647
                                    },
                                    "sealedValue": {
                                        "type": "string"
                                    }
                                }
                            }
                        }
                    }
                """.trimIndent(),
            ),
            TestData(
                type = getKType<SubClassA>(),
                testName = "sub class",
                expectedResultInlining = """
                    {
                        "type": "object",
                        "required": ["a","sealedValue"],
                        "properties": {
                            "a": {
                                "type": "integer",
                                "minimum": -2147483648,
                                "maximum": 2147483647
                            },
                            "sealedValue": {
                                "type": "string"
                            }
                        }
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "type": "object",
                        "required": [
                            "a",
                            "sealedValue"
                        ],
                        "properties": {
                            "a": {
                                "type": "integer",
                                "minimum": -2147483648,
                                "maximum": 2147483647
                            },
                            "sealedValue": {
                                "type": "string"
                            }
                        }
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "${'$'}ref": "#/definitions/io.github.smiley4.schemakenerator.test.models.kotlinx.SubClassA",
                        "definitions": {
                            "io.github.smiley4.schemakenerator.test.models.kotlinx.SubClassA": {
                                "type": "object",
                                "required": [
                                    "a",
                                    "sealedValue"
                                ],
                                "properties": {
                                    "a": {
                                        "type": "integer",
                                        "minimum": -2147483648,
                                        "maximum": 2147483647
                                    },
                                    "sealedValue": {
                                        "type": "string"
                                    }
                                }
                            }
                        }
                    }
                """.trimIndent(),
            ),
            TestData(
                type = getKType<ClassWIthDifferentGenerics>(),
                testName = "class with multiple different generics",
                expectedResultInlining = """
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
                                        "maximum": 2147483647
                                    }
                                }
                            },
                            "valueString": {
                                "type": "object",
                                "required": [
                                    "value"
                                ],
                                "properties": {
                                    "value": {
                                        "type": "string"
                                    }
                                }
                            }
                        }
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "type": "object",
                        "required": [
                            "valueInt",
                            "valueString"
                        ],
                        "properties": {
                            "valueInt": {
                                "${'$'}ref": "#/definitions/io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithGenericField"
                            },
                            "valueString": {
                                "${'$'}ref": "#/definitions/io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithGenericField#0"
                            }
                        },
                        "definitions": {
                            "io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithGenericField#0": {
                                "type": "object",
                                "required": [
                                    "value"
                                ],
                                "properties": {
                                    "value": {
                                        "type": "string"
                                    }
                                }
                            },
                            "io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithGenericField": {
                                "type": "object",
                                "required": [
                                    "value"
                                ],
                                "properties": {
                                    "value": {
                                        "type": "integer",
                                        "minimum": -2147483648,
                                        "maximum": 2147483647
                                    }
                                }
                            }
                        }
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "${'$'}ref": "#/definitions/io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWIthDifferentGenerics",
                        "definitions": {
                            "io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithGenericField": {
                                "type": "object",
                                "required": [
                                    "value"
                                ],
                                "properties": {
                                    "value": {
                                        "type": "integer",
                                        "minimum": -2147483648,
                                        "maximum": 2147483647
                                    }
                                }
                            },
                            "io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithGenericField#0": {
                                "type": "object",
                                "required": [
                                    "value"
                                ],
                                "properties": {
                                    "value": {
                                        "type": "string"
                                    }
                                }
                            },
                            "io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWIthDifferentGenerics": {
                                "type": "object",
                                "required": [
                                    "valueInt",
                                    "valueString"
                                ],
                                "properties": {
                                    "valueInt": {
                                        "${'$'}ref": "#/definitions/io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithGenericField"
                                    },
                                    "valueString": {
                                        "${'$'}ref": "#/definitions/io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithGenericField#0"
                                    }
                                }
                            }
                        }
                    }
                """.trimIndent(),
            ),
//            TestData(
//                type = getKType<ClassWithLocalDateTime>(),
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
//                        "type": "object",
//                        "required": [
//                            "dateTime"
//                        ],
//                        "properties": {
//                            "dateTime": {
//                                 "type": "string"
//                            }
//                        }
//                    }
//                """.trimIndent(),
//                expectedResultReferencing = """
//                    {
//                        "type": "object",
//                        "required": [
//                            "dateTime"
//                        ],
//                        "properties": {
//                            "dateTime": {
//                                 "type": "string"
//                            }
//                        }
//                    }
//                """.trimIndent(),
//                expectedResultReferencingRoot = """
//                    {
//                        "${'$'}ref": "#/definitions/ClassWithLocalDateTime",
//                        "definitions": {
//                            "ClassWithLocalDateTime": {
//                                "type": "object",
//                                "required": [
//                                    "dateTime"
//                                ],
//                                "properties": {
//                                    "dateTime": {
//                                         "type": "string"
//                                    }
//                                }
//                            }
//                        }
//                    }
//                """.trimIndent(),
//            ),
//            TestData(
//                type = getKType<ClassWithNestedClass>(),
//                testName = "auto title",
//                generatorModules = listOf(
//                    AutoTitleModule(type = AutoTitleType.SIMPLE_NAME)
//                ),
//                expectedResultInlining = """
//                    {
//                        "type": "object",
//                        "required": [
//                            "nested"
//                        ],
//                        "properties": {
//                            "nested": {
//                                "type": "object",
//                                "required": [
//                                    "text"
//                                ],
//                                "properties": {
//                                    "text": {
//                                        "type": "string",
//                                        "title": "String"
//                                    }
//                                },
//                                "title": "NestedClass"
//                            }
//                        },
//                        "title": "ClassWithNestedClass"
//                    }
//                """.trimIndent(),
//                expectedResultReferencing = """
//                    {
//                        "type": "object",
//                        "required": [
//                            "nested"
//                        ],
//                        "properties": {
//                            "nested": {
//                                "${'$'}ref": "#/definitions/NestedClass"
//                            }
//                        },
//                        "title": "ClassWithNestedClass",
//                        "definitions": {
//                            "NestedClass": {
//                                "type": "object",
//                                "required": [
//                                    "text"
//                                ],
//                                "properties": {
//                                    "text": {
//                                        "type": "string",
//                                        "title": "String"
//                                    }
//                                },
//                                "title": "NestedClass"
//                            }
//                        }
//                    }
//                """.trimIndent(),
//                expectedResultReferencingRoot = """
//                    {
//                        "${'$'}ref": "#/definitions/ClassWithNestedClass",
//                        "definitions": {
//                            "NestedClass": {
//                                "type": "object",
//                                "required": [
//                                    "text"
//                                ],
//                                "properties": {
//                                    "text": {
//                                        "type": "string",
//                                        "title": "String"
//                                    }
//                                },
//                                "title": "NestedClass"
//                            },
//                            "ClassWithNestedClass": {
//                                "type": "object",
//                                "required": [
//                                    "nested"
//                                ],
//                                "properties": {
//                                    "nested": {
//                                        "${'$'}ref": "#/definitions/NestedClass"
//                                    }
//                                },
//                                "title": "ClassWithNestedClass"
//                            }
//                        }
//                    }
//                """.trimIndent(),
//            ),
        )

    }

}
package io.github.smiley4.schemakenerator.jsonschema

import io.github.smiley4.schemakenerator.core.parser.CollectionTypeData
import io.github.smiley4.schemakenerator.core.parser.EnumTypeData
import io.github.smiley4.schemakenerator.core.parser.InlineTypeRef
import io.github.smiley4.schemakenerator.core.parser.MapTypeData
import io.github.smiley4.schemakenerator.core.parser.ObjectTypeData
import io.github.smiley4.schemakenerator.core.parser.PrimitiveTypeData
import io.github.smiley4.schemakenerator.core.parser.PropertyData
import io.github.smiley4.schemakenerator.core.parser.PropertyType
import io.github.smiley4.schemakenerator.core.parser.TypeId
import io.github.smiley4.schemakenerator.core.parser.TypeParameterData
import io.github.smiley4.schemakenerator.core.parser.TypeDataContext
import io.github.smiley4.schemakenerator.core.parser.Visibility
import io.github.smiley4.schemakenerator.core.parser.WildcardTypeData
import io.github.smiley4.schemakenerator.jsonschema.module.InliningGenerator
import io.kotest.assertions.json.ArrayOrder
import io.kotest.assertions.json.FieldComparison
import io.kotest.assertions.json.NumberFormat
import io.kotest.assertions.json.PropertyOrder
import io.kotest.assertions.json.TypeCoercion
import io.kotest.assertions.json.shouldEqualJson
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.WithDataTestName
import io.kotest.datatest.withData

class JsonSchemaTest : FunSpec({

    context("json schema generator: basic inline types") {
        withData(TEST_DATA) { data ->
            val schema = JsonSchemaGenerator()
                .withModule(InliningGenerator())
                .generate(data.typeData, TypeDataContext())
            schema.asJson().prettyPrint().shouldEqualJson {
                propertyOrder = PropertyOrder.Lenient
                arrayOrder = ArrayOrder.Lenient
                fieldComparison = FieldComparison.Strict
                numberFormat = NumberFormat.Lenient
                typeCoercion = TypeCoercion.Disabled
                data.expectedSchema
            }
        }
    }

}) {
    companion object {

        private class TestData(
            val typeData: InlineTypeRef,
            val expectedSchema: String,
            val testName: String
        ) : WithDataTestName {
            override fun dataTestName() = testName
        }

        private val TEST_DATA = listOf(
            TestData(
                testName = "primitive ubyte",
                typeData = InlineTypeRef(
                    PrimitiveTypeData(
                        id = TypeId("kotlin.UByte"),
                        qualifiedName = "kotlin.UByte",
                        simpleName = "UByte",
                        typeParameters = mutableMapOf()
                    )
                ),
                expectedSchema = """
                    {
                        "type": "integer",
                        "minimum": 0,
                        "maximum": 255
                    }
                """.trimIndent(),
            ),
            TestData(
                testName = "primitive integer",
                typeData = InlineTypeRef(
                    PrimitiveTypeData(
                        id = TypeId("kotlin.Int"),
                        qualifiedName = "kotlin.Int",
                        simpleName = "Int",
                        typeParameters = mutableMapOf()
                    )
                ),
                expectedSchema = """
                    {
                        "type": "integer",
                        "minimum": -2147483648,
                        "maximum": 2147483647
                    }
                """.trimIndent(),
            ),
            TestData(
                testName = "primitive float",
                typeData = InlineTypeRef(
                    PrimitiveTypeData(
                        id = TypeId("kotlin.Float"),
                        qualifiedName = "kotlin.Float",
                        simpleName = "Float",
                        typeParameters = mutableMapOf()
                    )
                ),
                expectedSchema = """
                    {
                        "type": "number",
                        "minimum": 1.4E-45,
                        "maximum": 3.4028235E38
                    }
                """.trimIndent(),
            ),
            TestData(
                testName = "primitive boolean",
                typeData = InlineTypeRef(
                    PrimitiveTypeData(
                        id = TypeId("kotlin.Boolean"),
                        qualifiedName = "kotlin.Boolean",
                        simpleName = "Boolean",
                        typeParameters = mutableMapOf()
                    )
                ),
                expectedSchema = """
                    {
                        "type": "boolean"
                    }
                """.trimIndent(),
            ),
            TestData(
                testName = "primitive string",
                typeData = InlineTypeRef(
                    PrimitiveTypeData(
                        id = TypeId("kotlin.String"),
                        qualifiedName = "kotlin.String",
                        simpleName = "String",
                        typeParameters = mutableMapOf()
                    )
                ),
                expectedSchema = """
                    {
                        "type": "string"
                    }
                """.trimIndent(),
            ),
            TestData(
                testName = "list",
                typeData = InlineTypeRef(
                    CollectionTypeData(
                        id = TypeId("kotlin.collections.List<kotlin.String>"),
                        qualifiedName = "kotlin.collections.List",
                        simpleName = "List",
                        typeParameters =mutableMapOf(
                            "E" to TypeParameterData(
                                name = "E",
                                type = InlineTypeRef(
                                    PrimitiveTypeData(
                                        id = TypeId("kotlin.String"),
                                        qualifiedName = "kotlin.String",
                                        simpleName = "String",
                                        typeParameters = mutableMapOf()
                                    )
                                ),
                                nullable = false
                            )
                        ),
                        itemType = PropertyData(
                            name = "item",
                            nullable = false,
                            visibility = Visibility.PUBLIC,
                            kind = PropertyType.PROPERTY,
                            type = InlineTypeRef(
                                PrimitiveTypeData(
                                    id = TypeId("kotlin.String"),
                                    qualifiedName = "kotlin.String",
                                    simpleName = "String",
                                    typeParameters = mutableMapOf()
                                )
                            ),
                            annotations = emptyList()
                        ),
                    )
                ),
                expectedSchema = """
                    {
                        "type": "array",
                        "items": {
                            "type": "string"
                        }
                    }
                """.trimIndent(),
            ),
            TestData(
                testName = "array",
                typeData = InlineTypeRef(
                    CollectionTypeData(
                        id = TypeId("kotlin.Array<kotlin.String>"),
                        qualifiedName = "kotlin.Array",
                        simpleName = "Array",
                        typeParameters = mutableMapOf(
                            "T" to TypeParameterData(
                                name = "T",
                                type = InlineTypeRef(
                                    PrimitiveTypeData(
                                        id = TypeId("kotlin.String"),
                                        qualifiedName = "kotlin.String",
                                        simpleName = "String",
                                        typeParameters = mutableMapOf()
                                    )
                                ),
                                nullable = false
                            )
                        ),
                        itemType = PropertyData(
                            name = "item",
                            nullable = false,
                            visibility = Visibility.PUBLIC,
                            kind = PropertyType.PROPERTY,
                            type = InlineTypeRef(
                                PrimitiveTypeData(
                                    id = TypeId("kotlin.String"),
                                    qualifiedName = "kotlin.String",
                                    simpleName = "String",
                                    typeParameters = mutableMapOf()
                                )
                            ),
                            annotations = emptyList()
                        ),
                    )
                ),
                expectedSchema = """
                    {
                        "type": "array",
                        "items": {
                            "type": "string"
                        }
                    }
                """.trimIndent(),
            ),
            TestData(
                testName = "map",
                typeData = InlineTypeRef(
                    MapTypeData(
                        id = TypeId("kotlin.collections.Map<kotlin.String,kotlin.Int>"),
                        qualifiedName = "kotlin.collections.Map",
                        simpleName = "Map",
                        typeParameters = mutableMapOf(
                            "K" to TypeParameterData(
                                name = "K",
                                nullable = false,
                                type = InlineTypeRef(
                                    PrimitiveTypeData(
                                        id = TypeId("kotlin.String"),
                                        qualifiedName = "kotlin.String",
                                        simpleName = "String",
                                        typeParameters = mutableMapOf()
                                    )
                                )
                            ),
                            "V" to TypeParameterData(
                                name = "V",
                                nullable = false,
                                type = InlineTypeRef(
                                    PrimitiveTypeData(
                                        id = TypeId("kotlin.Int"),
                                        qualifiedName = "kotlin.Int",
                                        simpleName = "Int",
                                        typeParameters = mutableMapOf()
                                    )
                                )
                            )
                        ),
                        keyType = PropertyData(
                            name = "key",
                            nullable = false,
                            visibility = Visibility.PUBLIC,
                            kind = PropertyType.PROPERTY,
                            type = InlineTypeRef(
                                PrimitiveTypeData(
                                    id = TypeId("kotlin.String"),
                                    qualifiedName = "kotlin.String",
                                    simpleName = "String",
                                    typeParameters = mutableMapOf()
                                )
                            )
                        ),
                        valueType = PropertyData(
                            name = "value",
                            nullable = false,
                            visibility = Visibility.PUBLIC,
                            kind = PropertyType.PROPERTY,
                            type = InlineTypeRef(
                                PrimitiveTypeData(
                                    id = TypeId("kotlin.Int"),
                                    qualifiedName = "kotlin.Int",
                                    simpleName = "Int",
                                    typeParameters = mutableMapOf()
                                )
                            )
                        ),
                    )
                ),
                expectedSchema = """
                    {
                        "type": "object",
                        "additionalProperties": {
                            "type": "integer",
                            "minimum": -2147483648,
                            "maximum": 2147483647
                        }
                    }
                """.trimIndent(),
            ),
            TestData(
                testName = "simple class",
                typeData = InlineTypeRef(
                    ObjectTypeData(
                        id = TypeId("MyClass"),
                        qualifiedName = "MyClass",
                        simpleName = "MyClass",
                        typeParameters = mutableMapOf(),
                        members = mutableListOf(
                            PropertyData(
                                name = "requiredField",
                                nullable = false,
                                visibility = Visibility.PUBLIC,
                                kind = PropertyType.PROPERTY,
                                type = InlineTypeRef(
                                    PrimitiveTypeData(
                                        id = TypeId("kotlin.String"),
                                        simpleName = "String",
                                        qualifiedName = "kotlin.String",
                                        typeParameters = mutableMapOf()
                                    )
                                )
                            ),
                            PropertyData(
                                name = "optionalField",
                                nullable = true,
                                visibility = Visibility.PUBLIC,
                                kind = PropertyType.PROPERTY,
                                type = InlineTypeRef(
                                    PrimitiveTypeData(
                                        id = TypeId("kotlin.Boolean"),
                                        simpleName = "Boolean",
                                        qualifiedName = "kotlin.Boolean",
                                        typeParameters = mutableMapOf()
                                    )
                                )
                            )
                        )
                    )
                ),
                expectedSchema = """
                    {
                        "type": "object",
                        "required": ["requiredField"],
                        "properties": {
                            "requiredField": {
                                "type": "string"
                            },
                            "optionalField": {
                                "type": "boolean"
                            }
                        }
                    }
                """.trimIndent(),
            ),
            TestData(
                testName = "generic class with wildcard type",
                typeData = InlineTypeRef(
                    ObjectTypeData(
                        id = TypeId("MyClass"),
                        qualifiedName = "MyClass",
                        simpleName = "MyClass",
                        typeParameters = mutableMapOf(
                            "T" to TypeParameterData(
                                name = "T",
                                nullable = false,
                                type = InlineTypeRef(
                                    WildcardTypeData()
                                )
                            ),
                        ),
                        members = mutableListOf(
                            PropertyData(
                                name = "value",
                                nullable = false,
                                visibility = Visibility.PUBLIC,
                                kind = PropertyType.PROPERTY,
                                type = InlineTypeRef(
                                    WildcardTypeData()
                                )
                            )
                        )
                    )
                ),
                expectedSchema = """
                    {
                        "type": "object",
                        "required": ["value"],
                        "properties": {
                            "value": {
                                "type": "object"
                            }
                        }
                    }
                """.trimIndent(),
            ),
            TestData(
                testName = "enum",
                typeData = InlineTypeRef(
                    EnumTypeData(
                        id = TypeId("TestEnum"),
                        qualifiedName = "TestEnum",
                        simpleName = "TestEnum",
                        typeParameters = mutableMapOf(),
                        enumConstants = mutableListOf("RED", "GREEN", "BLUE")
                    )
                ),
                expectedSchema = """
                    {
                        "enum": ["RED", "GREEN", "BLUE"]
                    }
                """.trimIndent(),
            ),
            TestData(
                testName = "deep nested class",
                typeData = InlineTypeRef(
                    ObjectTypeData(
                        id = TypeId("MyClass"),
                        qualifiedName = "MyClass",
                        simpleName = "MyClass",
                        typeParameters = mutableMapOf(),
                        members = mutableListOf(
                            PropertyData(
                                name = "textField",
                                nullable = false,
                                visibility = Visibility.PUBLIC,
                                kind = PropertyType.PROPERTY,
                                type = InlineTypeRef(
                                    PrimitiveTypeData(
                                        id = TypeId("kotlin.String"),
                                        simpleName = "String",
                                        qualifiedName = "kotlin.String",
                                        typeParameters = mutableMapOf()
                                    )
                                )
                            ),
                            PropertyData(
                                name = "values",
                                nullable = false,
                                visibility = Visibility.PUBLIC,
                                kind = PropertyType.PROPERTY,
                                type = InlineTypeRef(
                                    CollectionTypeData(
                                        id = TypeId("kotlin.collections.List<kotlin.String>"),
                                        qualifiedName = "kotlin.collections.List",
                                        simpleName = "List",
                                        typeParameters = mutableMapOf(
                                            "E" to TypeParameterData(
                                                name = "E",
                                                type = InlineTypeRef(
                                                    PrimitiveTypeData(
                                                        id = TypeId("kotlin.String"),
                                                        qualifiedName = "kotlin.String",
                                                        simpleName = "String",
                                                        typeParameters = mutableMapOf()
                                                    )
                                                ),
                                                nullable = false
                                            )
                                        ),
                                        itemType = PropertyData(
                                            name = "item",
                                            nullable = false,
                                            visibility = Visibility.PUBLIC,
                                            kind = PropertyType.PROPERTY,
                                            type = InlineTypeRef(
                                                PrimitiveTypeData(
                                                    id = TypeId("kotlin.String"),
                                                    qualifiedName = "kotlin.String",
                                                    simpleName = "String",
                                                    typeParameters = mutableMapOf()
                                                )
                                            ),
                                            annotations = emptyList()
                                        ),
                                    )
                                )
                            ),
                            PropertyData(
                                name = "nested",
                                nullable = false,
                                visibility = Visibility.PUBLIC,
                                kind = PropertyType.PROPERTY,
                                type = InlineTypeRef(
                                    ObjectTypeData(
                                        id = TypeId("MyClass"),
                                        qualifiedName = "MyClass",
                                        simpleName = "MyClass",
                                        typeParameters = mutableMapOf(),
                                        members = mutableListOf(
                                            PropertyData(
                                                name = "nestedValue",
                                                nullable = false,
                                                visibility = Visibility.PUBLIC,
                                                kind = PropertyType.PROPERTY,
                                                type = InlineTypeRef(
                                                    ObjectTypeData(
                                                        id = TypeId("MyClass"),
                                                        qualifiedName = "MyClass",
                                                        simpleName = "MyClass",
                                                        typeParameters = mutableMapOf(),
                                                        members = mutableListOf(
                                                            PropertyData(
                                                                name = "deepNestedValue",
                                                                nullable = false,
                                                                visibility = Visibility.PUBLIC,
                                                                kind = PropertyType.PROPERTY,
                                                                type = InlineTypeRef(
                                                                    PrimitiveTypeData(
                                                                        id = TypeId("kotlin.String"),
                                                                        simpleName = "String",
                                                                        qualifiedName = "kotlin.String",
                                                                        typeParameters = mutableMapOf()
                                                                    )
                                                                )
                                                            ),
                                                        )
                                                    )
                                                )
                                            ),
                                        )
                                    )
                                )
                            ),
                        )
                    )
                ),
                expectedSchema = """
                    {
                        "type": "object",
                        "required": ["textField", "values", "nested"],
                        "properties": {
                            "textField": {
                                "type": "string"
                            },
                            "values": {
                                "type": "array",
                                "items": {
                                    "type": "string"
                                }
                            },
                            "nested": {
                                "type": "object",
                                "required": ["nestedValue"],
                                "properties": {
                                    "nestedValue": {
                                        "type": "object",
                                        "required": ["deepNestedValue"],
                                        "properties": {
                                            "deepNestedValue": {
                                                "type": "string"
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                """.trimIndent(),
            ),
        )

    }
}
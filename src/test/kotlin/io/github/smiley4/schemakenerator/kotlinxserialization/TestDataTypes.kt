package io.github.smiley4.schemakenerator.kotlinxserialization

import io.github.smiley4.schemakenerator.assertions.ExpectedEnumTypeData
import io.github.smiley4.schemakenerator.assertions.ExpectedObjectTypeData
import io.github.smiley4.schemakenerator.assertions.ExpectedPrimitiveTypeData
import io.github.smiley4.schemakenerator.assertions.ExpectedPropertyData
import io.github.smiley4.schemakenerator.assertions.shouldHaveExactly
import io.github.smiley4.schemakenerator.assertions.shouldMatch
import io.github.smiley4.schemakenerator.assertions.shouldMatchWildcard
import io.github.smiley4.schemakenerator.parser.core.TypeId
import io.github.smiley4.schemakenerator.parser.core.TypeParserContext
import io.github.smiley4.schemakenerator.parser.serialization.KotlinxSerializationTypeParser
import io.github.smiley4.schemakenerator.parser.serialization.KotlinxSerializationTypeParserConfig
import io.kotest.core.spec.style.StringSpec

class TestDataTypes : StringSpec({

    "Int" {
        val context = TypeParserContext()
        KotlinxSerializationTypeParser(context = context).parse<Int>()
            .let { context.getData(it)!! }
            .also { type ->
                type.shouldMatch(
                    ExpectedPrimitiveTypeData(
                        simpleName = "Int",
                        qualifiedName = "kotlin.Int",
                        typeParameters = emptyMap(),
                    )
                )
            }
        context shouldHaveExactly listOf("kotlin.Int")
    }

    "String" {
        val context = TypeParserContext()
        KotlinxSerializationTypeParser(context = context).parse<String>()
            .let { context.getData(it)!! }
            .also { type ->
                type.shouldMatch(
                    ExpectedPrimitiveTypeData(
                        simpleName = "String",
                        qualifiedName = "kotlin.String",
                        typeParameters = emptyMap(),
                    )
                )
            }
        context shouldHaveExactly listOf("kotlin.String")
    }

    "Any" {
        val context = TypeParserContext()
        KotlinxSerializationTypeParser(context = context).parse<Any>()
            .let { context.getData(it)!! }
            .also { type ->
                type.shouldMatchWildcard()
            }
        context shouldHaveExactly listOf("*")
    }

    "Unit" {
        val context = TypeParserContext()
        KotlinxSerializationTypeParser(context = context).parse<Unit>()
            .let { context.getData(it)!! }
            .also { type ->
                type.shouldMatch(
                    ExpectedPrimitiveTypeData(
                        simpleName = "Unit",
                        qualifiedName = "kotlin.Unit",
                        typeParameters = emptyMap(),
                    )
                )
            }
        context shouldHaveExactly listOf("kotlin.Unit")
    }

    "simple class" {
        val context = TypeParserContext()
        KotlinxSerializationTypeParser(context = context).parse<TestClassSimple>()
            .let { context.getData(it)!! }
            .also { type ->
                type.shouldMatch(
                    ExpectedObjectTypeData(
                        simpleName = "TestClassSimple",
                        qualifiedName = "io.github.smiley4.schemakenerator.kotlinxserialization.TestClassSimple",
                        typeParameters = emptyMap(),
                        members = listOf(
                            ExpectedPropertyData(
                                name = "someField",
                                typeId = "kotlin.String",
                                nullable = false
                            )
                        )
                    )
                )
            }
        context shouldHaveExactly listOf(
            "io.github.smiley4.schemakenerator.kotlinxserialization.TestClassSimple",
            "kotlin.String",
        )
    }

    "mixed types" {
        val context = TypeParserContext()
        KotlinxSerializationTypeParser(context = context).parse<TestClassMixedTypes>()
            .let { context.getData(it)!! }
            .also { type ->
                type.shouldMatch(
                    ExpectedObjectTypeData(
                        simpleName = "TestClassMixedTypes",
                        qualifiedName = "io.github.smiley4.schemakenerator.kotlinxserialization.TestClassMixedTypes",
                        members = listOf(
                            ExpectedPropertyData(
                                name = "myList",
                                typeId = "kotlin.collections.ArrayList<io.github.smiley4.schemakenerator.kotlinxserialization.TestClassSimple>",
                                        nullable = false
                            ),
                            ExpectedPropertyData(
                                name = "myMap",
                                typeId = "kotlin.collections.LinkedHashMap<io.github.smiley4.schemakenerator.kotlinxserialization.TestClassSimple,kotlin.String>",
                                nullable = false
                            ),
                            ExpectedPropertyData(
                                name = "myArray",
                                typeId = "kotlin.Array<io.github.smiley4.schemakenerator.kotlinxserialization.TestClassSimple>",
                                nullable = false
                            )
                        )
                    )
                )
            }
        context shouldHaveExactly listOf(
            "io.github.smiley4.schemakenerator.kotlinxserialization.TestClassSimple",
            "io.github.smiley4.schemakenerator.kotlinxserialization.TestClassMixedTypes",
            "kotlin.collections.ArrayList<io.github.smiley4.schemakenerator.kotlinxserialization.TestClassSimple>",
            "kotlin.collections.LinkedHashMap<io.github.smiley4.schemakenerator.kotlinxserialization.TestClassSimple,kotlin.String>",
            "kotlin.Array<io.github.smiley4.schemakenerator.kotlinxserialization.TestClassSimple>",
            "kotlin.String",
        )
    }

    "enum" {
        val context = TypeParserContext()
        KotlinxSerializationTypeParser(context = context).parse<TestClassWithEnumField>()
            .let { context.getData(it)!! }
            .also { type ->
                type.shouldMatch(
                    ExpectedObjectTypeData(
                        simpleName = "TestClassWithEnumField",
                        qualifiedName = "io.github.smiley4.schemakenerator.kotlinxserialization.TestClassWithEnumField",
                        typeParameters = emptyMap(),
                        supertypeIds = listOf(),
                        members = listOf(
                            ExpectedPropertyData(
                                name = "value",
                                typeId = "io.github.smiley4.schemakenerator.kotlinxserialization.TestEnum",
                                nullable = false
                            )
                        ),
                    )
                )
            }
        context shouldHaveExactly listOf(
            "io.github.smiley4.schemakenerator.kotlinxserialization.TestClassWithEnumField",
            "io.github.smiley4.schemakenerator.kotlinxserialization.TestEnum",
        )

        context.getData(TypeId("io.github.smiley4.schemakenerator.kotlinxserialization.TestEnum"))!!.also { type ->
            type.shouldMatch(
                ExpectedEnumTypeData(
                    simpleName = "TestEnum",
                    enumConstants = listOf("RED", "GREEN", "BLUE")
                )
            )
        }
    }

    "function type" {
        val context = TypeParserContext()
        KotlinxSerializationTypeParser(context = context).parse<TestClassWithFunctionField>()
            .let { context.getData(it)!! }
            .also { type ->
                type.shouldMatch(
                    ExpectedObjectTypeData(
                        simpleName = "TestClassWithFunctionField",
                        qualifiedName = "io.github.smiley4.schemakenerator.kotlinxserialization.TestClassWithFunctionField",
                        typeParameters = emptyMap(),
                        supertypeIds = listOf(),
                        members = listOf(
                            ExpectedPropertyData(
                                name = "value",
                                typeId = "kotlinx.serialization.Polymorphic<Function1>", // todo: seems like data is lost here
                                nullable = false
                            )
                        )
                    )
                )
            }
        context shouldHaveExactly listOf(
            "kotlinx.serialization.Polymorphic<Function1>",
            "io.github.smiley4.schemakenerator.kotlinxserialization.TestClassWithFunctionField",
        )
    }

})
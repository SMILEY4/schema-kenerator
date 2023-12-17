package io.github.smiley4.schemakenerator.reflection

import io.github.smiley4.schemakenerator.parser.core.TypeParsingContext
import io.github.smiley4.schemakenerator.parser.reflection.TypeReflectionParser
import io.github.smiley4.schemakenerator.parser.data.TypeRef
import io.github.smiley4.schemakenerator.assertions.ExpectedMemberData
import io.github.smiley4.schemakenerator.assertions.ExpectedTypeData
import io.github.smiley4.schemakenerator.assertions.shouldHave
import io.github.smiley4.schemakenerator.assertions.shouldMatch
import io.github.smiley4.schemakenerator.parser.core.TypeParsingConfig
import io.kotest.core.spec.style.StringSpec

class TestDataTypes : StringSpec({

    "Int" {
        val context = TypeParsingContext()
        TypeReflectionParser(TypeParsingConfig(), context).parse<Int>()
            .let { context.getData(it)!! }
            .also { type ->
                type.shouldMatch(
                    ExpectedTypeData(
                        simpleName = "Int",
                        qualifiedName = "kotlin.Int",
                        typeParameters = emptyMap(),
                        supertypeIds = emptyList(),
                        members = emptyList()
                    )
                )
            }
        context shouldHave listOf("kotlin.Int")
    }

    "String" {
        val context = TypeParsingContext()
        TypeReflectionParser(TypeParsingConfig(), context).parse<String>()
            .let { context.getData(it)!! }
            .also { type ->
                type.shouldMatch(
                    ExpectedTypeData(
                        simpleName = "String",
                        qualifiedName = "kotlin.String",
                        typeParameters = emptyMap(),
                        supertypeIds = emptyList(),
                        members = emptyList()
                    )
                )
            }
        context shouldHave listOf("kotlin.String")
    }

    "Any" {
        val context = TypeParsingContext()
        TypeReflectionParser(TypeParsingConfig(), context).parse<Any>()
            .let { context.getData(it)!! }
            .also { type ->
                type.shouldMatch(
                    ExpectedTypeData(
                        simpleName = "Any",
                        qualifiedName = "kotlin.Any",
                        typeParameters = emptyMap(),
                        supertypeIds = emptyList(),
                        members = emptyList()
                    )
                )
            }
        context shouldHave listOf("kotlin.Any")
    }

    "Unit" {
        val context = TypeParsingContext()
        TypeReflectionParser(TypeParsingConfig(), context).parse<Unit>()
            .let { context.getData(it)!! }
            .also { type ->
                type.shouldMatch(
                    ExpectedTypeData(
                        simpleName = "Unit",
                        qualifiedName = "kotlin.Unit",
                        typeParameters = emptyMap(),
                        supertypeIds = emptyList(),
                        members = emptyList()
                    )
                )
            }
        context shouldHave listOf("kotlin.Unit")
    }

    "simple class" {
        val context = TypeParsingContext()
        TypeReflectionParser(TypeParsingConfig(), context).parse<TestClassSimple>()
            .let { context.getData(it)!! }
            .also { type ->
                type.shouldMatch(
                    ExpectedTypeData(
                        simpleName = "TestClassSimple",
                        qualifiedName = "io.github.smiley4.schemakenerator.reflection.TestClassSimple",
                        typeParameters = emptyMap(),
                        supertypeIds = listOf("kotlin.Any"),
                        members = listOf(
                            ExpectedMemberData(
                                name = "someField",
                                typeId = "kotlin.String",
                                nullable = false
                            )
                        )
                    )
                )
            }
        context shouldHave listOf(
            "io.github.smiley4.schemakenerator.reflection.TestClassSimple",
            "kotlin.String",
            "kotlin.Any"
        )
    }

    "mixed types" {
        val context = TypeParsingContext()
        TypeReflectionParser(TypeParsingConfig(), context).parse<TestClassMixedTypes>()
            .let { context.getData(it)!! }
            .also { type ->
                type.shouldMatch(
                    ExpectedTypeData(
                        simpleName = "TestClassMixedTypes",
                        qualifiedName = "io.github.smiley4.schemakenerator.reflection.TestClassMixedTypes",
                    )
                )
            }
        context shouldHave listOf(
            "io.github.smiley4.schemakenerator.reflection.TestClassMixedTypes",
            "io.github.smiley4.schemakenerator.reflection.TestClassSimple",
            "kotlin.Array<io.github.smiley4.schemakenerator.reflection.TestClassSimple>",
            "kotlin.collections.Collection<io.github.smiley4.schemakenerator.reflection.TestClassSimple>",
            "kotlin.collections.Collection<kotlin.collections.Map.Entry<kotlin.String,io.github.smiley4.schemakenerator.reflection.TestClassSimple>>",
            "kotlin.collections.Collection<kotlin.String>",
            "kotlin.collections.Iterable<io.github.smiley4.schemakenerator.reflection.TestClassSimple>",
            "kotlin.collections.Iterable<kotlin.collections.Map.Entry<kotlin.String,io.github.smiley4.schemakenerator.reflection.TestClassSimple>>",
            "kotlin.collections.Iterable<kotlin.String>",
            "kotlin.collections.List<io.github.smiley4.schemakenerator.reflection.TestClassSimple>",
            "kotlin.collections.Map.Entry<kotlin.String,io.github.smiley4.schemakenerator.reflection.TestClassSimple>",
            "kotlin.collections.Map<kotlin.String,io.github.smiley4.schemakenerator.reflection.TestClassSimple>",
            "kotlin.collections.Set<kotlin.collections.Map.Entry<kotlin.String,io.github.smiley4.schemakenerator.reflection.TestClassSimple>>",
            "kotlin.collections.Set<kotlin.String>",
            "kotlin.Int",
            "kotlin.String",
            "kotlin.Any",
            "kotlin.Cloneable",
            "java.io.Serializable",
        )
    }

    "enum" {
        val context = TypeParsingContext()
        TypeReflectionParser(TypeParsingConfig(), context).parse<TestClassWithEnumField>()
            .let { context.getData(it)!! }
            .also { type ->
                type.shouldMatch(
                    ExpectedTypeData(
                        simpleName = "TestClassWithEnumField",
                        qualifiedName = "io.github.smiley4.schemakenerator.reflection.TestClassWithEnumField",
                        typeParameters = emptyMap(),
                        supertypeIds = listOf("kotlin.Any"),
                        members = listOf(
                            ExpectedMemberData(
                                name = "value",
                                typeId = "io.github.smiley4.schemakenerator.reflection.TestEnum",
                                nullable = false
                            )
                        )
                    )
                )
            }
        context shouldHave listOf(

            "io.github.smiley4.schemakenerator.reflection.TestClassWithEnumField",
            "io.github.smiley4.schemakenerator.reflection.TestEnum",

            "kotlin.Enum<io.github.smiley4.schemakenerator.models.TestEnum>",

            "kotlin.enums.EnumEntries<io.github.smiley4.schemakenerator.models.TestEnum>",
            "kotlin.collections.List<io.github.smiley4.schemakenerator.models.TestEnum>",
            "kotlin.collections.Collection<io.github.smiley4.schemakenerator.models.TestEnum>",
            "kotlin.collections.Iterable<io.github.smiley4.schemakenerator.models.TestEnum>",

            "kotlin.String",
            "kotlin.Int",
            "kotlin.Any",
        )

        context.getData(TypeRef("io.github.smiley4.schemakenerator.reflection.TestEnum"))!!.also { type ->
            type.shouldMatch(
                ExpectedTypeData(
                    simpleName = "TestEnum",
                    enumValues = listOf("RED", "GREEN", "BLUE")
                )
            )
        }
    }

    "function type" {
        val context = TypeParsingContext()
        TypeReflectionParser(TypeParsingConfig(), context).parse<TestClassWithFunctionField>()
            .let { context.getData(it)!! }
            .also { type ->
                type.shouldMatch(
                    ExpectedTypeData(
                        simpleName = "TestClassWithFunctionField",
                        qualifiedName = "io.github.smiley4.schemakenerator.reflection.TestClassWithFunctionField",
                        typeParameters = emptyMap(),
                        supertypeIds = listOf("kotlin.Any"),
                        members = listOf(
                            ExpectedMemberData(
                                name = "value",
                                typeId = "kotlin.Function1<kotlin.Int,kotlin.String>",
                                nullable = false
                            )
                        )
                    )
                )
            }
        context shouldHave listOf(
            "io.github.smiley4.schemakenerator.reflection.TestClassWithFunctionField",
            "kotlin.Function1<kotlin.Int,kotlin.String>",
            "kotlin.Function<kotlin.String>",
            "kotlin.Int",
            "kotlin.String",
            "kotlin.Any"
        )
    }

})
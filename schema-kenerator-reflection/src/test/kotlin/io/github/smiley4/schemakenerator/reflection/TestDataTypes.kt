package io.github.smiley4.schemakenerator.reflection

import io.github.smiley4.schemakenerator.assertions.ExpectedEnumTypeData
import io.github.smiley4.schemakenerator.assertions.ExpectedObjectTypeData
import io.github.smiley4.schemakenerator.assertions.ExpectedPrimitiveTypeData
import io.github.smiley4.schemakenerator.assertions.ExpectedPropertyData
import io.github.smiley4.schemakenerator.assertions.shouldHave
import io.github.smiley4.schemakenerator.assertions.shouldHaveExactly
import io.github.smiley4.schemakenerator.assertions.shouldMatch
import io.github.smiley4.schemakenerator.core.parser.PrimitiveTypeData
import io.github.smiley4.schemakenerator.core.parser.TypeId
import io.github.smiley4.schemakenerator.core.parser.TypeParserContext
import io.kotest.core.spec.style.StringSpec
import java.time.LocalDateTime

class TestDataTypes : StringSpec({

    "Int" {
        val context = TypeParserContext()
        ReflectionTypeParser(context = context).parse<Int>()
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
        ReflectionTypeParser(context = context).parse<String>()
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
        ReflectionTypeParser(context = context).parse<Any>()
            .let { context.getData(it)!! }
            .also { type ->
                type.shouldMatch(
                    ExpectedPrimitiveTypeData(
                        simpleName = "Any",
                        qualifiedName = "kotlin.Any",
                        typeParameters = emptyMap(),
                    )
                )
            }
        context shouldHaveExactly listOf("kotlin.Any")
    }

    "Unit" {
        val context = TypeParserContext()
        ReflectionTypeParser(context = context).parse<Unit>()
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
        ReflectionTypeParser(context = context).parse<TestClassSimple>()
            .let { context.getData(it)!! }
            .also { type ->
                type.shouldMatch(
                    ExpectedObjectTypeData(
                        simpleName = "TestClassSimple",
                        qualifiedName = "io.github.smiley4.schemakenerator.reflection.TestClassSimple",
                        typeParameters = emptyMap(),
                        supertypeIds = listOf("kotlin.Any"),
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
            "io.github.smiley4.schemakenerator.reflection.TestClassSimple",
            "kotlin.String",
            "kotlin.Any"
        )
    }

    "mixed types" {
        val context = TypeParserContext()
        ReflectionTypeParser(context = context).parse<TestClassMixedTypes>()
            .let { context.getData(it)!! }
            .also { type ->
                type.shouldMatch(
                    ExpectedObjectTypeData(
                        simpleName = "TestClassMixedTypes",
                        qualifiedName = "io.github.smiley4.schemakenerator.reflection.TestClassMixedTypes",
                        members = listOf(
                            ExpectedPropertyData(
                                name = "myList",
                                typeId = "kotlin.collections.List<io.github.smiley4.schemakenerator.reflection.TestClassSimple>",
                                nullable = false
                            ),
                            ExpectedPropertyData(
                                name = "myMap",
                                typeId = "kotlin.collections.Map<kotlin.String,io.github.smiley4.schemakenerator.reflection.TestClassSimple>",
                                nullable = false
                            ),
                            ExpectedPropertyData(
                                name = "myArray",
                                typeId = "kotlin.Array<io.github.smiley4.schemakenerator.reflection.TestClassSimple>",
                                nullable = false
                            )
                        )
                    )
                )
            }
        context shouldHaveExactly listOf(
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
        val context = TypeParserContext()
        ReflectionTypeParser(context = context).parse<TestClassWithEnumField>()
            .let { context.getData(it)!! }
            .also { type ->
                type.shouldMatch(
                    ExpectedObjectTypeData(
                        simpleName = "TestClassWithEnumField",
                        qualifiedName = "io.github.smiley4.schemakenerator.reflection.TestClassWithEnumField",
                        typeParameters = emptyMap(),
                        supertypeIds = listOf("kotlin.Any"),
                        members = listOf(
                            ExpectedPropertyData(
                                name = "value",
                                typeId = "io.github.smiley4.schemakenerator.reflection.TestEnum",
                                nullable = false
                            )
                        ),
                    )
                )
            }
        context shouldHaveExactly listOf(
            "io.github.smiley4.schemakenerator.reflection.TestClassWithEnumField",
            "io.github.smiley4.schemakenerator.reflection.TestEnum",
            "kotlin.Enum<io.github.smiley4.schemakenerator.reflection.TestEnum>",
            "kotlin.enums.EnumEntries<io.github.smiley4.schemakenerator.reflection.TestEnum>",
            "kotlin.collections.List<io.github.smiley4.schemakenerator.reflection.TestEnum>",
            "kotlin.collections.Collection<io.github.smiley4.schemakenerator.reflection.TestEnum>",
            "kotlin.collections.Iterable<io.github.smiley4.schemakenerator.reflection.TestEnum>",
            "kotlin.String",
            "kotlin.Int",
            "kotlin.Any",
        )

        context.getData(TypeId("io.github.smiley4.schemakenerator.reflection.TestEnum"))!!.also { type ->
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
        ReflectionTypeParser(context = context).parse<TestClassWithFunctionField>()
            .let { context.getData(it)!! }
            .also { type ->
                type.shouldMatch(
                    ExpectedObjectTypeData(
                        simpleName = "TestClassWithFunctionField",
                        qualifiedName = "io.github.smiley4.schemakenerator.reflection.TestClassWithFunctionField",
                        typeParameters = emptyMap(),
                        supertypeIds = listOf("kotlin.Any"),
                        members = listOf(
                            ExpectedPropertyData(
                                name = "value",
                                typeId = "kotlin.Function1<kotlin.Int,kotlin.String>",
                                nullable = false
                            )
                        )
                    )
                )
            }
        context shouldHaveExactly listOf(
            "io.github.smiley4.schemakenerator.reflection.TestClassWithFunctionField",
            "kotlin.Function1<kotlin.Int,kotlin.String>",
            "kotlin.Function<kotlin.String>",
            "kotlin.Int",
            "kotlin.String",
            "kotlin.Any"
        )
    }

    "java localdatetime" {
        val context = TypeParserContext()
        ReflectionTypeParser(context = context).parse<TestClassLocalDateTime>()
            .let { context.getData(it)!! }
            .also { type ->
                type.shouldMatch(
                    ExpectedObjectTypeData(
                        simpleName = "TestClassLocalDateTime",
                        qualifiedName = "io.github.smiley4.schemakenerator.reflection.TestClassLocalDateTime",
                        typeParameters = emptyMap(),
                        supertypeIds = listOf("kotlin.Any"),
                        members = listOf(
                            ExpectedPropertyData(
                                name = "timestamp",
                                typeId = "java.time.LocalDateTime",
                                nullable = false
                            )
                        )
                    )
                )
            }
        context shouldHave listOf(
            "io.github.smiley4.schemakenerator.reflection.TestClassLocalDateTime",
            "java.time.LocalDateTime",
            "java.time.temporal.Temporal",
            "java.time.LocalDate",
            "java.time.LocalTime",
            "java.time.chrono.ChronoLocalDate",
        )
    }

    "localdatetime customized" {
        val context = TypeParserContext()
        ReflectionTypeParser(
            context = context,
            config = {
                registerParser(LocalDateTime::class) { id, _ ->
                    PrimitiveTypeData(
                        id = id,
                        simpleName = LocalDateTime::class.simpleName!!,
                        qualifiedName = LocalDateTime::class.qualifiedName!!,
                        typeParameters = emptyMap()
                    )
                }
            }
        ).parse<TestClassLocalDateTime>()
            .let { context.getData(it)!! }
            .also { type ->
                type.shouldMatch(
                    ExpectedObjectTypeData(
                        simpleName = "TestClassLocalDateTime",
                        qualifiedName = "io.github.smiley4.schemakenerator.reflection.TestClassLocalDateTime",
                        typeParameters = emptyMap(),
                        supertypeIds = listOf("kotlin.Any"),
                        members = listOf(
                            ExpectedPropertyData(
                                name = "timestamp",
                                typeId = "java.time.LocalDateTime",
                                nullable = false
                            )
                        )
                    )
                )
            }
        context shouldHaveExactly listOf(
            "io.github.smiley4.schemakenerator.reflection.TestClassLocalDateTime",
            "java.time.LocalDateTime",
            "kotlin.Any"
        )
        context.getData(TypeId("java.time.LocalDateTime"))!!.also { type ->
            type.shouldMatch(
                ExpectedPrimitiveTypeData(
                    simpleName = "LocalDateTime",
                    qualifiedName = "java.time.LocalDateTime"
                )
            )
        }
    }

})
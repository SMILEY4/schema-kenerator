package io.github.smiley4.schemakenerator.reflection

import io.github.smiley4.schemakenerator.core.parser.ContextTypeRef
import io.github.smiley4.schemakenerator.core.parser.ObjectTypeData
import io.github.smiley4.schemakenerator.core.parser.PrimitiveTypeData
import io.github.smiley4.schemakenerator.core.parser.PropertyData
import io.github.smiley4.schemakenerator.core.parser.PropertyType
import io.github.smiley4.schemakenerator.core.parser.TypeId
import io.github.smiley4.schemakenerator.core.parser.TypeParserContext
import io.github.smiley4.schemakenerator.core.parser.Visibility
import io.github.smiley4.schemakenerator.core.parser.idStr
import io.github.smiley4.schemakenerator.core.parser.resolve
import io.github.smiley4.schemakenerator.reflection.parsers.ReflectionTypeParser
import io.github.smiley4.schemakenerator.testutils.shouldHaveExactly
import io.github.smiley4.schemakenerator.testutils.shouldMatch
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDateTime

class TestDataTypesContext : StringSpec({

    "Int" {
        val context = TypeParserContext()
        val result = ReflectionTypeParser(context = context, config = CONFIG).parse<Int>()
        context shouldHaveExactly listOf("kotlin.Int")
        result.also { ref ->
            (ref is ContextTypeRef) shouldBe true
            ref.idStr() shouldBe "kotlin.Int"
        }
        result.resolve(context)!!.also { type ->
            type.shouldMatch(TypeDataContext.int())
        }
    }

    "String" {
        val context = TypeParserContext()
        val result = ReflectionTypeParser(context = context, config = CONFIG).parse<String>()
        context shouldHaveExactly listOf("kotlin.String")
        result.also { ref ->
            (ref is ContextTypeRef) shouldBe true
            ref.idStr() shouldBe "kotlin.String"
        }
        result.resolve(context)!!.also { type ->
            type.shouldMatch(TypeDataContext.string())
        }
    }

    "Any" {
        val context = TypeParserContext()
        val result = ReflectionTypeParser(context = context, config = CONFIG).parse<Any>()
        context shouldHaveExactly listOf("kotlin.Any")
        result.also { ref ->
            (ref is ContextTypeRef) shouldBe true
            ref.idStr() shouldBe "kotlin.Any"
        }
        result.resolve(context)!!.also { type ->
            type.shouldMatch(TypeDataContext.any())
        }
    }

    "Unit" {
        val context = TypeParserContext()
        val result = ReflectionTypeParser(context = context, config = CONFIG).parse<Unit>()
        context shouldHaveExactly listOf("kotlin.Unit")
        result.also { ref ->
            (ref is ContextTypeRef) shouldBe true
            ref.idStr() shouldBe "kotlin.Unit"
        }
        result.resolve(context)!!.also { type ->
            type.shouldMatch(TypeDataContext.unit())
        }
    }

    "simple class" {
        val context = TypeParserContext()
        val result = ReflectionTypeParser(context = context, config = CONFIG).parse<TestClassSimple>()
        context shouldHaveExactly listOf(
            "io.github.smiley4.schemakenerator.reflection.TestClassSimple",
            "kotlin.String",
        )
        result.also { ref ->
            (ref is ContextTypeRef) shouldBe true
            ref.idStr() shouldBe "io.github.smiley4.schemakenerator.reflection.TestClassSimple"
        }
        context.getData(TypeId("io.github.smiley4.schemakenerator.reflection.TestClassSimple"))!!.also { type ->
            type.shouldMatch(TypeDataContext.testClassSimple())
        }
        context.getData(TypeId("kotlin.String"))!!.also { type ->
            type.shouldMatch(TypeDataContext.string())
        }
    }

    "mixed types" {
        val context = TypeParserContext()
        val result = ReflectionTypeParser(context = context, config = CONFIG).parse<TestClassMixedTypes>()
        context shouldHaveExactly listOf(
            "io.github.smiley4.schemakenerator.reflection.TestClassMixedTypes",
            "io.github.smiley4.schemakenerator.reflection.TestClassSimple",
            "kotlin.Array<io.github.smiley4.schemakenerator.reflection.TestClassSimple>",
            "kotlin.String",
            "kotlin.collections.List<io.github.smiley4.schemakenerator.reflection.TestClassSimple>",
            "kotlin.collections.Map<kotlin.String,io.github.smiley4.schemakenerator.reflection.TestClassSimple>"
        )
        result.also { ref ->
            (ref is ContextTypeRef) shouldBe true
            ref.idStr() shouldBe "io.github.smiley4.schemakenerator.reflection.TestClassMixedTypes"
        }
        context.getData(TypeId("io.github.smiley4.schemakenerator.reflection.TestClassMixedTypes"))!!.also { type ->
            type.shouldMatch(TypeDataContext.testClassMixedTypes())
        }
        context.getData(TypeId("io.github.smiley4.schemakenerator.reflection.TestClassSimple"))!!.also { type ->
            type.shouldMatch(TypeDataContext.testClassSimple())
        }
        context.getData(TypeId("kotlin.collections.List<io.github.smiley4.schemakenerator.reflection.TestClassSimple>"))!!
            .also { type ->
                type.shouldMatch(TypeDataContext.list(TypeDataContext.testClassSimple()))
            }
        context.getData(TypeId("kotlin.collections.Map<kotlin.String,io.github.smiley4.schemakenerator.reflection.TestClassSimple>"))!!
            .also { type ->
                type.shouldMatch(TypeDataContext.map(TypeDataContext.string(), TypeDataContext.testClassSimple()))
            }
        context.getData(TypeId("kotlin.Array<io.github.smiley4.schemakenerator.reflection.TestClassSimple>"))!!.also { type ->
            type.shouldMatch(TypeDataContext.array(TypeDataContext.testClassSimple()))
        }
        context.getData(TypeId("kotlin.String"))!!.also { type ->
            type.shouldMatch(TypeDataContext.string())
        }
    }

    "enum" {
        val context = TypeParserContext()
        val result = ReflectionTypeParser(context = context, config = CONFIG).parse<TestClassWithEnumField>()
        context shouldHaveExactly listOf(
            "io.github.smiley4.schemakenerator.reflection.TestEnum",
            "io.github.smiley4.schemakenerator.reflection.TestClassWithEnumField"
        )
        result.also { ref ->
            (ref is ContextTypeRef) shouldBe true
            ref.idStr() shouldBe "io.github.smiley4.schemakenerator.reflection.TestClassWithEnumField"
        }
        context.getData(TypeId("io.github.smiley4.schemakenerator.reflection.TestEnum"))!!.also { type ->
            type.shouldMatch(TypeDataContext.testEnum())
        }
        context.getData(TypeId("io.github.smiley4.schemakenerator.reflection.TestClassWithEnumField"))!!.also { type ->
            type.shouldMatch(TypeDataContext.testClassWithEnumField())
        }
    }

    "java localdatetime" {
        val context = TypeParserContext()
        val result = ReflectionTypeParser(context = context, config = CONFIG).parse<TestClassLocalDateTime>()
        context shouldHaveExactly listOf(
            "*",
            "io.github.smiley4.schemakenerator.reflection.TestClassLocalDateTime",
            "java.time.LocalDate",
            "java.time.LocalDateTime",
            "java.time.chrono.ChronoLocalDate",
            "java.time.chrono.ChronoLocalDateTime<*>",
            "java.time.chrono.ChronoLocalDateTime<java.time.LocalDate>",
            "java.time.temporal.Temporal",
            "java.time.temporal.TemporalAccessor",
            "java.time.temporal.TemporalAdjuster",
            "kotlin.Comparable<java.time.chrono.ChronoLocalDate>",
            "kotlin.Comparable<java.time.chrono.ChronoLocalDateTime<*>>"
        )
        result.also { ref ->
            (ref is ContextTypeRef) shouldBe true
            ref.idStr() shouldBe "io.github.smiley4.schemakenerator.reflection.TestClassLocalDateTime"
        }
        context.getData(TypeId("io.github.smiley4.schemakenerator.reflection.TestClassLocalDateTime"))!!.also { type ->
            type.shouldMatch(
                ObjectTypeData(
                    id = TypeId("io.github.smiley4.schemakenerator.reflection.TestClassLocalDateTime"),
                    simpleName = "TestClassLocalDateTime",
                    qualifiedName = "io.github.smiley4.schemakenerator.reflection.TestClassLocalDateTime",
                    members = listOf(
                        PropertyData(
                            name = "timestamp",
                            nullable = false,
                            visibility = Visibility.PUBLIC,
                            kind = PropertyType.PROPERTY,
                            type = ContextTypeRef(TypeId("java.time.LocalDateTime")),
                            annotations = emptyList()
                        )
                    )
                )
            )
        }
        context.getData(TypeId("java.time.LocalDateTime"))!!.also { type ->
            type.shouldMatch(
                ObjectTypeData(
                    id = TypeId("java.time.LocalDateTime"),
                    qualifiedName = "java.time.LocalDateTime",
                    simpleName = "LocalDateTime",
                    supertypes = listOf(
                        ContextTypeRef(TypeId("java.time.temporal.Temporal")),
                        ContextTypeRef(TypeId("java.time.temporal.TemporalAdjuster")),
                        ContextTypeRef(TypeId("java.time.chrono.ChronoLocalDateTime<java.time.LocalDate>")),
                    ),
                    members = listOf(
                        PropertyData(
                            name = "MIN",
                            nullable = false,
                            visibility = Visibility.PUBLIC,
                            kind = PropertyType.PROPERTY,
                            type = ContextTypeRef(TypeId("java.time.LocalDateTime")),
                            annotations = emptyList()
                        ),
                        PropertyData(
                            name = "MAX",
                            nullable = false,
                            visibility = Visibility.PUBLIC,
                            kind = PropertyType.PROPERTY,
                            type = ContextTypeRef(TypeId("java.time.LocalDateTime")),
                            annotations = emptyList()
                        )
                    ),
                )
            )
        }
    }

    "localdatetime customized" {
        val context = TypeParserContext()
        val result = ReflectionTypeParser(context = context, config = CONFIG_CUSTOM_LOCAL_DATE_TIME).parse<TestClassLocalDateTime>()
        context shouldHaveExactly listOf(
            "io.github.smiley4.schemakenerator.reflection.TestClassLocalDateTime",
            "java.time.LocalDateTime",
        )
        result.also { ref ->
            (ref is ContextTypeRef) shouldBe true
            ref.idStr() shouldBe "io.github.smiley4.schemakenerator.reflection.TestClassLocalDateTime"
        }
        context.getData(TypeId("io.github.smiley4.schemakenerator.reflection.TestClassLocalDateTime"))!!.also { type ->
            type.shouldMatch(
                ObjectTypeData(
                    id = TypeId("io.github.smiley4.schemakenerator.reflection.TestClassLocalDateTime"),
                    simpleName = "TestClassLocalDateTime",
                    qualifiedName = "io.github.smiley4.schemakenerator.reflection.TestClassLocalDateTime",
                    members = listOf(
                        PropertyData(
                            name = "timestamp",
                            nullable = false,
                            visibility = Visibility.PUBLIC,
                            kind = PropertyType.PROPERTY,
                            type = ContextTypeRef(TypeId("java.time.LocalDateTime")),
                            annotations = emptyList()
                        )
                    )
                )
            )
        }
        context.getData(TypeId("java.time.LocalDateTime"))!!.also { type ->
            type.shouldMatch(
                PrimitiveTypeData(
                    id = TypeId("java.time.LocalDateTime"),
                    simpleName = LocalDateTime::class.simpleName!!,
                    qualifiedName = LocalDateTime::class.qualifiedName!!,
                    typeParameters = emptyMap()
                )
            )
        }
    }

}) {

    companion object {

        val CONFIG: (ReflectionTypeParserConfigBuilder.() -> Unit) = {
            inline = false
        }

        val CONFIG_CUSTOM_LOCAL_DATE_TIME: (ReflectionTypeParserConfigBuilder.() -> Unit) = {
            inline = false
            registerParser(LocalDateTime::class) { id, _ ->
                PrimitiveTypeData(
                    id = id,
                    simpleName = LocalDateTime::class.simpleName!!,
                    qualifiedName = LocalDateTime::class.qualifiedName!!,
                    typeParameters = emptyMap()
                )
            }
        }

    }
}
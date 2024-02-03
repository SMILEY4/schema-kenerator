package io.github.smiley4.schemakenerator.reflection

import io.github.smiley4.schemakenerator.core.parser.InlineTypeRef
import io.github.smiley4.schemakenerator.core.parser.PrimitiveTypeData
import io.github.smiley4.schemakenerator.core.parser.TypeParserContext
import io.github.smiley4.schemakenerator.core.parser.idStr
import io.github.smiley4.schemakenerator.core.parser.resolve
import io.github.smiley4.schemakenerator.reflection.parsers.ReflectionTypeParser
import io.github.smiley4.schemakenerator.testutils.shouldBeEmpty
import io.github.smiley4.schemakenerator.testutils.shouldMatch
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDateTime

class TestDataTypesInline : StringSpec({

    "Int" {
        val context = TypeParserContext()
        val result = ReflectionTypeParser(context = context, config = CONFIG).parse<Int>()
        context.shouldBeEmpty()
        result.also { ref ->
            (ref is InlineTypeRef) shouldBe true
            ref.idStr() shouldBe "kotlin.Int"
        }
        result.resolve(context)!!.also { type ->
            type.shouldMatch(TypeDataInline.int())
        }
    }

    "String" {
        val context = TypeParserContext()
        val result = ReflectionTypeParser(context = context, config = CONFIG).parse<String>()
        context.shouldBeEmpty()
        result.also { ref ->
            (ref is InlineTypeRef) shouldBe true
            ref.idStr() shouldBe "kotlin.String"
        }
        result.resolve(context)!!.also { type ->
            type.shouldMatch(TypeDataInline.string())
        }
    }

    "Any" {
        val context = TypeParserContext()
        val result = ReflectionTypeParser(context = context, config = CONFIG).parse<Any>()
        context.shouldBeEmpty()
        result.also { ref ->
            (ref is InlineTypeRef) shouldBe true
            ref.idStr() shouldBe "kotlin.Any"
        }
        result.resolve(context)!!.also { type ->
            type.shouldMatch(TypeDataInline.any())
        }
    }

    "Unit" {
        val context = TypeParserContext()
        val result = ReflectionTypeParser(context = context, config = CONFIG).parse<Unit>()
        context.shouldBeEmpty()
        result.also { ref ->
            (ref is InlineTypeRef) shouldBe true
            ref.idStr() shouldBe "kotlin.Unit"
        }
        result.resolve(context)!!.also { type ->
            type.shouldMatch(TypeDataInline.unit())
        }
    }

    "simple class" {
        val context = TypeParserContext()
        val result = ReflectionTypeParser(context = context, config = CONFIG).parse<TestClassSimple>()
        context.shouldBeEmpty()
        result.also { ref ->
            (ref is InlineTypeRef) shouldBe true
            ref.idStr() shouldBe "io.github.smiley4.schemakenerator.reflection.TestClassSimple"
        }
        result.resolve(context)!!.also { type ->
            type.shouldMatch(TypeDataInline.testClassSimple())
        }
    }

    "mixed types" {
        val context = TypeParserContext()
        val result = ReflectionTypeParser(context = context, config = CONFIG).parse<TestClassMixedTypes>()
        context.shouldBeEmpty()
        result.also { ref ->
            (ref is InlineTypeRef) shouldBe true
            ref.idStr() shouldBe "io.github.smiley4.schemakenerator.reflection.TestClassMixedTypes"
        }
        result.resolve(context)!!.also { type ->
            type.shouldMatch(TypeDataInline.testClassMixedTypes())
        }
    }

    "enum" {
        val context = TypeParserContext()
        val result = ReflectionTypeParser(context = context, config = CONFIG).parse<TestClassWithEnumField>()
        context.shouldBeEmpty()
        result.also { ref ->
            (ref is InlineTypeRef) shouldBe true
            ref.idStr() shouldBe "io.github.smiley4.schemakenerator.reflection.TestClassWithEnumField"
        }
        result.resolve(context)!!.also { type ->
            type.shouldMatch(TypeDataInline.testClassWithEnumField())
        }
    }

    "java localdatetime" {
        shouldThrow<StackOverflowError> {
            // results in infinite recursion when using "inline"
            val context = TypeParserContext()
            ReflectionTypeParser(context = context, config = CONFIG).parse<TestClassLocalDateTime>()
        }
    }

    "localdatetime customized" {
        val context = TypeParserContext()
        val result = ReflectionTypeParser(context = context, config = CONFIG_CUSTOM_LOCAL_DATE_TIME).parse<TestClassLocalDateTime>()
        context.shouldBeEmpty()
        result.also { ref ->
            (ref is InlineTypeRef) shouldBe true
            ref.idStr() shouldBe "io.github.smiley4.schemakenerator.reflection.TestClassLocalDateTime"
        }
        result.resolve(context)!!.also { type ->
            type.shouldMatch(TypeDataInline.testClassLocalDateTimeCustomized())
        }
    }

}) {

    companion object {

        val CONFIG: (ReflectionTypeParserConfigBuilder.() -> Unit) = {
            inline = true
        }

        val CONFIG_CUSTOM_LOCAL_DATE_TIME: (ReflectionTypeParserConfigBuilder.() -> Unit) = {
            inline = true
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
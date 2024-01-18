package io.github.smiley4.schemakenerator.kotlinxserialization

import io.github.smiley4.schemakenerator.core.parser.InlineTypeRef
import io.github.smiley4.schemakenerator.core.parser.TypeParserContext
import io.github.smiley4.schemakenerator.core.parser.WildcardTypeData
import io.github.smiley4.schemakenerator.core.parser.id
import io.github.smiley4.schemakenerator.core.parser.resolve
import io.github.smiley4.schemakenerator.serialization.KotlinxSerializationTypeParser
import io.github.smiley4.schemakenerator.serialization.KotlinxSerializationTypeParserConfigBuilder
import io.github.smiley4.schemakenerator.testutils.shouldBeEmpty
import io.github.smiley4.schemakenerator.testutils.shouldMatch
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class InlineRefsTest : StringSpec({

    "Int" {
        val context = TypeParserContext()
        val result = KotlinxSerializationTypeParser(context = context, config = CONFIG).parse<Int>()
        context.shouldBeEmpty()
        result.also { ref ->
            (ref is InlineTypeRef) shouldBe true
            ref.id() shouldBe "kotlin.Int"
        }
        result.resolve(context)!!.also { type ->
            type shouldMatch TypeDataInline.int()
        }
    }

    "String" {
        val context = TypeParserContext()
        val result = KotlinxSerializationTypeParser(context = context, config = CONFIG).parse<String>()
        context.shouldBeEmpty()
        result.also { ref ->
            (ref is InlineTypeRef) shouldBe true
            ref.id() shouldBe "kotlin.String"
        }
        result.resolve(context)!!.also { type ->
            type shouldMatch TypeDataInline.string()
        }
    }

    "Any" {
        val context = TypeParserContext()
        val result = KotlinxSerializationTypeParser(context = context, config = CONFIG).parse<Any>()
        context.shouldBeEmpty()
        result.also { ref ->
            (ref is InlineTypeRef) shouldBe true
            ref.id() shouldBe "*"
        }
        result.resolve(context)!!.also { type ->
            type shouldMatch WildcardTypeData()
        }
    }

    "Unit" {
        val context = TypeParserContext()
        val result = KotlinxSerializationTypeParser(context = context, config = CONFIG).parse<Unit>()
        context.shouldBeEmpty()
        result.also { ref ->
            (ref is InlineTypeRef) shouldBe true
            ref.id() shouldBe "kotlin.Unit"
        }
        result.resolve(context)!!.also { type ->
            type shouldMatch TypeDataInline.unit()
        }
    }

    "class with only one nullable primitive field" {
        val context = TypeParserContext()
        val result = KotlinxSerializationTypeParser(context = context, config = CONFIG).parse<TestClassSimple>()
        context.shouldBeEmpty()
        result.also { ref ->
            (ref is InlineTypeRef) shouldBe true
            ref.id() shouldBe "io.github.smiley4.schemakenerator.kotlinxserialization.TestClassSimple"
        }
        result.resolve(context)!!.also { type ->
            type shouldMatch TypeDataInline.testClassSimple()
        }
    }

    "class with fields containing (fixed) generic types" {
        val context = TypeParserContext()
        val result = KotlinxSerializationTypeParser(context = context, config = CONFIG).parse<TestClassMixedTypes>()
        context.shouldBeEmpty()
        result.also { ref ->
            (ref is InlineTypeRef) shouldBe true
            ref.id() shouldBe "io.github.smiley4.schemakenerator.kotlinxserialization.TestClassMixedTypes"
        }
        result.resolve(context)!!.also { type ->
            type.shouldMatch(TypeDataInline.testClassMixedTypes())
        }
    }

    "class with enum field" {
        val context = TypeParserContext()
        val result = KotlinxSerializationTypeParser(context = context, config = CONFIG).parse<TestClassWithEnumField>()
        context.shouldBeEmpty()
        result.also { ref ->
            (ref is InlineTypeRef) shouldBe true
            ref.id() shouldBe "io.github.smiley4.schemakenerator.kotlinxserialization.TestClassWithEnumField"
        }
        result.resolve(context)!!.also { type ->
            type.shouldMatch(TypeDataInline.testClassWithEnumField())
        }
    }

    "class with function-type field" {
        val context = TypeParserContext()
        val result = KotlinxSerializationTypeParser(context = context, config = CONFIG).parse<TestClassWithFunctionField>()
        context.shouldBeEmpty()
        result.also { ref ->
            (ref is InlineTypeRef) shouldBe true
            ref.id() shouldBe "io.github.smiley4.schemakenerator.kotlinxserialization.TestClassWithFunctionField"
        }
        result.resolve(context)!!.also { type ->
            type.shouldMatch(TypeDataInline.testClassWithFunctionField())
        }
    }

    "test nested fixed generics (expect valid result compared to context)" {
        val context = TypeParserContext()
        val result = KotlinxSerializationTypeParser(context = context, config = CONFIG).parse<TestClassDeepGeneric>()
        context.shouldBeEmpty()
        result.also { ref ->
            (ref is InlineTypeRef) shouldBe true
            ref.id() shouldBe "io.github.smiley4.schemakenerator.kotlinxserialization.TestClassDeepGeneric"
        }
        result.resolve(context)!!.also { type ->
            type.shouldMatch(TypeDataInline.testClassDeepGeneric())
        }
    }

}) {

    companion object {

        val CONFIG: (KotlinxSerializationTypeParserConfigBuilder.() -> Unit) = {
            inline = true
        }

    }

}
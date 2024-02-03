package io.github.smiley4.schemakenerator.kotlinxserialization

import io.github.smiley4.schemakenerator.core.parser.ContextTypeRef
import io.github.smiley4.schemakenerator.core.parser.PrimitiveTypeData
import io.github.smiley4.schemakenerator.core.parser.TypeId
import io.github.smiley4.schemakenerator.core.parser.TypeParserContext
import io.github.smiley4.schemakenerator.core.parser.idStr
import io.github.smiley4.schemakenerator.core.parser.resolve
import io.github.smiley4.schemakenerator.serialization.KotlinxSerializationTypeParser
import io.github.smiley4.schemakenerator.serialization.KotlinxSerializationTypeParserConfigBuilder
import io.github.smiley4.schemakenerator.testutils.shouldHaveExactly
import io.github.smiley4.schemakenerator.testutils.shouldMatch
import io.github.smiley4.schemakenerator.testutils.shouldMatchWildcard
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ContextRefsTest : StringSpec({

    "Int" {
        val context = TypeParserContext()
        val result = KotlinxSerializationTypeParser(context = context, config = CONFIG).parse<Int>()
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
        val result = KotlinxSerializationTypeParser(context = context, config = CONFIG).parse<String>()
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
        val result = KotlinxSerializationTypeParser(context = context, config = CONFIG).parse<Any>()
        context shouldHaveExactly listOf("*")
        result.also { ref ->
            (ref is ContextTypeRef) shouldBe true
            ref.idStr() shouldBe "*"
        }
        result.resolve(context)!!.also { type ->
            type.shouldMatchWildcard()
        }
    }

    "Unit" {
        val context = TypeParserContext()
        val result = KotlinxSerializationTypeParser(context = context, config = CONFIG).parse<Unit>()
        context shouldHaveExactly listOf("kotlin.Unit")
        result.also { ref ->
            (ref is ContextTypeRef) shouldBe true
            ref.idStr() shouldBe "kotlin.Unit"
        }
        result.resolve(context)!!.also { type ->
            type.shouldMatch(TypeDataContext.unit())
        }
    }

    "class with only one nullable primitive field" {
        val context = TypeParserContext()
        val result = KotlinxSerializationTypeParser(context = context, config = CONFIG).parse<TestClassSimple>()
        context shouldHaveExactly listOf(
            "io.github.smiley4.schemakenerator.kotlinxserialization.TestClassSimple",
            "kotlin.String",
        )
        result.also { ref ->
            (ref is ContextTypeRef) shouldBe true
            ref.idStr() shouldBe "io.github.smiley4.schemakenerator.kotlinxserialization.TestClassSimple"
        }
        context.getData(TypeId("io.github.smiley4.schemakenerator.kotlinxserialization.TestClassSimple"))!!.also { type ->
            type.shouldMatch(TypeDataContext.testClassSimple())
        }
        context.getData(TypeId("kotlin.String"))!!.also { type ->
            type.shouldMatch(TypeDataContext.string())
        }
    }

    "class with fields containing (fixed) generic types" {
        val context = TypeParserContext()
        val result = KotlinxSerializationTypeParser(context = context, config = CONFIG).parse<TestClassMixedTypes>()
        context shouldHaveExactly listOf(
            "io.github.smiley4.schemakenerator.kotlinxserialization.TestClassSimple",
            "io.github.smiley4.schemakenerator.kotlinxserialization.TestClassMixedTypes",
            "kotlin.collections.ArrayList<io.github.smiley4.schemakenerator.kotlinxserialization.TestClassSimple>",
            "kotlin.collections.LinkedHashMap<kotlin.String,io.github.smiley4.schemakenerator.kotlinxserialization.TestClassSimple>",
            "kotlin.Array<io.github.smiley4.schemakenerator.kotlinxserialization.TestClassSimple>",
            "kotlin.String",
        )
        result.also { ref ->
            (ref is ContextTypeRef) shouldBe true
            ref.idStr() shouldBe "io.github.smiley4.schemakenerator.kotlinxserialization.TestClassMixedTypes"
        }
        context.getData(TypeId("io.github.smiley4.schemakenerator.kotlinxserialization.TestClassMixedTypes"))!!.also { type ->
            type.shouldMatch(TypeDataContext.testClassMixedTypes())
        }
        context.getData(TypeId("io.github.smiley4.schemakenerator.kotlinxserialization.TestClassSimple"))!!.also { type ->
            type.shouldMatch(TypeDataContext.testClassSimple())
        }
        context.getData(TypeId("kotlin.collections.ArrayList<io.github.smiley4.schemakenerator.kotlinxserialization.TestClassSimple>"))!!
            .also { type ->
                type.shouldMatch(TypeDataContext.list(TypeDataContext.testClassSimple()))
            }
        context.getData(TypeId("kotlin.collections.LinkedHashMap<kotlin.Stringio.github.smiley4.schemakenerator.kotlinxserialization.TestClassSimple>"))!!
            .also { type ->
                type.shouldMatch(TypeDataContext.map(TypeDataContext.string(), TypeDataContext.testClassSimple()))
            }
        context.getData(TypeId("kotlin.Array<io.github.smiley4.schemakenerator.kotlinxserialization.TestClassSimple>"))!!.also { type ->
            type.shouldMatch(TypeDataContext.array(TypeDataContext.testClassSimple()))
        }
        context.getData(TypeId("kotlin.String"))!!.also { type ->
            type.shouldMatch(TypeDataContext.string())
        }
    }

    "class with enum field" {
        val context = TypeParserContext()
        val result = KotlinxSerializationTypeParser(context = context, config = CONFIG).parse<TestClassWithEnumField>()
        context shouldHaveExactly listOf(
            "io.github.smiley4.schemakenerator.kotlinxserialization.TestClassWithEnumField",
            "io.github.smiley4.schemakenerator.kotlinxserialization.TestEnum",
        )
        result.also { ref ->
            (ref is ContextTypeRef) shouldBe true
            ref.idStr() shouldBe "io.github.smiley4.schemakenerator.kotlinxserialization.TestClassWithEnumField"
        }
        context.getData(TypeId("io.github.smiley4.schemakenerator.kotlinxserialization.TestClassWithEnumField"))!!.also { type ->
            type.shouldMatch(TypeDataContext.testClassWithEnumField())
        }
        context.getData(TypeId("io.github.smiley4.schemakenerator.kotlinxserialization.TestEnum"))!!.also { type ->
            type.shouldMatch(TypeDataContext.testEnum())
        }
    }

    "class with function-type field" {
        val context = TypeParserContext()
        val result = KotlinxSerializationTypeParser(context = context, config = CONFIG).parse<TestClassWithFunctionField>()
        context shouldHaveExactly listOf(
            "kotlinx.serialization.Polymorphic<Function1>",
            "io.github.smiley4.schemakenerator.kotlinxserialization.TestClassWithFunctionField",
        )
        result.also { ref ->
            (ref is ContextTypeRef) shouldBe true
            ref.idStr() shouldBe "io.github.smiley4.schemakenerator.kotlinxserialization.TestClassWithFunctionField"
        }
        context.getData(TypeId("kotlinx.serialization.Polymorphic<Function1>"))!!.also { type ->
            type.shouldMatch(TypeDataContext.polymorphicFunction())
        }
        context.getData(TypeId("io.github.smiley4.schemakenerator.kotlinxserialization.TestClassWithFunctionField"))!!.also { type ->
            type.shouldMatch(TypeDataContext.testClassWithFunctionField())
        }
    }

    "test nested fixed generics (expect invalid result, i.e. duplicate ids with different member-types; fixed with inline)" {
        val context = TypeParserContext()
        val result = KotlinxSerializationTypeParser(context = context, config = CONFIG).parse<TestClassDeepGeneric>()
        context shouldHaveExactly listOf(
            "io.github.smiley4.schemakenerator.kotlinxserialization.TestClassDeepGeneric",
            "io.github.smiley4.schemakenerator.kotlinxserialization.TestClassGeneric",
            "kotlin.Int",
            "kotlin.String"
        )
        result.also { ref ->
            (ref is ContextTypeRef) shouldBe true
            ref.idStr() shouldBe "io.github.smiley4.schemakenerator.kotlinxserialization.TestClassDeepGeneric"
        }
        context.getData(TypeId("io.github.smiley4.schemakenerator.kotlinxserialization.TestClassDeepGeneric"))!!.also { type ->
            type.shouldMatch(TypeDataContext.testClassDeepGeneric())
        }
    }

}) {

    companion object {

        val CONFIG: (KotlinxSerializationTypeParserConfigBuilder.() -> Unit) = {
            inline = false
        }

    }

}
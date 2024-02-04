package io.github.smiley4.schemakenerator.reflection

import io.github.smiley4.schemakenerator.core.parser.ContextTypeRef
import io.github.smiley4.schemakenerator.core.parser.TypeId
import io.github.smiley4.schemakenerator.core.parser.TypeParserContext
import io.github.smiley4.schemakenerator.core.parser.idStr
import io.github.smiley4.schemakenerator.core.parser.resolve
import io.github.smiley4.schemakenerator.reflection.parsers.ReflectionTypeParser
import io.github.smiley4.schemakenerator.testutils.shouldHaveExactly
import io.github.smiley4.schemakenerator.testutils.shouldMatch
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class TestGenerics : StringSpec({

    "test basic single generic" {
        val context = TypeParserContext()
        val result = ReflectionTypeParser(context = context, config =  CONFIG).parse<TestClassGeneric<String>>()
        context shouldHaveExactly listOf(
            "kotlin.String",
            "io.github.smiley4.schemakenerator.reflection.TestClassGeneric<kotlin.String>"
        )
        result.also { ref ->
            (ref is ContextTypeRef) shouldBe true
            ref.idStr() shouldBe "io.github.smiley4.schemakenerator.reflection.TestClassGeneric<kotlin.String>"
        }
        result.resolve(context)!!.also { type ->
            type.shouldMatch(TypeDataContext.testClassGeneric(TypeDataContext.string()))
        }
    }

    "test nested same single generic" {
        val context = TypeParserContext()
        val result = ReflectionTypeParser(context = context, config =  CONFIG).parse<TestClassGeneric<TestClassGeneric<String>>>()
        context shouldHaveExactly listOf(
            "kotlin.String",
            "io.github.smiley4.schemakenerator.reflection.TestClassGeneric<kotlin.String>",
            "io.github.smiley4.schemakenerator.reflection.TestClassGeneric<io.github.smiley4.schemakenerator.reflection.TestClassGeneric<kotlin.String>>"
        )
        result.also { ref ->
            (ref is ContextTypeRef) shouldBe true
            ref.idStr() shouldBe "io.github.smiley4.schemakenerator.reflection.TestClassGeneric<io.github.smiley4.schemakenerator.reflection.TestClassGeneric<kotlin.String>>"
        }
        result.resolve(context)!!.also { type ->
            type.shouldMatch(TypeDataContext.testClassGeneric(TypeDataContext.testClassGeneric(TypeDataContext.string())))
        }
        context.getData(TypeId("io.github.smiley4.schemakenerator.reflection.TestClassGeneric<kotlin.String>"))!!.also { type ->
            type.shouldMatch(TypeDataContext.testClassGeneric(TypeDataContext.string()))
        }
    }

    "test deep generic" {
        val context = TypeParserContext()
        val result = ReflectionTypeParser(context = context, config =  CONFIG).parse<TestClassDeepGeneric<String>>()
        context shouldHaveExactly listOf(
            "kotlin.String",
            "io.github.smiley4.schemakenerator.reflection.TestClassDeepGeneric<kotlin.String>",
            "kotlin.collections.List<kotlin.String>"
        )
        result.also { ref ->
            (ref is ContextTypeRef) shouldBe true
            ref.idStr() shouldBe "io.github.smiley4.schemakenerator.reflection.TestClassDeepGeneric<kotlin.String>"
        }
        result.resolve(context)!!.also { type ->
            type.shouldMatch(TypeDataContext.testClassDeepGeneric(TypeDataContext.string()))
        }
    }

    "test wildcard" {
        val context = TypeParserContext()
        val result = ReflectionTypeParser(context = context, config =  CONFIG).parse<TestClassGeneric<*>>()
        context shouldHaveExactly listOf(
            "*",
            "io.github.smiley4.schemakenerator.reflection.TestClassGeneric<*>",
        )
        result.also { ref ->
            (ref is ContextTypeRef) shouldBe true
            ref.idStr() shouldBe "io.github.smiley4.schemakenerator.reflection.TestClassGeneric<*>"
        }
        result.resolve(context)!!.also { type ->
            type.shouldMatch(TypeDataContext.testClassGeneric(TypeDataContext.wildcard()))
        }
    }

}) {

    companion object {

        val CONFIG: (ReflectionTypeParserConfigBuilder.() -> Unit) = {
            inline = false
        }

    }
}
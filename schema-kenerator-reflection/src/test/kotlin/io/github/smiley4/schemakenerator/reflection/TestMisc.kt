package io.github.smiley4.schemakenerator.reflection

import io.github.smiley4.schemakenerator.core.parser.ContextTypeRef
import io.github.smiley4.schemakenerator.testutils.ExpectedObjectTypeData
import io.github.smiley4.schemakenerator.testutils.ExpectedPropertyData
import io.github.smiley4.schemakenerator.testutils.ExpectedTypeParameterData
import io.github.smiley4.schemakenerator.testutils.shouldHaveExactly
import io.github.smiley4.schemakenerator.testutils.shouldMatch
import io.github.smiley4.schemakenerator.core.parser.ObjectTypeData
import io.github.smiley4.schemakenerator.core.parser.TypeId
import io.github.smiley4.schemakenerator.core.parser.TypeParserContext
import io.github.smiley4.schemakenerator.core.parser.idStr
import io.github.smiley4.schemakenerator.core.parser.resolve
import io.github.smiley4.schemakenerator.reflection.parsers.ReflectionTypeParser
import io.github.smiley4.schemakenerator.testutils.shouldMatchJson
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe

class TestMisc : StringSpec({

    "test basic" {
        val context = TypeParserContext()
        val result = ReflectionTypeParser(context = context, config =  CONFIG).parse<TestSubClass>()
        context shouldHaveExactly listOf(
            "io.github.smiley4.schemakenerator.reflection.TestSubClass",
            "io.github.smiley4.schemakenerator.reflection.TestOpenClass",
            "kotlin.String",
            "kotlin.Int",
        )
        result.also { ref ->
            (ref is ContextTypeRef) shouldBe true
            ref.idStr() shouldBe "io.github.smiley4.schemakenerator.reflection.TestSubClass"
        }
        result.resolve(context)!!.also { type ->
            type.shouldMatch(TypeDataContext.testSubClass())
        }
    }

    "test recursive" {
        val context = TypeParserContext()
        val result = ReflectionTypeParser(context = context, config = CONFIG).parse<TestClassRecursiveGeneric<String>>()
        context shouldHaveExactly listOf(
            "*",
            "kotlin.String",
            "io.github.smiley4.schemakenerator.reflection.TestClassRecursiveGeneric<kotlin.String>",
            "io.github.smiley4.schemakenerator.reflection.TestClassRecursiveGeneric<*>",
            "io.github.smiley4.schemakenerator.reflection.TestInterfaceRecursiveGeneric<io.github.smiley4.schemakenerator.reflection.TestClassRecursiveGeneric<*>>",
        )
        result.also { ref ->
            (ref is ContextTypeRef) shouldBe true
            ref.idStr() shouldBe "io.github.smiley4.schemakenerator.reflection.TestClassRecursiveGeneric<kotlin.String>"
        }
        result.resolve(context)!!.also { type ->
            type.shouldMatch(TypeDataContext.testClassRecursiveGeneric(TypeDataContext.string()))
        }
        context.getData(TypeId("io.github.smiley4.schemakenerator.reflection.TestClassRecursiveGeneric<*>"))!!.also { type ->
            type.shouldMatch(TypeDataContext.testClassRecursiveGeneric(TypeDataContext.wildcard()))
        }
        context.getData(TypeId("io.github.smiley4.schemakenerator.reflection.TestInterfaceRecursiveGeneric<io.github.smiley4.schemakenerator.reflection.TestClassRecursiveGeneric<*>>"))!!.also { type ->
            type.shouldMatch(TypeDataContext.testInterfaceRecursiveGeneric(TypeDataContext.testClassRecursiveGeneric(TypeDataContext.wildcard())))
        }
    }

    "test filters - default config" {
        val context = TypeParserContext()
        ReflectionTypeParser(
            context = context,
            config = {}
        ).parse<TestClassWithMethods>()
            .let { it.resolve(context)!! }
            .also { type ->
                val members = (type as ObjectTypeData).members.map { it.name }
                members shouldContainExactlyInAnyOrder listOf(
                    "someText",
                    "myFlag",
                    "isEnabled",
                    // "hiddenField",
                    // "calculateValue",
                    // "compare",
                    // "isDisabled",
                    // "hiddenFunction",
                    // "equals",
                    // "hashCode",
                    // "toString",
                )
            }
    }

    "test filters - include getters" {
        val context = TypeParserContext()
        ReflectionTypeParser(
            context = context,
            config = {
                includeGetters = true
            }
        ).parse<TestClassWithMethods>()
            .let { it.resolve(context)!! }
            .also { type ->
                val members = (type as ObjectTypeData).members.map { it.name }
                members shouldContainExactlyInAnyOrder listOf(
                    "someText",
                    "myFlag",
                    "isEnabled",
                    // "hiddenField",
                    // "calculateValue",
                    // "compare",
                    "isDisabled",
                    // "hiddenFunction",
                    // "equals",
                    // "hashCode",
                    // "toString",
                )
            }
    }

    "test filters - include weak getters" {
        val context = TypeParserContext()
        ReflectionTypeParser(
            context = context,
            config = {
                includeWeakGetters = true
            }
        ).parse<TestClassWithMethods>()
            .let { it.resolve(context)!! }
            .also { type ->
                val members = (type as ObjectTypeData).members.map { it.name }
                members shouldContainExactlyInAnyOrder listOf(
                    "someText",
                    "myFlag",
                    "isEnabled",
                    // "hiddenField",
                    "calculateValue",
                    // "compare",
                    // "isDisabled",
                    // "hiddenFunction",
                    // "equals",
                    "hashCode",
                    "toString",
                )
            }
    }

    "test filters - include all getters" {
        val context = TypeParserContext()
        ReflectionTypeParser(
            context = context,
            config = {
                includeGetters = true
                includeWeakGetters = true
            }
        ).parse<TestClassWithMethods>()
            .let { it.resolve(context)!! }
            .also { type ->
                val members = (type as ObjectTypeData).members.map { it.name }
                members shouldContainExactlyInAnyOrder listOf(
                    "someText",
                    "myFlag",
                    "isEnabled",
                    // "hiddenField",
                    "calculateValue",
                    // "compare",
                    "isDisabled",
                    // "hiddenFunction",
                    // "equals",
                    "hashCode",
                    "toString",
                )
            }
    }


    "test filters - include hidden" {
        val context = TypeParserContext()
        ReflectionTypeParser(
            context = context,
            config = {
                includeHidden = true
            }
        ).parse<TestClassWithMethods>()
            .let { it.resolve(context)!! }
            .also { type ->
                val members = (type as ObjectTypeData).members.map { it.name }
                members shouldContainExactlyInAnyOrder listOf(
                    "someText",
                    "myFlag",
                    "isEnabled",
                    "hiddenField",
                    // "calculateValue",
                    // "compare",
                    // "isDisabled",
                    // "hiddenFunction",
                    // "equals",
                    // "hashCode",
                    // "toString",
                )
            }
    }

    "test annotations" {
        val context = TypeParserContext()
        val result = ReflectionTypeParser(context = context, config =  CONFIG).parse<TestClassWithAnnotations>()
        context shouldHaveExactly listOf(
            "io.github.smiley4.schemakenerator.reflection.TestClassWithAnnotations",
            "kotlin.String"
        )
        result.also { ref ->
            (ref is ContextTypeRef) shouldBe true
            ref.idStr() shouldBe "io.github.smiley4.schemakenerator.reflection.TestClassWithAnnotations"
        }
        result.resolve(context)!!.also { type ->
            type.shouldMatch(TypeDataContext.testClassWithAnnotations())
        }
    }

}) {

    companion object {

        val CONFIG: (ReflectionTypeParserConfigBuilder.() -> Unit) = {
            inline = false
        }

    }
}
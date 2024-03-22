package io.github.smiley4.schemakenerator.reflection

import io.github.smiley4.schemakenerator.core.enhancer.ContextEnhancer
import io.github.smiley4.schemakenerator.core.parser.ObjectTypeData
import io.github.smiley4.schemakenerator.core.parser.TypeId
import io.github.smiley4.schemakenerator.core.parser.TypeParserContext
import io.github.smiley4.schemakenerator.core.parser.id
import io.github.smiley4.schemakenerator.reflection.parsers.ReflectionTypeParser
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldNotBe

class ReflectionEnhancerTest : StringSpec({

    "test context enhancing, inline=false" {

        val context = TypeParserContext()
        val parser = ReflectionTypeParser(context = context, config = { clearContext = false; inline = false; });

        parser.parse<TestClassA1>()
        parser.parse<TestClassA2>()
        parser.parse<TestClassB>()

        ContextEnhancer(false).enrichSubTypes(context)

        context.getIds() shouldContainExactlyInAnyOrder listOf(
            "io.github.smiley4.schemakenerator.reflection.ReflectionEnhancerTest.Companion.TestClassA",
            "io.github.smiley4.schemakenerator.reflection.ReflectionEnhancerTest.Companion.TestClassA1",
            "io.github.smiley4.schemakenerator.reflection.ReflectionEnhancerTest.Companion.TestClassA2",
            "io.github.smiley4.schemakenerator.reflection.ReflectionEnhancerTest.Companion.TestClassB",
            "io.github.smiley4.schemakenerator.reflection.ReflectionEnhancerTest.Companion.TestBaseClass",
            "kotlin.String",
        )


        context.getData(TypeId( "io.github.smiley4.schemakenerator.reflection.ReflectionEnhancerTest.Companion.TestClassA"))
            .also { type -> type shouldNotBe null }
            .let { type -> type as ObjectTypeData }
            .also { type ->
                type.supertypes.map { it.id().id } shouldContainExactlyInAnyOrder listOf(
                    "io.github.smiley4.schemakenerator.reflection.ReflectionEnhancerTest.Companion.TestBaseClass"
                )
                type.subtypes.map { it.id().id } shouldContainExactlyInAnyOrder listOf(
                    "io.github.smiley4.schemakenerator.reflection.ReflectionEnhancerTest.Companion.TestClassA1",
                    "io.github.smiley4.schemakenerator.reflection.ReflectionEnhancerTest.Companion.TestClassA2"
                )
            }

        context.getData(TypeId( "io.github.smiley4.schemakenerator.reflection.ReflectionEnhancerTest.Companion.TestClassA1"))
            .also { type -> type shouldNotBe null }
            .let { type -> type as ObjectTypeData }
            .also { type ->
                type.supertypes.map { it.id().id } shouldContainExactlyInAnyOrder listOf(
                    "io.github.smiley4.schemakenerator.reflection.ReflectionEnhancerTest.Companion.TestClassA"
                )
                type.subtypes.map { it.id().id } shouldContainExactlyInAnyOrder listOf()
            }

        context.getData(TypeId( "io.github.smiley4.schemakenerator.reflection.ReflectionEnhancerTest.Companion.TestClassA2"))
            .also { type -> type shouldNotBe null }
            .let { type -> type as ObjectTypeData }
            .also { type ->
                type.supertypes.map { it.id().id } shouldContainExactlyInAnyOrder listOf(
                    "io.github.smiley4.schemakenerator.reflection.ReflectionEnhancerTest.Companion.TestClassA"
                )
                type.subtypes.map { it.id().id } shouldContainExactlyInAnyOrder listOf( )
            }

        context.getData(TypeId( "io.github.smiley4.schemakenerator.reflection.ReflectionEnhancerTest.Companion.TestClassB"))
            .also { type -> type shouldNotBe null }
            .let { type -> type as ObjectTypeData }
            .also { type ->
                type.supertypes.map { it.id().id } shouldContainExactlyInAnyOrder listOf(
                    "io.github.smiley4.schemakenerator.reflection.ReflectionEnhancerTest.Companion.TestBaseClass"
                )
                type.subtypes.map { it.id().id } shouldContainExactlyInAnyOrder listOf()
            }


        context.getData(TypeId( "io.github.smiley4.schemakenerator.reflection.ReflectionEnhancerTest.Companion.TestBaseClass"))
            .also { type -> type shouldNotBe null }
            .let { type -> type as ObjectTypeData }
            .also { type ->
                type.supertypes.map { it.id().id } shouldContainExactlyInAnyOrder listOf()
                type.subtypes.map { it.id().id } shouldContainExactlyInAnyOrder listOf(
                    "io.github.smiley4.schemakenerator.reflection.ReflectionEnhancerTest.Companion.TestClassA",
                    "io.github.smiley4.schemakenerator.reflection.ReflectionEnhancerTest.Companion.TestClassB"
                )
            }
    }

}) {
    companion object {

        private abstract class TestBaseClass(val baseValue: String)

        private abstract class TestClassA(val a: String, baseValue: String) : TestBaseClass(baseValue)
        private class TestClassA1(val a1: String, a: String, baseValue: String) : TestClassA(a, baseValue)
        private class TestClassA2(val a2: String, a: String, baseValue: String) : TestClassA(a, baseValue)

        private abstract class TestClassB(val b: String, baseValue: String) : TestBaseClass(baseValue)


    }
}
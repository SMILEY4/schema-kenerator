package io.github.smiley4.schemakenerator.test

import io.github.smiley4.schemakenerator.core.enhancer.ContextSubTypeEnhancer
import io.github.smiley4.schemakenerator.core.parser.BaseTypeData
import io.github.smiley4.schemakenerator.core.parser.ObjectTypeData
import io.github.smiley4.schemakenerator.core.parser.TypeDataContext
import io.github.smiley4.schemakenerator.core.parser.idStr
import io.github.smiley4.schemakenerator.core.parser.resolve
import io.github.smiley4.schemakenerator.reflection.parsers.ReflectionTypeParser
import io.github.smiley4.schemakenerator.serialization.KotlinxSerializationTypeParser
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

@Suppress("ClassName")
class TypeDataEnhancer_ContextSubTypeTests : StringSpec({

    "context sub-type enhancer with reflection parser" {
        val context = TypeDataContext()
        val parser = ReflectionTypeParser(context = context, config = { clearContext = false })

        val refSuper = parser.parse<Reflection.TestSuperClass>()
        val refA = parser.parse<Reflection.TestSubClassA>()
        val refB = parser.parse<Reflection.TestSubClassB>()
        val refC = parser.parse<Reflection.TestSubClassC>()

        ContextSubTypeEnhancer().enhance(context)

        refSuper.resolve(context)!!.also {
            it shouldHaveSubTypes listOf(
                Reflection.TestSubClassA::class,
                Reflection.TestSubClassB::class,
                Reflection.TestSubClassC::class
            )
            it shouldHaveSuperTypes emptyList()
        }
        refA.resolve(context)!!.also {
            it shouldHaveSubTypes emptyList()
            it shouldHaveSuperTypes listOf(Reflection.TestSuperClass::class)
        }
        refB.resolve(context)!!.also {
            it shouldHaveSubTypes emptyList()
            it shouldHaveSuperTypes listOf(Reflection.TestSuperClass::class)
        }
        refC.resolve(context)!!.also {
            it shouldHaveSubTypes emptyList()
            it shouldHaveSuperTypes listOf(Reflection.TestSuperClass::class)
        }

    }

    "context sub-type enhancer with kotlinx-serialization parser" {
        val context = TypeDataContext()
        val parser = KotlinxSerializationTypeParser(context = context, config = { clearContext = false })

        val refSuper = parser.parse<KotlinxSerialization.TestSuperClass>()
        val refA = parser.parse<KotlinxSerialization.TestSubClassA>()
        val refB = parser.parse<KotlinxSerialization.TestSubClassB>()
        val refC = parser.parse<KotlinxSerialization.TestSubClassC>()

        ContextSubTypeEnhancer().enhance(context)

        refSuper.resolve(context)!!.also {
            it shouldHaveSubTypes listOf(
                KotlinxSerialization.TestSubClassA::class,
                KotlinxSerialization.TestSubClassB::class,
                KotlinxSerialization.TestSubClassC::class
            )
            it shouldHaveSuperTypes emptyList()
        }
        refA.resolve(context)!!.also {
            it shouldHaveSubTypes emptyList()
            it shouldHaveSuperTypes listOf(KotlinxSerialization.TestSuperClass::class)
        }
        refB.resolve(context)!!.also {
            it shouldHaveSubTypes emptyList()
            it shouldHaveSuperTypes listOf(KotlinxSerialization.TestSuperClass::class)
        }
        refC.resolve(context)!!.also {
            it shouldHaveSubTypes emptyList()
            it shouldHaveSuperTypes listOf(KotlinxSerialization.TestSuperClass::class)
        }
    }

}) {

    private object Reflection {
        open class TestSuperClass
        class TestSubClassA : TestSuperClass()
        class TestSubClassB : TestSuperClass()
        class TestSubClassC : TestSuperClass()
    }

    private object KotlinxSerialization {
        @Serializable
        sealed class TestSuperClass


        @Serializable
        class TestSubClassA : TestSuperClass()


        @Serializable
        class TestSubClassB : TestSuperClass()


        @Serializable
        class TestSubClassC : TestSuperClass()
    }

    companion object {
        private infix fun BaseTypeData.shouldHaveSubTypes(expected: List<KClass<*>>) {
            (this is ObjectTypeData) shouldBe true
            if (this is ObjectTypeData) {
                this.subtypes.map { it.idStr() } shouldContainExactlyInAnyOrder expected.map { it.qualifiedName!! }
            }
        }

        private infix fun BaseTypeData.shouldHaveSuperTypes(expected: List<KClass<*>>) {
            (this is ObjectTypeData) shouldBe true
            if (this is ObjectTypeData) {
                this.supertypes.map { it.idStr() } shouldContainExactlyInAnyOrder expected.map { it.qualifiedName!! }
            }
        }
    }
}
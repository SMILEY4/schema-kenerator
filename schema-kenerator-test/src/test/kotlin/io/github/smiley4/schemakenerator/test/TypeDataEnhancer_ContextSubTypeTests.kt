package io.github.smiley4.schemakenerator.test

import io.github.smiley4.schemakenerator.core.enhancer.ContextSubTypeEnhancer
import io.github.smiley4.schemakenerator.core.parser.ObjectTypeData
import io.github.smiley4.schemakenerator.core.parser.TypeDataContext
import io.github.smiley4.schemakenerator.core.parser.idStr
import io.github.smiley4.schemakenerator.core.parser.resolve
import io.github.smiley4.schemakenerator.reflection.parsers.ReflectionTypeParser
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder

@Suppress("ClassName")
class TypeDataEnhancer_ContextSubTypeTests : StringSpec({

    "context sub-type enhancer" {
        val context = TypeDataContext()
        val parser = ReflectionTypeParser(context = context, config = { clearContext = false })

        val superRef = parser.parse<TestSuperClass>()
        parser.parse<TestSubClassA>()
        parser.parse<TestSubClassB>()
        parser.parse<TestSubClassC>()

        ContextSubTypeEnhancer(false).enhance(context)

        superRef.resolve(context)!!
            .let { superTypeData -> superTypeData as ObjectTypeData }
            .also { superTypeData ->
                superTypeData.subtypes.map { it.idStr() } shouldContainExactlyInAnyOrder listOf(
                    TestSubClassA::class.qualifiedName!!,
                    TestSubClassB::class.qualifiedName!!,
                    TestSubClassC::class.qualifiedName!!
                )
            }
    }

}) {
    companion object {
        private open class TestSuperClass
        private class TestSubClassA : TestSuperClass()
        private class TestSubClassB : TestSuperClass()
        private class TestSubClassC : TestSuperClass()
    }
}
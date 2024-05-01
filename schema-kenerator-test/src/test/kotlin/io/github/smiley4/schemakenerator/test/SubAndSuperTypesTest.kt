package io.github.smiley4.schemakenerator.test

import io.github.smiley4.schemakenerator.core.data.ObjectTypeData
import io.github.smiley4.schemakenerator.core.steps.ConnectSubTypes
import io.github.smiley4.schemakenerator.reflection.ReflectionAnnotationSubType
import io.github.smiley4.schemakenerator.reflection.ReflectionTypeProcessor
import io.github.smiley4.schemakenerator.reflection.SubType
import io.github.smiley4.schemakenerator.reflection.getKType
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder

class SubAndSuperTypesTest : StringSpec({

    "test tree of sub-types" {

        val result = listOf(getKType<BaseClass1>())
            .let { ReflectionAnnotationSubType().process(it) }
            .let { ReflectionTypeProcessor().process(it) }
            .let { ConnectSubTypes().process(it) }

        result.map { it.qualifiedName } shouldContainExactlyInAnyOrder listOf(
            BaseClass1::class.qualifiedName,
            SubClass1A::class.qualifiedName,
            SubClass1B::class.qualifiedName,
            SubClass1C::class.qualifiedName,
            BaseClass2::class.qualifiedName,
            SubClass2A::class.qualifiedName,
            SubClass2B::class.qualifiedName,
            Any::class.qualifiedName
        )

        result.find { it.qualifiedName == BaseClass1::class.qualifiedName }!!.let { it as ObjectTypeData }.also {
            it.subtypes.map { t -> t.base } shouldContainExactlyInAnyOrder listOf(
                SubClass1A::class.qualifiedName,
                SubClass1B::class.qualifiedName,
                SubClass1C::class.qualifiedName,
            )
            it.supertypes.map { t -> t.base } shouldContainExactlyInAnyOrder listOf(
                Any::class.qualifiedName
            )
        }
        result.find { it.qualifiedName == SubClass1A::class.qualifiedName }!!.let { it as ObjectTypeData }.also { it ->
            it.subtypes.map { t -> t.base } shouldContainExactlyInAnyOrder listOf()
            it.supertypes.map { t -> t.base } shouldContainExactlyInAnyOrder listOf(
                BaseClass1::class.qualifiedName
            )
        }
        result.find { it.qualifiedName == SubClass1B::class.qualifiedName }!!.let { it as ObjectTypeData }.also { it ->
            it.subtypes.map { t -> t.base } shouldContainExactlyInAnyOrder listOf()
            it.supertypes.map { t -> t.base } shouldContainExactlyInAnyOrder listOf(
                BaseClass1::class.qualifiedName
            )
        }
        result.find { it.qualifiedName == SubClass1C::class.qualifiedName }!!.let { it as ObjectTypeData }.also { it ->
            it.subtypes.map { t -> t.base } shouldContainExactlyInAnyOrder listOf()
            it.supertypes.map { t -> t.base } shouldContainExactlyInAnyOrder listOf(
                BaseClass1::class.qualifiedName
            )
        }

        result.find { it.qualifiedName == BaseClass2::class.qualifiedName }!!.let { it as ObjectTypeData }.also {
            it.subtypes.map { t -> t.base } shouldContainExactlyInAnyOrder listOf(
                SubClass2A::class.qualifiedName,
                SubClass2B::class.qualifiedName,
            )
            it.supertypes.map { t -> t.base } shouldContainExactlyInAnyOrder listOf(
                Any::class.qualifiedName
            )
        }
        result.find { it.qualifiedName == SubClass2A::class.qualifiedName }!!.let { it as ObjectTypeData }.also { it ->
            it.subtypes.map { t -> t.base } shouldContainExactlyInAnyOrder listOf()
            it.supertypes.map { t -> t.base } shouldContainExactlyInAnyOrder listOf(
                BaseClass2::class.qualifiedName
            )
        }
        result.find { it.qualifiedName == SubClass2B::class.qualifiedName }!!.let { it as ObjectTypeData }.also { it ->
            it.subtypes.map { t -> t.base } shouldContainExactlyInAnyOrder listOf()
            it.supertypes.map { t -> t.base } shouldContainExactlyInAnyOrder listOf(
                BaseClass2::class.qualifiedName
            )
        }

    }

}) {

    companion object {

        @SubType(SubClass1A::class)
        @SubType(SubClass1B::class)
        @SubType(SubClass1C::class)
        private open class BaseClass1
        private class SubClass1A : BaseClass1()
        private class SubClass1B : BaseClass1()
        private class SubClass1C(val nested: BaseClass2) : BaseClass1()


        @SubType(SubClass2A::class)
        @SubType(SubClass2B::class)
        private open class BaseClass2
        private class SubClass2A : BaseClass2()
        private class SubClass2B : BaseClass2()

    }

}
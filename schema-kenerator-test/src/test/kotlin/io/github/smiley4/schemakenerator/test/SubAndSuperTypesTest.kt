package io.github.smiley4.schemakenerator.test

import com.fasterxml.jackson.annotation.JsonSubTypes
import io.github.smiley4.schemakenerator.core.data.ObjectTypeData
import io.github.smiley4.schemakenerator.core.steps.ConnectSubTypesStep
import io.github.smiley4.schemakenerator.jackson.steps.JacksonSubTypeStep
import io.github.smiley4.schemakenerator.reflection.data.SubType
import io.github.smiley4.schemakenerator.reflection.getKType
import io.github.smiley4.schemakenerator.reflection.steps.ReflectionAnnotationSubTypeStep
import io.github.smiley4.schemakenerator.reflection.steps.ReflectionTypeProcessingStep
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder

class SubAndSuperTypesTest : StringSpec({

    "reflection subtype-annotation" {

        val result = listOf(getKType<BaseClass1>())
            .let { ReflectionAnnotationSubTypeStep().process(it) }
            .let { ReflectionTypeProcessingStep().process(it) }
            .let { ConnectSubTypesStep().process(it) }

        result.map { it.qualifiedName } shouldContainExactlyInAnyOrder listOf(
            BaseClass1::class.qualifiedName,
            SubClass1A::class.qualifiedName,
            SubClass1B::class.qualifiedName,
            SubClass1C::class.qualifiedName,
            BaseClass2::class.qualifiedName,
            SubClass2A::class.qualifiedName,
            SubClass2B::class.qualifiedName,
        )

        result.find { it.qualifiedName == BaseClass1::class.qualifiedName }!!.let { it as ObjectTypeData }.also {
            it.subtypes.map { t -> t.base } shouldContainExactlyInAnyOrder listOf(
                SubClass1A::class.qualifiedName,
                SubClass1B::class.qualifiedName,
                SubClass1C::class.qualifiedName,
            )
            it.supertypes.map { t -> t.base } shouldContainExactlyInAnyOrder listOf()
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
            it.supertypes.map { t -> t.base } shouldContainExactlyInAnyOrder listOf()
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

    "jackson subtype-annotation" {

        val result = listOf(getKType<JacksonBaseClass1>())
            .let { JacksonSubTypeStep(typeProcessing = { types -> ReflectionTypeProcessingStep().process(types) }).process(it) }
            .let { ReflectionTypeProcessingStep().process(it) }
            .let { ConnectSubTypesStep().process(it) }

        result.map { it.qualifiedName } shouldContainExactlyInAnyOrder listOf(
            JacksonBaseClass1::class.qualifiedName,
            JacksonSubClass1A::class.qualifiedName,
            JacksonSubClass1B::class.qualifiedName,
            JacksonSubClass1C::class.qualifiedName,
            JacksonBaseClass2::class.qualifiedName,
            JacksonSubClass2A::class.qualifiedName,
            JacksonSubClass2B::class.qualifiedName,
        )

        result.find { it.qualifiedName == JacksonBaseClass1::class.qualifiedName }!!.let { it as ObjectTypeData }.also {
            it.subtypes.map { t -> t.base } shouldContainExactlyInAnyOrder listOf(
                JacksonSubClass1A::class.qualifiedName,
                JacksonSubClass1B::class.qualifiedName,
                JacksonSubClass1C::class.qualifiedName,
            )
            it.supertypes.map { t -> t.base } shouldContainExactlyInAnyOrder listOf()
        }
        result.find { it.qualifiedName == JacksonSubClass1A::class.qualifiedName }!!.let { it as ObjectTypeData }.also { it ->
            it.subtypes.map { t -> t.base } shouldContainExactlyInAnyOrder listOf()
            it.supertypes.map { t -> t.base } shouldContainExactlyInAnyOrder listOf(
                JacksonBaseClass1::class.qualifiedName
            )
        }
        result.find { it.qualifiedName == JacksonSubClass1B::class.qualifiedName }!!.let { it as ObjectTypeData }.also { it ->
            it.subtypes.map { t -> t.base } shouldContainExactlyInAnyOrder listOf()
            it.supertypes.map { t -> t.base } shouldContainExactlyInAnyOrder listOf(
                JacksonBaseClass1::class.qualifiedName
            )
        }
        result.find { it.qualifiedName == JacksonSubClass1C::class.qualifiedName }!!.let { it as ObjectTypeData }.also { it ->
            it.subtypes.map { t -> t.base } shouldContainExactlyInAnyOrder listOf()
            it.supertypes.map { t -> t.base } shouldContainExactlyInAnyOrder listOf(
                JacksonBaseClass1::class.qualifiedName
            )
        }

        result.find { it.qualifiedName == JacksonBaseClass2::class.qualifiedName }!!.let { it as ObjectTypeData }.also {
            it.subtypes.map { t -> t.base } shouldContainExactlyInAnyOrder listOf(
                JacksonSubClass2A::class.qualifiedName,
                JacksonSubClass2B::class.qualifiedName,
            )
            it.supertypes.map { t -> t.base } shouldContainExactlyInAnyOrder listOf()
        }
        result.find { it.qualifiedName == JacksonSubClass2A::class.qualifiedName }!!.let { it as ObjectTypeData }.also { it ->
            it.subtypes.map { t -> t.base } shouldContainExactlyInAnyOrder listOf()
            it.supertypes.map { t -> t.base } shouldContainExactlyInAnyOrder listOf(
                JacksonBaseClass2::class.qualifiedName
            )
        }
        result.find { it.qualifiedName == JacksonSubClass2B::class.qualifiedName }!!.let { it as ObjectTypeData }.also { it ->
            it.subtypes.map { t -> t.base } shouldContainExactlyInAnyOrder listOf()
            it.supertypes.map { t -> t.base } shouldContainExactlyInAnyOrder listOf(
                JacksonBaseClass2::class.qualifiedName
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


        @JsonSubTypes(
            JsonSubTypes.Type(value = JacksonSubClass1A::class),
            JsonSubTypes.Type(value = JacksonSubClass1B::class),
            JsonSubTypes.Type(value = JacksonSubClass1C::class)
        )
        private open class JacksonBaseClass1
        private class JacksonSubClass1A : JacksonBaseClass1()
        private class JacksonSubClass1B : JacksonBaseClass1()
        private class JacksonSubClass1C(val nested: JacksonBaseClass2) : JacksonBaseClass1()


        @JsonSubTypes(
            JsonSubTypes.Type(value = JacksonSubClass2A::class),
            JsonSubTypes.Type(value = JacksonSubClass2B::class),
        )
        private open class JacksonBaseClass2
        private class JacksonSubClass2A : JacksonBaseClass2()
        private class JacksonSubClass2B : JacksonBaseClass2()

    }

}
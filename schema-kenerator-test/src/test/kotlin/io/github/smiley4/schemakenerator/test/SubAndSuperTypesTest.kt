@file:OptIn(ExperimentalSerializationApi::class)

package io.github.smiley4.schemakenerator.test

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.github.smiley4.schemakenerator.core.data.ObjectTypeData
import io.github.smiley4.schemakenerator.core.steps.AddDiscriminatorStep
import io.github.smiley4.schemakenerator.core.steps.ConnectSubTypesStep
import io.github.smiley4.schemakenerator.jackson.steps.JacksonJsonTypeInfoDiscriminatorStep
import io.github.smiley4.schemakenerator.jackson.steps.JacksonSubTypeStep
import io.github.smiley4.schemakenerator.reflection.data.SubType
import io.github.smiley4.schemakenerator.reflection.steps.ReflectionAnnotationSubTypeStep
import io.github.smiley4.schemakenerator.reflection.steps.ReflectionTypeProcessingStep
import io.github.smiley4.schemakenerator.serialization.steps.HandleJsonClassDiscriminatorStep
import io.github.smiley4.schemakenerator.serialization.steps.KotlinxSerializationTypeProcessingStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCompileInlineStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaGenerationStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaTitleStep
import io.github.smiley4.schemakenerator.swagger.steps.TitleBuilder
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlin.reflect.typeOf

class SubAndSuperTypesTest : StringSpec({

    "reflection subtype-annotation" {

        val result = typeOf<BaseClass1>()
            .let { ReflectionAnnotationSubTypeStep().process(it) }
            .let { ReflectionTypeProcessingStep().process(it) }
            .let { ConnectSubTypesStep().process(it) }

        result.data.qualifiedName shouldBe BaseClass1::class.qualifiedName
        result.supporting.map { it.qualifiedName } shouldContainExactlyInAnyOrder listOf(
            SubClass1A::class.qualifiedName,
            SubClass1B::class.qualifiedName,
            SubClass1C::class.qualifiedName,
            BaseClass2::class.qualifiedName,
            SubClass2A::class.qualifiedName,
            SubClass2B::class.qualifiedName,
        )

        result.data.let { it as ObjectTypeData }.also {
            it.subtypes.map { t -> t.base } shouldContainExactlyInAnyOrder listOf(
                SubClass1A::class.qualifiedName,
                SubClass1B::class.qualifiedName,
                SubClass1C::class.qualifiedName,
            )
            it.supertypes.map { t -> t.base } shouldContainExactlyInAnyOrder listOf()
        }

        result.supporting.find { it.qualifiedName == SubClass1A::class.qualifiedName }!!.let { it as ObjectTypeData }.also { it ->
            it.subtypes.map { t -> t.base } shouldContainExactlyInAnyOrder listOf()
            it.supertypes.map { t -> t.base } shouldContainExactlyInAnyOrder listOf(
                BaseClass1::class.qualifiedName
            )
        }
        result.supporting.find { it.qualifiedName == SubClass1B::class.qualifiedName }!!.let { it as ObjectTypeData }.also { it ->
            it.subtypes.map { t -> t.base } shouldContainExactlyInAnyOrder listOf()
            it.supertypes.map { t -> t.base } shouldContainExactlyInAnyOrder listOf(
                BaseClass1::class.qualifiedName
            )
        }
        result.supporting.find { it.qualifiedName == SubClass1C::class.qualifiedName }!!.let { it as ObjectTypeData }.also { it ->
            it.subtypes.map { t -> t.base } shouldContainExactlyInAnyOrder listOf()
            it.supertypes.map { t -> t.base } shouldContainExactlyInAnyOrder listOf(
                BaseClass1::class.qualifiedName
            )
        }

        result.supporting.find { it.qualifiedName == BaseClass2::class.qualifiedName }!!.let { it as ObjectTypeData }.also {
            it.subtypes.map { t -> t.base } shouldContainExactlyInAnyOrder listOf(
                SubClass2A::class.qualifiedName,
                SubClass2B::class.qualifiedName,
            )
            it.supertypes.map { t -> t.base } shouldContainExactlyInAnyOrder listOf()
        }
        result.supporting.find { it.qualifiedName == SubClass2A::class.qualifiedName }!!.let { it as ObjectTypeData }.also { it ->
            it.subtypes.map { t -> t.base } shouldContainExactlyInAnyOrder listOf()
            it.supertypes.map { t -> t.base } shouldContainExactlyInAnyOrder listOf(
                BaseClass2::class.qualifiedName
            )
        }
        result.supporting.find { it.qualifiedName == SubClass2B::class.qualifiedName }!!.let { it as ObjectTypeData }.also { it ->
            it.subtypes.map { t -> t.base } shouldContainExactlyInAnyOrder listOf()
            it.supertypes.map { t -> t.base } shouldContainExactlyInAnyOrder listOf(
                BaseClass2::class.qualifiedName
            )
        }

    }

    "without reflection subtype-annotation" {

        val result = typeOf<NormalClass>()
            .let { ReflectionAnnotationSubTypeStep().process(it) }
            .let { ReflectionTypeProcessingStep().process(it) }
            .let { ConnectSubTypesStep().process(it) }

        result.data.qualifiedName shouldBe NormalClass::class.qualifiedName
        result.supporting.map { it.qualifiedName } shouldContainExactlyInAnyOrder listOf(
            String::class.qualifiedName,
        )

    }

    "jackson subtype-annotation" {

        val result = typeOf<JacksonBaseClass1>()
            .let { JacksonSubTypeStep(typeProcessing = { type -> ReflectionTypeProcessingStep().process(type) }).process(it) }
            .let { ReflectionTypeProcessingStep().process(it) }
            .let { ConnectSubTypesStep().process(it) }

        result.data.qualifiedName shouldBe JacksonBaseClass1::class.qualifiedName
        result.supporting.map { it.qualifiedName } shouldContainExactlyInAnyOrder listOf(
            JacksonSubClass1A::class.qualifiedName,
            JacksonSubClass1B::class.qualifiedName,
            JacksonSubClass1C::class.qualifiedName,
            JacksonBaseClass2::class.qualifiedName,
            JacksonSubClass2A::class.qualifiedName,
            JacksonSubClass2B::class.qualifiedName,
        )

        result.data.let { it as ObjectTypeData }.also {
            it.subtypes.map { t -> t.base } shouldContainExactlyInAnyOrder listOf(
                JacksonSubClass1A::class.qualifiedName,
                JacksonSubClass1B::class.qualifiedName,
                JacksonSubClass1C::class.qualifiedName,
            )
            it.supertypes.map { t -> t.base } shouldContainExactlyInAnyOrder listOf()
        }

        result.supporting.find { it.qualifiedName == JacksonSubClass1A::class.qualifiedName }!!.let { it as ObjectTypeData }.also { it ->
            it.subtypes.map { t -> t.base } shouldContainExactlyInAnyOrder listOf()
            it.supertypes.map { t -> t.base } shouldContainExactlyInAnyOrder listOf(
                JacksonBaseClass1::class.qualifiedName
            )
        }
        result.supporting.find { it.qualifiedName == JacksonSubClass1B::class.qualifiedName }!!.let { it as ObjectTypeData }.also { it ->
            it.subtypes.map { t -> t.base } shouldContainExactlyInAnyOrder listOf()
            it.supertypes.map { t -> t.base } shouldContainExactlyInAnyOrder listOf(
                JacksonBaseClass1::class.qualifiedName
            )
        }
        result.supporting.find { it.qualifiedName == JacksonSubClass1C::class.qualifiedName }!!.let { it as ObjectTypeData }.also { it ->
            it.subtypes.map { t -> t.base } shouldContainExactlyInAnyOrder listOf()
            it.supertypes.map { t -> t.base } shouldContainExactlyInAnyOrder listOf(
                JacksonBaseClass1::class.qualifiedName
            )
        }

        result.supporting.find { it.qualifiedName == JacksonBaseClass2::class.qualifiedName }!!.let { it as ObjectTypeData }.also {
            it.subtypes.map { t -> t.base } shouldContainExactlyInAnyOrder listOf(
                JacksonSubClass2A::class.qualifiedName,
                JacksonSubClass2B::class.qualifiedName,
            )
            it.supertypes.map { t -> t.base } shouldContainExactlyInAnyOrder listOf()
        }
        result.supporting.find { it.qualifiedName == JacksonSubClass2A::class.qualifiedName }!!.let { it as ObjectTypeData }.also { it ->
            it.subtypes.map { t -> t.base } shouldContainExactlyInAnyOrder listOf()
            it.supertypes.map { t -> t.base } shouldContainExactlyInAnyOrder listOf(
                JacksonBaseClass2::class.qualifiedName
            )
        }
        result.supporting.find { it.qualifiedName == JacksonSubClass2B::class.qualifiedName }!!.let { it as ObjectTypeData }.also { it ->
            it.subtypes.map { t -> t.base } shouldContainExactlyInAnyOrder listOf()
            it.supertypes.map { t -> t.base } shouldContainExactlyInAnyOrder listOf(
                JacksonBaseClass2::class.qualifiedName
            )
        }
    }


    "without jackson subtype-annotation" {

        val result = typeOf<NormalClass>()
            .let { JacksonSubTypeStep(typeProcessing = { type -> ReflectionTypeProcessingStep().process(type) }).process(it) }
            .let { ReflectionTypeProcessingStep().process(it) }
            .let { ConnectSubTypesStep().process(it) }

        result.data.qualifiedName shouldBe NormalClass::class.qualifiedName
        result.supporting.map { it.qualifiedName } shouldContainExactlyInAnyOrder listOf(
            String::class.qualifiedName,
        )

    }

    "include default discriminator with swagger-schema" {

        val result = typeOf<BaseClass1>()
            .let { ReflectionAnnotationSubTypeStep().process(it) }
            .let { ReflectionTypeProcessingStep().process(it) }
            .let { ConnectSubTypesStep().process(it) }
            .let { JacksonJsonTypeInfoDiscriminatorStep().process(it) }
            .let { HandleJsonClassDiscriminatorStep().process(it) }
            .let { AddDiscriminatorStep("_type").process(it) }
            .let { SwaggerSchemaGenerationStep().generate(it) }
            .let { SwaggerSchemaTitleStep(TitleBuilder.BUILDER_SIMPLE).process(it) }
            .let { SwaggerSchemaCompileInlineStep().compile(it) }
            .swagger

        result.discriminator.propertyName shouldBe "_type"
        result.anyOf.map { it.title } shouldContainExactlyInAnyOrder listOf("SubClass1A", "SubClass1B", "SubClass1C")
        result.anyOf.forEach { subtype ->
            subtype.required shouldContain "_type"
            subtype.properties.keys shouldContain "_type"
            subtype.properties["_type"]?.type shouldBe "string"
        }
    }

    "include discriminator from kotlinx @JsonClassDiscriminator with swagger-schema" {

        val result = typeOf<KotlinxParent>()
            .let { KotlinxSerializationTypeProcessingStep().process(it) }
            .let { ConnectSubTypesStep().process(it) }
            .let { JacksonJsonTypeInfoDiscriminatorStep().process(it) }
            .let { HandleJsonClassDiscriminatorStep().process(it) }
            .let { AddDiscriminatorStep("_type").process(it) }
            .let { SwaggerSchemaGenerationStep().generate(it) }
            .let { SwaggerSchemaTitleStep(TitleBuilder.BUILDER_SIMPLE).process(it) }
            .let { SwaggerSchemaCompileInlineStep().compile(it) }
            .swagger

        result.discriminator.propertyName shouldBe "kotlinx_type"
        result.anyOf.map { it.title } shouldContainExactlyInAnyOrder listOf("ChildOne", "ChildTwo")
        result.anyOf.forEach { subtype ->
            subtype.required shouldContain "kotlinx_type"
            subtype.properties.keys shouldContain "kotlinx_type"
            subtype.properties["kotlinx_type"]?.type shouldBe "string"
        }
    }

    "include discriminator from jackson @JsonClassDiscriminator with swagger-schema" {

        val result = typeOf<JacksonParent>()
            .let { ReflectionAnnotationSubTypeStep(10).process(it) }
            .let { ReflectionTypeProcessingStep().process(it) }
            .let { ConnectSubTypesStep().process(it) }
            .let { JacksonJsonTypeInfoDiscriminatorStep().process(it) }
            .let { HandleJsonClassDiscriminatorStep().process(it) }
            .let { AddDiscriminatorStep("_type").process(it) }
            .let { SwaggerSchemaGenerationStep().generate(it) }
            .let { SwaggerSchemaTitleStep(TitleBuilder.BUILDER_SIMPLE).process(it) }
            .let { SwaggerSchemaCompileInlineStep().compile(it) }
            .swagger

        result.discriminator.propertyName shouldBe "jackson_type"
        result.anyOf.map { it.title } shouldContainExactlyInAnyOrder listOf("ChildOne", "ChildTwo")
        result.anyOf.forEach { subtype ->
            subtype.required shouldContain "jackson_type"
            subtype.properties.keys shouldContain "jackson_type"
            subtype.properties["jackson_type"]?.type shouldBe "string"
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


        private class NormalClass(val value: String)


        @Serializable
        @JsonClassDiscriminator("kotlinx_type")
        private sealed class KotlinxParent(val common: Boolean) {

            @Serializable
            data class ChildOne(val text: Byte) : KotlinxParent(false)


            @Serializable
            data class ChildTwo(val number: Int) : KotlinxParent(false)

        }

        @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.PROPERTY,
            property = "jackson_type"
        )
        private sealed class JacksonParent(val common: Boolean) {

            data class ChildOne(val text: Byte) : JacksonParent(false)

            data class ChildTwo(val number: Int) : JacksonParent(false)

        }


    }

}
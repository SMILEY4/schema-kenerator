@file:OptIn(ExperimentalSerializationApi::class)

package io.github.smiley4.schemakenerator.test

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.github.smiley4.schemakenerator.core.addDiscriminatorProperty
import io.github.smiley4.schemakenerator.core.addMissingSupertypeSubtypeRelations
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.flattenToMap
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.jackson.addJacksonTypeInfoDiscriminatorProperty
import io.github.smiley4.schemakenerator.jackson.collectJacksonSubTypes
import io.github.smiley4.schemakenerator.reflection.collectSubTypes
import io.github.smiley4.schemakenerator.reflection.data.SubType
import io.github.smiley4.schemakenerator.reflection.analyzeTypeUsingReflection
import io.github.smiley4.schemakenerator.serialization.addJsonClassDiscriminatorProperty
import io.github.smiley4.schemakenerator.swagger.compileInlining
import io.github.smiley4.schemakenerator.swagger.generateSwaggerSchema
import io.github.smiley4.schemakenerator.swagger.TitleBuilder
import io.github.smiley4.schemakenerator.swagger.withTitle
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
            .collectSubTypes()
            .analyzeTypeUsingReflection()
            .addMissingSupertypeSubtypeRelations()

        result.data.identifyingName.full shouldBe BaseClass1::class.qualifiedName
        result.supporting.map { it.identifyingName.full } shouldContainExactlyInAnyOrder listOf(
            SubClass1A::class.qualifiedName,
            SubClass1B::class.qualifiedName,
            SubClass1C::class.qualifiedName,
            BaseClass2::class.qualifiedName,
            SubClass2A::class.qualifiedName,
            SubClass2B::class.qualifiedName,
        )

        result.data.also {
            it.subtypes.map { t -> result.find(t).identifyingName.full } shouldContainExactlyInAnyOrder listOf(
                SubClass1A::class.qualifiedName,
                SubClass1B::class.qualifiedName,
                SubClass1C::class.qualifiedName,
            )
            it.supertypes.map { t -> result.find(t).identifyingName.full } shouldContainExactlyInAnyOrder listOf()
        }

        result.supporting.find { it.identifyingName.full == SubClass1A::class.qualifiedName }!!.also { it ->
            it.subtypes.map { t -> result.find(t).identifyingName.full } shouldContainExactlyInAnyOrder listOf()
            it.supertypes.map { t -> result.find(t).identifyingName.full} shouldContainExactlyInAnyOrder listOf(
                BaseClass1::class.qualifiedName
            )
        }
        result.supporting.find { it.identifyingName.full == SubClass1B::class.qualifiedName }!!.also { it ->
            it.subtypes.map { t -> result.find(t).identifyingName.full } shouldContainExactlyInAnyOrder listOf()
            it.supertypes.map { t -> result.find(t).identifyingName.full } shouldContainExactlyInAnyOrder listOf(
                BaseClass1::class.qualifiedName
            )
        }
        result.supporting.find { it.identifyingName.full == SubClass1C::class.qualifiedName }!!.also { it ->
            it.subtypes.map { t -> result.find(t).identifyingName.full } shouldContainExactlyInAnyOrder listOf()
            it.supertypes.map { t ->result.find(t).identifyingName.full} shouldContainExactlyInAnyOrder listOf(
                BaseClass1::class.qualifiedName
            )
        }

        result.supporting.find { it.identifyingName.full == BaseClass2::class.qualifiedName }!!.also {
            it.subtypes.map { t ->result.find(t).identifyingName.full} shouldContainExactlyInAnyOrder listOf(
                SubClass2A::class.qualifiedName,
                SubClass2B::class.qualifiedName,
            )
            it.supertypes.map { t ->result.find(t).identifyingName.full} shouldContainExactlyInAnyOrder listOf()
        }
        result.supporting.find { it.identifyingName.full == SubClass2A::class.qualifiedName }!!.also { it ->
            it.subtypes.map { t ->result.find(t).identifyingName.full} shouldContainExactlyInAnyOrder listOf()
            it.supertypes.map { t ->result.find(t).identifyingName.full} shouldContainExactlyInAnyOrder listOf(
                BaseClass2::class.qualifiedName
            )
        }
        result.supporting.find { it.identifyingName.full == SubClass2B::class.qualifiedName }!!.also { it ->
            it.subtypes.map { t ->result.find(t).identifyingName.full} shouldContainExactlyInAnyOrder listOf()
            it.supertypes.map { t ->result.find(t).identifyingName.full} shouldContainExactlyInAnyOrder listOf(
                BaseClass2::class.qualifiedName
            )
        }

    }

    "without reflection subtype-annotation" {

        val result = typeOf<NormalClass>()
            .collectSubTypes()
            .analyzeTypeUsingReflection()
            .addMissingSupertypeSubtypeRelations()

        result.data.identifyingName.full shouldBe NormalClass::class.qualifiedName
        result.supporting.map { it.identifyingName.full } shouldContainExactlyInAnyOrder listOf(
            String::class.qualifiedName,
        )

    }

    "jackson subtype-annotation" {

        val result = typeOf<JacksonBaseClass1>()
            .collectJacksonSubTypes(typeProcessing = { t -> t.analyzeTypeUsingReflection() })
            .analyzeTypeUsingReflection()
            .addMissingSupertypeSubtypeRelations()

        result.data.identifyingName.full shouldBe JacksonBaseClass1::class.qualifiedName
        result.supporting.map { it.identifyingName.full } shouldContainExactlyInAnyOrder listOf(
            JacksonSubClass1A::class.qualifiedName,
            JacksonSubClass1B::class.qualifiedName,
            JacksonSubClass1C::class.qualifiedName,
            JacksonBaseClass2::class.qualifiedName,
            JacksonSubClass2A::class.qualifiedName,
            JacksonSubClass2B::class.qualifiedName,
        )

        result.data.also {
            it.subtypes.map { t ->result.find(t).identifyingName.full} shouldContainExactlyInAnyOrder listOf(
                JacksonSubClass1A::class.qualifiedName,
                JacksonSubClass1B::class.qualifiedName,
                JacksonSubClass1C::class.qualifiedName,
            )
            it.supertypes.map { t ->result.find(t).identifyingName.full} shouldContainExactlyInAnyOrder listOf()
        }

        result.supporting.find { it.identifyingName.full == JacksonSubClass1A::class.qualifiedName }!!.also { it ->
            it.subtypes.map { t ->result.find(t).identifyingName.full} shouldContainExactlyInAnyOrder listOf()
            it.supertypes.map { t ->result.find(t).identifyingName.full} shouldContainExactlyInAnyOrder listOf(
                JacksonBaseClass1::class.qualifiedName
            )
        }
        result.supporting.find { it.identifyingName.full == JacksonSubClass1B::class.qualifiedName }!!.also { it ->
            it.subtypes.map { t ->result.find(t).identifyingName.full} shouldContainExactlyInAnyOrder listOf()
            it.supertypes.map { t ->result.find(t).identifyingName.full} shouldContainExactlyInAnyOrder listOf(
                JacksonBaseClass1::class.qualifiedName
            )
        }
        result.supporting.find { it.identifyingName.full == JacksonSubClass1C::class.qualifiedName }!!.also { it ->
            it.subtypes.map { t ->result.find(t).identifyingName.full} shouldContainExactlyInAnyOrder listOf()
            it.supertypes.map { t ->result.find(t).identifyingName.full} shouldContainExactlyInAnyOrder listOf(
                JacksonBaseClass1::class.qualifiedName
            )
        }

        result.supporting.find { it.identifyingName.full == JacksonBaseClass2::class.qualifiedName }!!.also {
            it.subtypes.map { t ->result.find(t).identifyingName.full} shouldContainExactlyInAnyOrder listOf(
                JacksonSubClass2A::class.qualifiedName,
                JacksonSubClass2B::class.qualifiedName,
            )
            it.supertypes.map { t ->result.find(t).identifyingName.full} shouldContainExactlyInAnyOrder listOf()
        }
        result.supporting.find { it.identifyingName.full == JacksonSubClass2A::class.qualifiedName }!!.also { it ->
            it.subtypes.map { t ->result.find(t).identifyingName.full} shouldContainExactlyInAnyOrder listOf()
            it.supertypes.map { t ->result.find(t).identifyingName.full} shouldContainExactlyInAnyOrder listOf(
                JacksonBaseClass2::class.qualifiedName
            )
        }
        result.supporting.find { it.identifyingName.full == JacksonSubClass2B::class.qualifiedName }!!.also { it ->
            it.subtypes.map { t ->result.find(t).identifyingName.full} shouldContainExactlyInAnyOrder listOf()
            it.supertypes.map { t ->result.find(t).identifyingName.full} shouldContainExactlyInAnyOrder listOf(
                JacksonBaseClass2::class.qualifiedName
            )
        }
    }


    "without jackson subtype-annotation" {

        val result = typeOf<NormalClass>()
            .collectJacksonSubTypes(typeProcessing = { it.analyzeTypeUsingReflection() })
            .analyzeTypeUsingReflection()
            .addMissingSupertypeSubtypeRelations()

        result.data.identifyingName.full shouldBe NormalClass::class.qualifiedName
        result.supporting.map { it.identifyingName.full } shouldContainExactlyInAnyOrder listOf(
            String::class.qualifiedName,
        )

    }

    "include default discriminator with swagger-schema" {

        val result = typeOf<BaseClass1>()
            .collectSubTypes()
            .analyzeTypeUsingReflection()
            .addMissingSupertypeSubtypeRelations()
            .addJacksonTypeInfoDiscriminatorProperty()
            .addJsonClassDiscriminatorProperty()
            .addDiscriminatorProperty("_type")
            .generateSwaggerSchema()
            .withTitle(TitleBuilder.BUILDER_SIMPLE)
            .compileInlining()
            .swagger

        result.discriminator.propertyName shouldBe "_type"
        result.anyOf.map { it.title } shouldContainExactlyInAnyOrder listOf("SubClass1A", "SubClass1B", "SubClass1C")
        result.anyOf.forEach { subtype ->
            subtype.required shouldContain "_type"
            subtype.properties.keys shouldContain "_type"
            subtype.properties["_type"]?.types shouldContainExactlyInAnyOrder setOf("string")
        }
    }

    "include discriminator from kotlinx @JsonClassDiscriminator with swagger-schema" {

        val result = typeOf<KotlinxParent>()
            .collectSubTypes()
            .analyzeTypeUsingReflection()
            .addMissingSupertypeSubtypeRelations()
            .addJacksonTypeInfoDiscriminatorProperty()
            .addJsonClassDiscriminatorProperty()
            .addDiscriminatorProperty("_type")
            .generateSwaggerSchema()
            .withTitle(TitleBuilder.BUILDER_SIMPLE)
            .compileInlining()
            .swagger

        result.discriminator.propertyName shouldBe "kotlinx_type"
        result.anyOf.map { it.title } shouldContainExactlyInAnyOrder listOf("ChildOne", "ChildTwo")
        result.anyOf.forEach { subtype ->
            subtype.required shouldContain "kotlinx_type"
            subtype.properties.keys shouldContain "kotlinx_type"
            subtype.properties["kotlinx_type"]?.types shouldContainExactlyInAnyOrder setOf("string")
        }
    }

    "include discriminator from jackson @JsonClassDiscriminator with swagger-schema" {

        val result = typeOf<JacksonParent>()
            .collectSubTypes()
            .analyzeTypeUsingReflection()
            .addMissingSupertypeSubtypeRelations()
            .addJacksonTypeInfoDiscriminatorProperty()
            .addJsonClassDiscriminatorProperty()
            .addDiscriminatorProperty("_type")
            .generateSwaggerSchema()
            .withTitle(TitleBuilder.BUILDER_SIMPLE)
            .compileInlining()
            .swagger

        result.discriminator.propertyName shouldBe "jackson_type"
        result.anyOf.map { it.title } shouldContainExactlyInAnyOrder listOf("ChildOne", "ChildTwo")
        result.anyOf.forEach { subtype ->
            subtype.required shouldContain "jackson_type"
            subtype.properties.keys shouldContain "jackson_type"
            subtype.properties["jackson_type"]?.types shouldContainExactlyInAnyOrder setOf("string")
        }
    }

}) {

    companion object {

        fun Bundle<TypeData>.find(id: TypeId) = this.flattenToMap()[id]!!

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
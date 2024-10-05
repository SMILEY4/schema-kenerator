@file:OptIn(ExperimentalSerializationApi::class)

package io.github.smiley4.schemakenerator.test

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.smiley4.schemakenerator.core.connectSubTypes
import io.github.smiley4.schemakenerator.jackson.addJacksonTypeInfoDiscriminatorProperty
import io.github.smiley4.schemakenerator.jackson.collectJacksonSubTypes
import io.github.smiley4.schemakenerator.jsonschema.compileInlining
import io.github.smiley4.schemakenerator.jsonschema.generateJsonSchema
import io.github.smiley4.schemakenerator.reflection.collectSubTypes
import io.github.smiley4.schemakenerator.reflection.data.SubType
import io.github.smiley4.schemakenerator.reflection.processReflection
import io.github.smiley4.schemakenerator.serialization.processKotlinxSerialization
import io.github.smiley4.schemakenerator.swagger.compileReferencing
import io.github.smiley4.schemakenerator.swagger.generateSwaggerSchema
import io.github.smiley4.schemakenerator.swagger.withTitle
import io.kotest.core.spec.style.StringSpec
import io.swagger.v3.oas.models.media.Schema
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlin.reflect.typeOf

/**
 * internal / manual tests only
 */
class Test : StringSpec({

    "test json" {
        val result = typeOf<KotlinxParent>()
            .processKotlinxSerialization()
            .connectSubTypes()
            .generateJsonSchema()
            .compileInlining()

        println(result.json.prettyPrint())

    }

    "test swagger" {
        val result = typeOf<JacksonParent>()
            .collectSubTypes()
            .processReflection()
            .connectSubTypes()
            .addJacksonTypeInfoDiscriminatorProperty()
//            .addJsonClassDiscriminatorProperty()
//            .addDiscriminatorProperty("_my_test_discriminator")
            .generateSwaggerSchema {
                discriminatorFromMarkerAnnotation = true
            }
            .withTitle()
            .compileReferencing()
            .let {
                SwaggerResult(
                    root = it.swagger,
                    componentSchemas = it.componentSchemas
                )
            }

        println(json.writeValueAsString(result))

    }

}) {
    companion object {

        class SwaggerResult(
            val root: Schema<*>,
            val componentSchemas: Map<String, Schema<*>>
        )


        @Serializable
        @JsonClassDiscriminator("the_type")
        sealed class KotlinxParent(val common: Boolean) {

            @Serializable
            data class ChildOne(val text: Byte) : KotlinxParent(false)


            @Serializable
            data class ChildTwo(val number: Int) : KotlinxParent(false)

        }


        @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.PROPERTY,
            property = "the_type"
        )
        @SubType(JacksonParent.ChildOne::class)
        @SubType(JacksonParent.ChildTwo::class)
        sealed class JacksonParent(val common: Boolean) {

            data class ChildOne(val text: Byte) : JacksonParent(false)

            data class ChildTwo(val number: Int) : JacksonParent(false)
        }

        private val json = jacksonObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL).writerWithDefaultPrettyPrinter()!!

    }
}
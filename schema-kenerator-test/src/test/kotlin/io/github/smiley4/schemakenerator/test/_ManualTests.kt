@file:OptIn(ExperimentalSerializationApi::class)
@file:Suppress("ClassName")

package io.github.smiley4.schemakenerator.test

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.smiley4.schemakenerator.core.addDiscriminatorProperty
import io.github.smiley4.schemakenerator.jackson.addJacksonTypeInfoDiscriminatorProperty
import io.github.smiley4.schemakenerator.reflection.processReflection
import io.github.smiley4.schemakenerator.serialization.addJsonClassDiscriminatorProperty
import io.github.smiley4.schemakenerator.serialization.processKotlinxSerialization
import io.github.smiley4.schemakenerator.swagger.compileInlining
import io.github.smiley4.schemakenerator.swagger.compileReferencing
import io.github.smiley4.schemakenerator.swagger.data.CompiledSwaggerSchema
import io.github.smiley4.schemakenerator.swagger.data.RefType
import io.github.smiley4.schemakenerator.swagger.generateSwaggerSchema
import io.github.smiley4.schemakenerator.swagger.handleSchemaAnnotations
import io.kotest.core.spec.style.StringSpec
import io.swagger.v3.core.util.Json31
import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlin.reflect.typeOf

/**
 * internal / manual tests only
 */
class _ManualTests : StringSpec({

    "test json" {
//        val result = typeOf<KotlinxParent>()
//            .processKotlinxSerialization()
//            .connectSubTypes()
//            .generateJsonSchema()
//            .compileInlining()
//
//        println(result.json.prettyPrint())

    }

    "test swagger" {
        val result = typeOf<Vehicle>()
            .processReflection()
            .addJacksonTypeInfoDiscriminatorProperty()
            .generateSwaggerSchema()
            .compileReferencing(RefType.OPENAPI_FULL)
            .asPrintable()

        println(Json31.prettyPrint(result))

    }

}) {
    companion object {

        @JsonTypeInfo(
            property = "_myType",
            include = JsonTypeInfo.As.PROPERTY,
            use = JsonTypeInfo.Id.NAME,
        )
        @JsonSubTypes(
            JsonSubTypes.Type(value = Car::class, name = "myCar"),
            JsonSubTypes.Type(value = Train::class, name = "myTrain"),
        )
        sealed class Vehicle

        class Car : Vehicle()

        class Train : Vehicle()

        class SwaggerResult(
            val root: io.swagger.v3.oas.models.media.Schema<*>,
            val componentSchemas: Map<String, io.swagger.v3.oas.models.media.Schema<*>>
        )

        fun CompiledSwaggerSchema.asPrintable(): SwaggerResult {
            return SwaggerResult(
                root = this.swagger,
                componentSchemas = this.componentSchemas
            )
        }

        private val json = jacksonObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL).writerWithDefaultPrettyPrinter()!!

    }
}
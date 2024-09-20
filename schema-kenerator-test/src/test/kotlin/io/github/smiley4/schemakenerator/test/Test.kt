package io.github.smiley4.schemakenerator.test

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.smiley4.schemakenerator.serialization.processKotlinxSerialization
import io.github.smiley4.schemakenerator.swagger.OptionalHandling
import io.github.smiley4.schemakenerator.swagger.compileInlining
import io.github.smiley4.schemakenerator.swagger.generateSwaggerSchema
import io.kotest.core.spec.style.StringSpec
import io.swagger.v3.oas.models.media.Schema
import kotlinx.serialization.Serializable
import kotlin.reflect.typeOf

/**
 * internal / manual tests only
 */
class Test : StringSpec({

    "test" {
        val result = typeOf<CreateProduct>()
            .processKotlinxSerialization()
            .generateSwaggerSchema {
                optionalHandling = OptionalHandling.NON_REQUIRED
            }
            .compileInlining()
            .let {
                SwaggerResult(
                    root = it.swagger,
                    componentSchemas = it.componentSchemas
                )
            }

        /*
        * BUG: https://github.com/SMILEY4/schema-kenerator/issues/16
        * - "name" is marked as not required (nullable)
        * - "description" processed first, registered with id "String" and nullable = true
        * - "name" processed after -> reuses type with id "String" -> type is nullable even though name isnt
        * */


        println(json.writeValueAsString(result))

    }

}) {
    companion object {

        class SwaggerResult(
            val root: Schema<*>,
            val componentSchemas: Map<String, Schema<*>>
        )

        @Serializable
        data class CreateProduct(
            val name: String,
            val description: String? = null
        )

        private val json = jacksonObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL).writerWithDefaultPrettyPrinter()!!

    }
}
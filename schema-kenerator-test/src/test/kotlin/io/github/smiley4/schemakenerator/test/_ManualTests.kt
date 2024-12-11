@file:OptIn(ExperimentalSerializationApi::class)
@file:Suppress("ClassName")

package io.github.smiley4.schemakenerator.test

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.smiley4.schemakenerator.jsonschema.compileInlining
import io.github.smiley4.schemakenerator.jsonschema.data.TitleType
import io.github.smiley4.schemakenerator.jsonschema.generateJsonSchema
import io.github.smiley4.schemakenerator.jsonschema.withTitle
import io.github.smiley4.schemakenerator.reflection.processReflection
import io.github.smiley4.schemakenerator.serialization.processKotlinxSerialization
import io.github.smiley4.schemakenerator.swagger.compileInlining
import io.github.smiley4.schemakenerator.swagger.data.CompiledSwaggerSchema
import io.kotest.core.spec.style.StringSpec
import io.swagger.v3.core.util.Json31
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlin.reflect.typeOf

/**
 * internal / manual tests only
 */
class _ManualTests : StringSpec({

    "test" {
        val result = typeOf<Generic<Int>>()
            .processKotlinxSerialization()
            .generateJsonSchema()
            .withTitle(TitleType.SIMPLE)
            .compileInlining()
            .json
            .prettyPrint()

        println(result)
    }

}) {
    companion object {

        @Serializable
        data class Parent(val data: Generic<Int>)

        @Serializable
        data class Generic<T>(
            val item: T,
        )

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


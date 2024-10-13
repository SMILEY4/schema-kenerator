@file:OptIn(ExperimentalSerializationApi::class)
@file:Suppress("ClassName")

package io.github.smiley4.schemakenerator.test

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.smiley4.schemakenerator.reflection.processReflection
import io.github.smiley4.schemakenerator.swagger.compileReferencing
import io.github.smiley4.schemakenerator.swagger.data.CompiledSwaggerSchema
import io.github.smiley4.schemakenerator.swagger.generateSwaggerSchema
import io.github.smiley4.schemakenerator.swagger.handleSchemaAnnotations
import io.kotest.core.spec.style.StringSpec
import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
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
        val result = typeOf<TestClass>()
            .processReflection()
            .generateSwaggerSchema()
            .handleSchemaAnnotations()
            .compileReferencing()
            .asPrintable()

        println(json.writeValueAsString(result))
    }

}) {
    companion object {

        @JvmInline
        @Serializable
        @Schema(
            format = "hex",
            description = "Represents a hexadecimal string."
        )
        value class HexString private constructor(
            val value: String
        )

        class TestClass(
            val myHex: HexString
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
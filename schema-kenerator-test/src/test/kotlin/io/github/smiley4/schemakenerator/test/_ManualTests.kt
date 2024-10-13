@file:OptIn(ExperimentalSerializationApi::class)
@file:Suppress("ClassName")

package io.github.smiley4.schemakenerator.test

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.smiley4.schemakenerator.reflection.processReflection
import io.github.smiley4.schemakenerator.swagger.compileInlining
import io.github.smiley4.schemakenerator.swagger.compileReferencing
import io.github.smiley4.schemakenerator.swagger.data.CompiledSwaggerSchema
import io.github.smiley4.schemakenerator.swagger.generateSwaggerSchema
import io.github.smiley4.schemakenerator.swagger.handleSchemaAnnotations
import io.kotest.core.spec.style.StringSpec
import io.swagger.v3.core.util.Json31
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
            .compileInlining()
            .asPrintable()

        println(json.writeValueAsString(result))

        println("===========")

        println(Json31.prettyPrint(result))

    }

}) {
    companion object {

        enum class ColorEnum {
            RED, GREEN, BLUE
        }

        class TestClass(
            val someColor: ColorEnum?
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
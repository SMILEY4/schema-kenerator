@file:Suppress("ClassName")

package io.github.smiley4.schemakenerator.test

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.smiley4.schemakenerator.core.connectSubTypes
import io.github.smiley4.schemakenerator.core.renameProperties
import io.github.smiley4.schemakenerator.reflection.processReflection
import io.github.smiley4.schemakenerator.swagger.compileInlining
import io.github.smiley4.schemakenerator.swagger.data.CompiledSwaggerSchema
import io.github.smiley4.schemakenerator.swagger.data.TitleType
import io.github.smiley4.schemakenerator.swagger.generateSwaggerSchema
import io.github.smiley4.schemakenerator.swagger.handleSchemaAnnotations
import io.github.smiley4.schemakenerator.swagger.withTitle
import io.kotest.core.spec.style.StringSpec
import io.swagger.v3.oas.annotations.media.Schema
import kotlin.reflect.typeOf

/**
 * internal / manual tests only
 */
class _ManualTests : StringSpec({

    "test root" {
        val result = typeOf<Example>()
            .processReflection()
            .connectSubTypes()
            .generateSwaggerSchema()
            .withTitle(TitleType.SIMPLE)
            .handleSchemaAnnotations()
            .compileInlining()
            .asPrintable()

        println(json.writeValueAsString(result))
    }

}) {
    companion object {

        data class Example(
            @Schema(allowableValues = ["Allowable values work here"], name = "This name annotation does not work")
            val myProperty: String
        )

        data class Root(
            val withInt: MyInterface.WithInt?,
            val withEnum: MyInterface.WithEnum?,
            val intHolder: IntHolder,
        )

        enum class MyEnum {
            Alpha, Beta,
        }

        sealed interface MyInterface<T> {
            val data: T

            data class WithInt(
                override val data: Int
            ) : MyInterface<Int>

            data class WithEnum(
                override val data: MyEnum
            ) : MyInterface<MyEnum>
        }

        interface SimpleInterface {
            val value: Number
        }

        data class IntHolder(
            override val value: Int
        ) : SimpleInterface

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


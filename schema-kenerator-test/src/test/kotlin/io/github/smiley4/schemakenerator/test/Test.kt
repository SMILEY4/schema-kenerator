package io.github.smiley4.schemakenerator.test

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.smiley4.schemakenerator.core.data.AnnotationData
import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.PrimitiveTypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.serialization.processKotlinxSerialization
import io.github.smiley4.schemakenerator.swagger.compileInlining
import io.github.smiley4.schemakenerator.swagger.customizeTypes
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema
import io.github.smiley4.schemakenerator.swagger.generateSwaggerSchema
import io.github.smiley4.schemakenerator.swagger.steps.buildTypeDataMap
import io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithLocalDateTime
import io.kotest.core.spec.style.StringSpec
import java.time.LocalDateTime
import kotlin.reflect.typeOf

class Test : StringSpec({

    "before" {

        fun process(schema: SwaggerSchema, typeDataMap: Map<TypeId, BaseTypeData>) {
            schema.typeData.annotations
                .find { it.name == "swagger-format" } // find our custom annotation
                ?.also { annotation ->
                    schema.swagger.format = annotation.values["format"] as String // set the "format"-property of the swagger-schema
                }
        }

        val result = typeOf<ClassWithLocalDateTime>()
            .processKotlinxSerialization {
                customProcessor<LocalDateTime> {
                    PrimitiveTypeData(
                        id = TypeId.build(String::class.qualifiedName!!),
                        simpleName = String::class.simpleName!!,
                        qualifiedName = String::class.qualifiedName!!,
                        annotations = mutableListOf(
                            AnnotationData(
                                name = "swagger-format",
                                values = mutableMapOf("format" to "date-time"),
                                annotation = null
                            )
                        )
                    )
                }
            }
            .generateSwaggerSchema()
            .let { bundle ->
                val typeDataMap = bundle.buildTypeDataMap()
                bundle.also { schema ->
                    process(schema.data, typeDataMap)
                    schema.supporting.forEach { process(it, typeDataMap) }
                }
            }
            .compileInlining()


        println(json.writeValueAsString(result.swagger))
        result.componentSchemas.forEach { (name, schema) ->
            println("$name: ${json.writeValueAsString(schema)}")
        }
    }


    "after" {

        val result = typeOf<ClassWithLocalDateTime>()
            .processKotlinxSerialization {
                customProcessor<LocalDateTime> {
                    PrimitiveTypeData(
                        id = TypeId.build(String::class.qualifiedName!!),
                        simpleName = String::class.simpleName!!,
                        qualifiedName = String::class.qualifiedName!!,
                        annotations = mutableListOf(
                            AnnotationData(
                                name = "swagger-format",
                                values = mutableMapOf("format" to "date-time"),
                                annotation = null
                            )
                        )
                    )
                }
            }
            .generateSwaggerSchema()
            .customizeTypes { type, schema ->
                type.annotations.find { it.name == "swagger-format" }?.also { annotation ->
                    schema.format = annotation.values["format"] as String
                }
            }
            .compileInlining()


        println(json.writeValueAsString(result.swagger))
        result.componentSchemas.forEach { (name, schema) ->
            println("$name: ${json.writeValueAsString(schema)}")
        }
    }

}) {
    companion object {

        private val json = jacksonObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL).writerWithDefaultPrettyPrinter()!!


    }
}
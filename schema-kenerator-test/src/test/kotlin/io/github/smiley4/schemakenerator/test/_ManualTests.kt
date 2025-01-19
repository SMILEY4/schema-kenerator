@file:Suppress("ClassName")

package io.github.smiley4.schemakenerator.test

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.smiley4.schemakenerator.core.annotations.Format
import io.github.smiley4.schemakenerator.core.annotations.Title
import io.github.smiley4.schemakenerator.core.annotations.Type
import io.github.smiley4.schemakenerator.core.connectSubTypes
import io.github.smiley4.schemakenerator.core.data.PrimitiveTypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.core.handleNameAnnotation
import io.github.smiley4.schemakenerator.reflection.collectSubTypes
import io.github.smiley4.schemakenerator.reflection.processReflection
import io.github.smiley4.schemakenerator.swagger.compileReferencingRoot
import io.github.smiley4.schemakenerator.swagger.data.CompiledSwaggerSchema
import io.github.smiley4.schemakenerator.swagger.data.TitleType
import io.github.smiley4.schemakenerator.swagger.generateSwaggerSchema
import io.github.smiley4.schemakenerator.swagger.handleCoreAnnotations
import io.github.smiley4.schemakenerator.swagger.mergePropertyAttributesIntoType
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerMergePropertyAttributesStep
import io.github.smiley4.schemakenerator.swagger.withTitle
import io.kotest.core.spec.style.StringSpec
import kotlinx.serialization.Serializable
import kotlin.reflect.typeOf

/**
 * internal / manual tests only
 */
class _ManualTests : StringSpec({

    "with default ktor-openapi generator" {
        val result = typeOf<MyInstantClass>()
            .collectSubTypes()
            .processReflection()
            .connectSubTypes()
            .handleNameAnnotation()
            .generateSwaggerSchema()
            .handleCoreAnnotations()
            .withTitle(TitleType.SIMPLE)
            .compileReferencingRoot()
            .asPrintable()
            .let { json.writeValueAsString(it) }

        println(result)
    }

    "with default ktor-openapi generator (fix)" {
        val result = typeOf<MyInstantClass>()
            .collectSubTypes()
            .processReflection()
            .connectSubTypes()
            .handleNameAnnotation()
            .generateSwaggerSchema()
            .handleCoreAnnotations()
            .withTitle(TitleType.SIMPLE)
            .mergePropertyAttributesIntoType()
            .compileReferencingRoot()
            .asPrintable()
            .let { json.writeValueAsString(it) }

        println(result)
    }

    "with custom generator" {
        val result = typeOf<MyInstantClass>()
            .collectSubTypes()
            .processReflection {
                customProcessor<DummyInstant> {
                    PrimitiveTypeData(
                        id = TypeId.build(DummyInstant::class.qualifiedName!!),
                        simpleName = DummyInstant::class.simpleName!!,
                        qualifiedName = DummyInstant::class.qualifiedName!!,
                    )
                }
            }
            .connectSubTypes()
            .handleNameAnnotation()
            .generateSwaggerSchema()
            .handleCoreAnnotations()
            .withTitle(TitleType.SIMPLE)
            .let { SwaggerMergePropertyAttributesStep().process(it) }
            .compileReferencingRoot()
            .asPrintable()
            .let { json.writeValueAsString(it) }

        println(result)
    }

}) {
    companion object {

        @Serializable
        @Title("My title")
        @Type("string")
        data class MyInstantClass(
            @Type("string")
            @Format("date-time")
            val time: DummyInstant
        )


        @Serializable
        class DummyInstant

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

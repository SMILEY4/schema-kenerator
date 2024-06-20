package io.github.smiley4.schemakenerator.test

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.smiley4.schemakenerator.core.connectSubTypes
import io.github.smiley4.schemakenerator.core.handleNameAnnotation
import io.github.smiley4.schemakenerator.jsonschema.compileReferencingRoot
import io.github.smiley4.schemakenerator.jsonschema.generateJsonSchema
import io.github.smiley4.schemakenerator.jsonschema.handleCoreAnnotations
import io.github.smiley4.schemakenerator.reflection.collectSubTypes
import io.github.smiley4.schemakenerator.reflection.processReflection
import io.github.smiley4.schemakenerator.serialization.steps.KotlinxSerializationTypeProcessingStep
import io.github.smiley4.schemakenerator.swagger.compileReferencingRoot
import io.github.smiley4.schemakenerator.swagger.generateSwaggerSchema
import io.github.smiley4.schemakenerator.swagger.handleCoreAnnotations
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCompileReferenceRootStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaGenerationStep
import io.github.smiley4.schemakenerator.test.models.kotlinx.ClassDirectSelfReferencing
import io.kotest.core.spec.style.StringSpec
import kotlin.reflect.typeOf

class Test : StringSpec({

    "kotlinx processing infinite loop" {

        val result = typeOf<ClassDirectSelfReferencing>()
            .let { KotlinxSerializationTypeProcessingStep().process(it) }
            .let { SwaggerSchemaGenerationStep().generate(it) }
            .let { SwaggerSchemaCompileReferenceRootStep().compile(it) }

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
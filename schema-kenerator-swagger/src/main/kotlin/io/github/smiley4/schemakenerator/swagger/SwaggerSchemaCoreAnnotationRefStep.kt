package io.github.smiley4.schemakenerator.swagger

import io.github.smiley4.schemakenerator.core.annotations.Ref
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.swagger.SwaggerSchemaAnnotationUtils.iterateProperties
import io.github.smiley4.schemakenerator.swagger.data.IntermediateSwaggerSchemaData
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchemaData
import io.swagger.v3.oas.models.media.Schema
import jdk.javadoc.internal.doclets.formats.html.markup.HtmlStyle

internal class SwaggerSchemaCoreAnnotationRefStep {

    fun process(input: IntermediateSwaggerSchemaData): IntermediateSwaggerSchemaData {
        return IntermediateSwaggerSchemaData(
            rootId = input.rootId,
            data = input.data.mapValues { process(it.value) }
        )
    }

    private fun process(schema: SwaggerSchemaData): SwaggerSchemaData {
        determineRef(schema.typeData)?.also { refUrl ->
            return SwaggerSchemaData(
                swagger = Schema<Any>().also {
                    it.`raw$ref`(refUrl)
                },
                typeData = schema.typeData
            )
        }
        return schema
    }

    private fun determineRef(typeData: TypeData): String? {
        return typeData.annotations
            .filter { it.name == Ref::class.qualifiedName }
            .map { it.values["url"] as String }
            .firstOrNull()
    }

}

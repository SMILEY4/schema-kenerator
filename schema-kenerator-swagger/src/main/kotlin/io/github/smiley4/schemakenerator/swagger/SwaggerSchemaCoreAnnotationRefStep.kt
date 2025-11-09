package io.github.smiley4.schemakenerator.swagger

import io.github.smiley4.schemakenerator.core.annotations.Ref
import io.github.smiley4.schemakenerator.core.data.AnnotationData
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.core.data.TypeName
import io.github.smiley4.schemakenerator.swagger.SwaggerSchemaAnnotationUtils.iterateProperties
import io.github.smiley4.schemakenerator.swagger.data.IntermediateSwaggerSchemaData
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchemaData
import io.swagger.v3.oas.models.media.Schema

internal class SwaggerSchemaCoreAnnotationRefStep {
    
    fun process(input: IntermediateSwaggerSchemaData): IntermediateSwaggerSchemaData {
        val typeDataMap = input.typeDataById
        return IntermediateSwaggerSchemaData(
            rootId = input.rootId,
            data = buildMap {
                input.data.forEach { (key, value) ->
                    val data = process(value, typeDataMap)
                    data.forEach {
                        this[it.typeData.id] = it
                    }
                }
            }
        )
    }

    private fun process(schema: SwaggerSchemaData, typeDataMap: Map<TypeId, TypeData>): List<SwaggerSchemaData> {

        determineRef(schema.typeData.annotations)?.also { refUrl ->
            return listOf(
                SwaggerSchemaData(
                    swagger = Schema<Any>().also {
                        it.`raw$ref`(refUrl)
                    },
                    typeData = schema.typeData
                )
            )
        }

        val result = mutableListOf(schema)

        iterateProperties(schema, typeDataMap) { prop, propData, propTypeData ->
            determineRef(propData.annotations + propTypeData.annotations)?.also { refUrl ->
                val newData = SwaggerSchemaData(
                    swagger = Schema<Any>().also {
                        it.`raw$ref`(refUrl)
                    },
                    typeData = createRefTypeData(refUrl, propTypeData.descriptiveName)
                )
                propData.type = newData.typeData.id
                prop.`raw$ref`(newData.typeData.id.id)
                result.add(newData)
            }
        }

        return result
    }

    private fun determineRef(annotations: Collection<AnnotationData>): String? {
        return annotations
            .filter { it.name == Ref::class.qualifiedName }
            .map { it.values["url"] as String }
            .firstOrNull()
    }

    private fun createRefTypeData(url: String, descriptiveName: TypeName): TypeData {
        return TypeData(
            id = TypeId.create(),
            identifyingName = TypeName(
                full = Any::class.qualifiedName!!,
                short = Any::class.simpleName!!
            ),
            descriptiveName = descriptiveName,
            annotations = mutableListOf(
                AnnotationData(
                    name = Ref::class.qualifiedName!!,
                    values = mutableMapOf(
                        "url" to url
                    )
                )
            )
        )
    }

}

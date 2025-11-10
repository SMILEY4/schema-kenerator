package io.github.smiley4.schemakenerator.jsonschema

import io.github.smiley4.schemakenerator.core.annotations.Ref
import io.github.smiley4.schemakenerator.core.data.AnnotationData
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.core.data.TypeName
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaAnnotationUtils.iterateProperties
import io.github.smiley4.schemakenerator.jsonschema.data.IntermediateJsonSchemaData
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchemaData
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonObject
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonTextValue
import kotlin.collections.forEach

/**
 * Handles the core [Ref] annotation
 */
internal class JsonSchemaCoreAnnotationRefStep {

    fun process(input: IntermediateJsonSchemaData): IntermediateJsonSchemaData {
        val typeDataMap = input.typeDataById
        return IntermediateJsonSchemaData(
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

    private fun process(schema: JsonSchemaData, typeDataMap: Map<TypeId, TypeData>): List<JsonSchemaData> {
        if (schema.json is JsonObject) {
            determineRef(schema.typeData.annotations)?.also { refUrl ->
                return listOf(JsonSchemaData(
                    json = JsonObject(mutableMapOf(
                        "${'$'}ref" to JsonTextValue(refUrl)
                    )),
                    typeData = schema.typeData,
                ))
            }
        }

        val result =  mutableListOf(schema)
        iterateProperties(schema, typeDataMap) { prop, propData, propTypeData ->
            determineRef(propData.annotations + propTypeData.annotations)?.also { refUrl ->
                val newData = JsonSchemaData(
                    json = JsonObject(mutableMapOf(
                        "${'$'}ref" to JsonTextValue(refUrl)
                    )),
                    typeData = createRefTypeData(refUrl, propTypeData.descriptiveName)
                )
                result.add(newData)
                propData.type = newData.typeData.id
                prop.properties["${'$'}ref"] = JsonTextValue(newData.typeData.id.id)
            }
        }

        return result
    }

    private fun determineRef(annotations: List<AnnotationData>): String? {
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

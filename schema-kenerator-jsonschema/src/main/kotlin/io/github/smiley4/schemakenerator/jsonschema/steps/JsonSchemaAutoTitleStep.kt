package io.github.smiley4.schemakenerator.jsonschema.steps

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchema
import io.github.smiley4.schemakenerator.jsonschema.data.TitleType
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonObject
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonTextValue

/**
 * Adds an automatically determined title to schemas.
 * @param type the type of the title
 */
class JsonSchemaAutoTitleStep(val type: TitleType = TitleType.FULL) {

    fun process(bundle: Bundle<JsonSchema>): Bundle<JsonSchema> {
        val typeDataMap = bundle.buildTypeDataMap()
        return bundle.also { schema ->
            process(schema.data, typeDataMap)
            schema.supporting.forEach { process(it, typeDataMap) }
        }
    }

    private fun process(schema: JsonSchema, typeDataMap: Map<TypeId, BaseTypeData>) {
        if (schema.json is JsonObject && schema.json.properties["title"] == null) {
            schema.json.properties["title"] = JsonTextValue(determineTitle(schema.typeData, typeDataMap))
        }
    }

    private fun determineTitle(typeData: BaseTypeData, typeDataMap: Map<TypeId, BaseTypeData>): String {
        return when (type) {
            TitleType.FULL -> typeData.qualifiedName
            TitleType.SIMPLE -> typeData.simpleName
        }.let {
            if (typeData.typeParameters.isNotEmpty()) {
                val paramString = typeData.typeParameters
                    .map { (_, param) -> determineTitle(typeDataMap[param.type]!!, typeDataMap) }
                    .joinToString(",")
                "$it<$paramString>"
            } else {
                it
            }
        }.let {
            it + (typeData.id.additionalId?.let { a -> "#$a" } ?: "")
        }
    }

}

package io.github.smiley4.schemakenerator.jsonschema.steps

import io.github.smiley4.schemakenerator.core.annotations.SchemaTitle
import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchema
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonObject
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonTextValue

/**
 * Adds a title specified by the [SchemaTitle]-annotation
 */
class JsonSchemaCoreAnnotationTitleStep {

    fun process(bundle: Bundle<JsonSchema>): Bundle<JsonSchema> {
        return bundle.also { schema ->
            process(schema.data)
            schema.supporting.forEach { process(it) }
        }
    }

    private fun process(schema: JsonSchema) {
        if (schema.json is JsonObject && schema.json.properties["title"] == null) {
            determineTitle(schema.typeData)?.also { title ->
                schema.json.properties["title"] = JsonTextValue(title)
            }
        }
    }

    private fun determineTitle(typeData: BaseTypeData): String? {
        return typeData.annotations
            .filter { it.name == SchemaTitle::class.qualifiedName }
            .map { it.values["title"] as String }
            .firstOrNull()
    }

}

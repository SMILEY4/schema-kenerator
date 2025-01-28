package io.github.smiley4.schemakenerator.jsonschema

import io.github.smiley4.schemakenerator.core.GenericBundleIndependentContentStep
import io.github.smiley4.schemakenerator.core.annotations.Title
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchema
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonObject
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonTextValue

/**
 * Adds a title specified by the [Title]-annotation
 */
class JsonSchemaCoreAnnotationTitleStep : GenericBundleIndependentContentStep<JsonSchema>() {

    override fun process(input: JsonSchema) {
        if (input.json is JsonObject && input.json.properties["title"] == null) {
            determineTitle(input.typeData)?.also { title ->
                input.json.properties["title"] = JsonTextValue(title)
            }
        }
    }

    private fun determineTitle(typeData: TypeData): String? {
        return typeData.annotations
            .filter { it.name == Title::class.qualifiedName }
            .map { it.values["title"] as String }
            .firstOrNull()
    }

}

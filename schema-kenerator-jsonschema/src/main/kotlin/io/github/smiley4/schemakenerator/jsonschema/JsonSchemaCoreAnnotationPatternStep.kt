package io.github.smiley4.schemakenerator.jsonschema

import io.github.smiley4.schemakenerator.core.annotations.Pattern
import io.github.smiley4.schemakenerator.core.data.AnnotationData
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaAnnotationUtils.iterateProperties
import io.github.smiley4.schemakenerator.jsonschema.data.IntermediateJsonSchemaData
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchemaData
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonTextValue

/**
 * Handles the core [Pattern] annotation.
 */
internal class JsonSchemaCoreAnnotationPatternStep {

    fun process(input: IntermediateJsonSchemaData): IntermediateJsonSchemaData {
        input.entries.forEach { process(it, input.typeDataById) }
        return input
    }

    private fun process(schema: JsonSchemaData, typeDataMap: Map<TypeId, TypeData>) {
        iterateProperties(schema, typeDataMap) { prop, propData, propTypeData ->
            determineValue(propData.annotations + propTypeData.annotations)?.also { value ->
                prop.properties["pattern"] = JsonTextValue(value)
            }
        }
    }

    private fun determineValue(annotations: List<AnnotationData>): String? {
        return annotations
            .filter { it.name == Pattern::class.qualifiedName }
            .map { it.values["value"] as String }
            .firstOrNull()
    }
}

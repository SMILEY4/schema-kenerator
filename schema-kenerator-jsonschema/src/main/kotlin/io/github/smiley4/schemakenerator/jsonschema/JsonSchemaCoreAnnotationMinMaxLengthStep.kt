package io.github.smiley4.schemakenerator.jsonschema

import io.github.smiley4.schemakenerator.core.annotations.MaxLength
import io.github.smiley4.schemakenerator.core.annotations.MinLength
import io.github.smiley4.schemakenerator.core.data.AnnotationData
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaAnnotationUtils.iterateProperties
import io.github.smiley4.schemakenerator.jsonschema.data.IntermediateJsonSchemaData
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchemaData
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonNumericValue

/**
 * Handles the core [MinLength] and [MaxLength] annotations for string and arrays.
 */
internal class JsonSchemaCoreAnnotationMinMaxLengthStep {

    fun process(input: IntermediateJsonSchemaData): IntermediateJsonSchemaData {
        input.entries.forEach { process(it, input.typeDataById) }
        return input
    }

    private fun process(schema: JsonSchemaData, typeDataMap: Map<TypeId, TypeData>) {
        process(schema, typeDataMap, "minLength", "minItems") { determineMinLength(it) }
        process(schema, typeDataMap, "maxLength", "maxItems") { determineMaxLength(it) }
    }

    private fun process(
        schema: JsonSchemaData,
        typeDataMap: Map<TypeId, TypeData>,
        propertyNameString: String,
        propertyNameArray: String,
        determineValue: (annotations: List<AnnotationData>) -> Long?
    ) {
        iterateProperties(schema, typeDataMap) { prop, propData, propTypeData ->
            determineValue(propData.annotations + propTypeData.annotations)?.also { value ->
                val propName = if (propTypeData.isCollection) propertyNameArray else propertyNameString
                prop.properties[propName] = JsonNumericValue(value)
            }
        }
    }

    private fun determineMinLength(annotations: List<AnnotationData>): Long? {
        return annotations
            .filter { it.name == MinLength::class.qualifiedName }
            .map { it.values["value"] as Long }
            .firstOrNull()
    }

    private fun determineMaxLength(annotations: List<AnnotationData>): Long? {
        return annotations
            .filter { it.name == MaxLength::class.qualifiedName }
            .map { it.values["value"] as Long }
            .firstOrNull()
    }
}

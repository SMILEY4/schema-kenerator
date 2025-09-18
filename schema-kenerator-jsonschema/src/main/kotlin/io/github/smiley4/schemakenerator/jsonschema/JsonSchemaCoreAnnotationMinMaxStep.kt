package io.github.smiley4.schemakenerator.jsonschema

import io.github.smiley4.schemakenerator.core.annotations.ExclusiveMax
import io.github.smiley4.schemakenerator.core.annotations.ExclusiveMin
import io.github.smiley4.schemakenerator.core.annotations.Max
import io.github.smiley4.schemakenerator.core.annotations.Min
import io.github.smiley4.schemakenerator.core.data.AnnotationData
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaAnnotationUtils.iterateProperties
import io.github.smiley4.schemakenerator.jsonschema.data.IntermediateJsonSchemaData
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchemaData
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonNumericValue

/**
 * Handles the core [Min], [ExclusiveMin], [Max] and [ExclusiveMax] annotations.
 */
internal class JsonSchemaCoreAnnotationMinMaxStep {

    fun process(input: IntermediateJsonSchemaData): IntermediateJsonSchemaData {
        input.entries.forEach { process(it, input.typeDataById) }
        return input
    }

    private fun process(schema: JsonSchemaData, typeDataMap: Map<TypeId, TypeData>) {
        process(schema, typeDataMap, "minimum") { determineMin(it) }
        process(schema, typeDataMap, "maximum") { determineMax(it) }
        process(schema, typeDataMap, "exclusiveMinimum") { determineExclusiveMin(it) }
        process(schema, typeDataMap, "exclusiveMaximum") { determineExclusiveMax(it) }
    }

    private fun process(
        schema: JsonSchemaData,
        typeDataMap: Map<TypeId, TypeData>,
        propertyName: String,
        determineValue: (annotations: List<AnnotationData>) -> Long?
    ) {
        iterateProperties(schema, typeDataMap) { prop, propData, propTypeData ->
            determineValue(propData.annotations + propTypeData.annotations)?.also { value ->
                prop.properties[propertyName] = JsonNumericValue(value)
            }
        }
    }

    private fun determineMin(annotations: List<AnnotationData>): Long? {
        return annotations
            .filter { it.name == Min::class.qualifiedName }
            .map { it.values["value"] as Long }
            .firstOrNull()
    }

    private fun determineExclusiveMin(annotations: List<AnnotationData>): Long? {
        return annotations
            .filter { it.name == ExclusiveMin::class.qualifiedName }
            .map { it.values["value"] as Long }
            .firstOrNull()
    }

    private fun determineMax(annotations: List<AnnotationData>): Long? {
        return annotations
            .filter { it.name == Max::class.qualifiedName }
            .map { it.values["value"] as Long }
            .firstOrNull()
    }

    private fun determineExclusiveMax(annotations: List<AnnotationData>): Long? {
        return annotations
            .filter { it.name == ExclusiveMax::class.qualifiedName }
            .map { it.values["value"] as Long }
            .firstOrNull()
    }
}

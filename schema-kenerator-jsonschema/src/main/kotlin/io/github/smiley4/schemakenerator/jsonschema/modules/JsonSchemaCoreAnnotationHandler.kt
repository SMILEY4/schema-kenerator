package io.github.smiley4.schemakenerator.jsonschema.modules

import io.github.smiley4.schemakenerator.core.annotations.SchemaDefault
import io.github.smiley4.schemakenerator.core.annotations.SchemaDeprecated
import io.github.smiley4.schemakenerator.core.annotations.SchemaDescription
import io.github.smiley4.schemakenerator.core.annotations.SchemaExample
import io.github.smiley4.schemakenerator.core.annotations.SchemaTitle
import io.github.smiley4.schemakenerator.core.data.AnnotationData
import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.ObjectTypeData
import io.github.smiley4.schemakenerator.core.data.PropertyData
import io.github.smiley4.schemakenerator.jsonschema.json.JsonArray
import io.github.smiley4.schemakenerator.jsonschema.json.JsonBooleanValue
import io.github.smiley4.schemakenerator.jsonschema.json.JsonObject
import io.github.smiley4.schemakenerator.jsonschema.json.JsonTextValue
import io.github.smiley4.schemakenerator.jsonschema.schema.JsonSchema

class JsonSchemaCoreAnnotationHandler {

    fun appendTitle(schemas: Collection<JsonSchema>): List<JsonSchema> {
        schemas.forEach { appendTitle(it) }
        return schemas.toList()
    }

    fun appendDescription(schemas: Collection<JsonSchema>): List<JsonSchema> {
        schemas.forEach { appendDescription(it) }
        return schemas.toList()
    }

    fun appendDeprecated(schemas: Collection<JsonSchema>): List<JsonSchema> {
        schemas.forEach { appendDeprecated(it) }
        return schemas.toList()
    }

    fun appendExamples(schemas: Collection<JsonSchema>): List<JsonSchema> {
        schemas.forEach { appendExamples(it) }
        return schemas.toList()
    }

    fun appendDefaults(schemas: Collection<JsonSchema>): List<JsonSchema> {
        schemas.forEach { appendDefaults(it) }
        return schemas.toList()
    }

    //===== TITLE ==================================================

    private fun appendTitle(schema: JsonSchema) {
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

    //===== DESCRIPTION ============================================

    private fun appendDescription(schema: JsonSchema) {
        if (schema.json is JsonObject && schema.json.properties["description"] == null) {
            determineDescription(schema.typeData)?.also { description ->
                schema.json.properties["description"] = JsonTextValue(description)
            }
        }
        iterateProperties(schema) { prop, data ->
            determineDescription(data)?.also { description ->
                prop.properties["description"] = JsonTextValue(description)
            }
        }
    }

    private fun determineDescription(typeData: BaseTypeData): String? {
        return typeData.annotations
            .filter { it.name == SchemaDescription::class.qualifiedName }
            .map { it.values["description"] as String }
            .firstOrNull()
    }

    private fun determineDescription(typeData: PropertyData): String? {
        return typeData.annotations
            .filter { it.name == SchemaDescription::class.qualifiedName }
            .map { it.values["description"] as String }
            .firstOrNull()
    }

    //===== DEPRECATED =============================================

    private fun appendDeprecated(schema: JsonSchema) {
        if (schema.json is JsonObject && schema.json.properties["deprecated"] == null) {
            determineDeprecated(schema.typeData)?.also { deprecated ->
                schema.json.properties["deprecated"] = JsonBooleanValue(deprecated)
            }
        }
        iterateProperties(schema) { prop, data ->
            determineDeprecated(data)?.also { deprecated ->
                prop.properties["deprecated"] = JsonBooleanValue(deprecated)
            }
        }
    }

    private fun determineDeprecated(typeData: BaseTypeData): Boolean? {
        return determineDeprecatedCore(typeData.annotations) ?: determineDeprecatedStd(typeData.annotations)
    }

    private fun determineDeprecated(typeData: PropertyData): Boolean? {
        return determineDeprecatedCore(typeData.annotations) ?: determineDeprecatedStd(typeData.annotations)
    }

    private fun determineDeprecatedCore(annotations: Collection<AnnotationData>): Boolean? {
        return annotations
            .filter { it.name == SchemaDeprecated::class.qualifiedName }
            .map { it.values["deprecated"] as Boolean }
            .firstOrNull()
    }

    private fun determineDeprecatedStd(annotations: Collection<AnnotationData>): Boolean? {
        return if (annotations.any { it.name == Deprecated::class.qualifiedName }) {
            true
        } else {
            null
        }
    }

    //===== EXAMPLES ===============================================

    private fun appendExamples(schema: JsonSchema) {
        if (schema.json is JsonObject && schema.json.properties["examples"] == null) {
            determineExamples(schema.typeData)?.also { examples ->
                schema.json.properties["examples"] = JsonArray().also { arr -> arr.items.addAll(examples.map { JsonTextValue(it) }) }
            }
        }
        iterateProperties(schema) { prop, data ->
            determineExamples(data)?.also { examples ->
                prop.properties["examples"] = JsonArray().also { arr -> arr.items.addAll(examples.map { JsonTextValue(it) }) }
            }
        }
    }

    private fun determineExamples(typeData: PropertyData): List<String>? {
        return typeData.annotations
            .filter { it.name == SchemaExample::class.qualifiedName }
            .map { it.values["example"] as String }
            .let {
                it.ifEmpty {
                    null
                }
            }
    }

    private fun determineExamples(typeData: BaseTypeData): List<String>? {
        return typeData.annotations
            .filter { it.name == SchemaExample::class.qualifiedName }
            .map { it.values["example"] as String }
            .let {
                it.ifEmpty {
                    null
                }
            }
    }

    //===== DEFAULT ================================================

    private fun appendDefaults(schema: JsonSchema) {
        if (schema.json is JsonObject && schema.json.properties["default"] == null) {
            determineDefaults(schema.typeData)?.also { default ->
                schema.json.properties["default"] = JsonTextValue(default)
            }
        }
    }

    private fun determineDefaults(typeData: BaseTypeData): String? {
        return typeData.annotations
            .filter { it.name == SchemaDefault::class.qualifiedName }
            .map { it.values["default"] as String }
            .firstOrNull()
    }

    //===== UTILS ==================================================

    private fun iterateProperties(schema: JsonSchema, action: (property: JsonObject, data: PropertyData) -> Unit) {
        if (schema.typeData is ObjectTypeData && schema.json is JsonObject && schema.json.properties.containsKey("properties")) {
            (schema.json.properties["properties"] as JsonObject).properties.forEach { (propKey, prop) ->
                schema.typeData.members.find { it.name == propKey }?.also { propertyData ->
                    action(prop as JsonObject, propertyData)
                }
            }
        }
    }

}
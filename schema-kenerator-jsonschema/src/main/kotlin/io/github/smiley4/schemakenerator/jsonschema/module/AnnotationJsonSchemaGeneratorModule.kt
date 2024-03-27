package io.github.smiley4.schemakenerator.jsonschema.module

import io.github.smiley4.schemakenerator.core.annotations.SchemaDefault
import io.github.smiley4.schemakenerator.core.annotations.SchemaDeprecated
import io.github.smiley4.schemakenerator.core.annotations.SchemaDescription
import io.github.smiley4.schemakenerator.core.annotations.SchemaExample
import io.github.smiley4.schemakenerator.core.annotations.SchemaTitle
import io.github.smiley4.schemakenerator.core.parser.BaseTypeData
import io.github.smiley4.schemakenerator.core.parser.ObjectTypeData
import io.github.smiley4.schemakenerator.core.parser.TypeDataContext
import io.github.smiley4.schemakenerator.core.parser.resolve
import io.github.smiley4.schemakenerator.jsonschema.JsonSchema
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaGenerator
import io.github.smiley4.schemakenerator.jsonschema.getByRefOrThrow
import io.github.smiley4.schemakenerator.jsonschema.json.JsonArray
import io.github.smiley4.schemakenerator.jsonschema.json.JsonBooleanValue
import io.github.smiley4.schemakenerator.jsonschema.json.JsonNode
import io.github.smiley4.schemakenerator.jsonschema.json.JsonObject
import io.github.smiley4.schemakenerator.jsonschema.json.JsonTextValue

/**
 * Adds additional metadata to the json-schema
 * - title from [SchemaTitle] or detects the title of objects/classes automatically as specified in [AnnotationJsonSchemaGeneratorModule.autoTitle]
 * - example from [SchemaTitle]
 * - default from [SchemaDefault]
 * - deprecated from [SchemaDeprecated] or [Deprecated]
 */
class AnnotationJsonSchemaGeneratorModule(private val autoTitle: AutoTitle = AutoTitle.SIMPLE_NAME) : JsonSchemaGeneratorModule {

    companion object {
        enum class AutoTitle {
            NONE,
            SIMPLE_NAME,
            QUALIFIED_NAME
        }
    }

    override fun build(generator: JsonSchemaGenerator, context: TypeDataContext, typeData: BaseTypeData, depth: Int): JsonSchema? = null

    override fun enhance(generator: JsonSchemaGenerator, context: TypeDataContext, typeData: BaseTypeData, schema: JsonSchema, depth: Int) {
        appendDescription(typeData, resolveSchemaDefinition(schema))
        appendExample(typeData, resolveSchemaDefinition(schema))
        appendDefault(typeData, resolveSchemaDefinition(schema))
        appendDeprecated(typeData, resolveSchemaDefinition(schema))
        appendTitle(context, typeData, resolveSchemaDefinition(schema))
    }

    //========== DESCRIPTION ====================================================================

    private fun appendDescription(typeData: BaseTypeData, schema: JsonObject) {
        appendObjectDescription(typeData, schema)
        if (typeData is ObjectTypeData) {
            appendPropertyDescription(typeData, schema)
        }
    }

    private fun appendObjectDescription(typeData: BaseTypeData, schema: JsonObject) {
        typeData.annotations
            .find { it.name === SchemaDescription::class.qualifiedName }
            ?.also { setObjectField(schema, "description", JsonTextValue(it.values["description"].toString())) }
    }

    private fun appendPropertyDescription(typeData: ObjectTypeData, schema: JsonObject) {
        typeData.members.forEach { member ->
            member.annotations.find { it.name === SchemaDescription::class.qualifiedName }?.also { annotation ->
                setPropertyField(schema, member.name, "description", JsonTextValue(annotation.values["description"].toString()))
            }
        }
    }

    //========== EXAMPLE ========================================================================

    private fun appendExample(typeData: BaseTypeData, node: JsonObject) {
        appendObjectExample(typeData, node)
        if (typeData is ObjectTypeData) {
            appendPropertyExample(typeData, node)
        }
    }

    private fun appendObjectExample(typeData: BaseTypeData, node: JsonObject) {
        val examples: List<JsonNode> = typeData.annotations
            .filter { it.name === SchemaExample::class.qualifiedName }
            .map { JsonTextValue(it.values["example"].toString()) }
        if (examples.isNotEmpty()) {
            setObjectField(node, "examples", JsonArray(examples.toMutableList()))
        }
    }

    private fun appendPropertyExample(typeData: ObjectTypeData, node: JsonObject) {
        typeData.members.forEach { member ->
            val examples: List<JsonNode> = member.annotations
                .filter { it.name === SchemaExample::class.qualifiedName }
                .map { JsonTextValue(it.values["example"].toString()) }
            if (examples.isNotEmpty()) {
                setPropertyField(node, member.name, "examples", JsonArray(examples.toMutableList()))
            }
        }
    }

    //========== DEFAULT ========================================================================

    private fun appendDefault(typeData: BaseTypeData, node: JsonObject) {
        appendObjectDefault(typeData, node)
        if (typeData is ObjectTypeData) {
            appendPropertyDefault(typeData, node)
        }
    }

    private fun appendObjectDefault(typeData: BaseTypeData, node: JsonObject) {
        typeData.annotations
            .find { it.name === SchemaDefault::class.qualifiedName }
            ?.also { setObjectField(node, "default", JsonTextValue(it.values["default"].toString())) }
    }

    private fun appendPropertyDefault(typeData: ObjectTypeData, node: JsonObject) {
        typeData.members.forEach { member ->
            member.annotations.find { it.name === SchemaDefault::class.qualifiedName }?.also { annotation ->
                setPropertyField(node, member.name, "default", JsonTextValue(annotation.values["default"].toString()))
            }
        }
    }

    //========== DEPRECATED =====================================================================

    private fun appendDeprecated(typeData: BaseTypeData, node: JsonObject) {
        appendObjectDeprecated(typeData, node)
        if (typeData is ObjectTypeData) {
            appendPropertyDeprecated(typeData, node)
        }
    }

    private fun appendObjectDeprecated(typeData: BaseTypeData, node: JsonObject) {
        typeData.annotations
            .find { it.name === Deprecated::class.qualifiedName }
            ?.also { setObjectField(node, "deprecated", JsonBooleanValue(true)) }
        typeData.annotations
            .find { it.name === SchemaDeprecated::class.qualifiedName }
            ?.also { setObjectField(node, "deprecated", JsonBooleanValue(it.values["deprecated"] as Boolean)) }
    }

    private fun appendPropertyDeprecated(typeData: ObjectTypeData, node: JsonObject) {
        typeData.members.forEach { member ->
            member.annotations.find { it.name === Deprecated::class.qualifiedName }?.also {
                setPropertyField(node, member.name, "deprecated", JsonBooleanValue(true))
            }
            member.annotations.find { it.name === SchemaDeprecated::class.qualifiedName }?.also { annotation ->
                setPropertyField(node, member.name, "deprecated", JsonBooleanValue(annotation.values["deprecated"] as Boolean))
            }
        }
    }

    //========== TITLE ==========================================================================

    private fun appendTitle(context: TypeDataContext, typeData: BaseTypeData, node: JsonObject) {
        buildObjectTitle(context, typeData)?.also { title ->
            node.properties["title"] = JsonTextValue(title)
        }
        typeData.annotations
            .find { it.name === SchemaTitle::class.qualifiedName }
            ?.also { setObjectField(node, "title", JsonTextValue(it.values["title"].toString())) }
    }

    private fun buildObjectTitle(context: TypeDataContext, typeData: BaseTypeData): String? {
        return when (autoTitle) {
            AutoTitle.NONE -> null
            AutoTitle.SIMPLE_NAME -> buildMergedTitle(context, typeData) { it.simpleName }
            AutoTitle.QUALIFIED_NAME -> buildMergedTitle(context, typeData) { it.qualifiedName }
        }
    }

    private fun buildMergedTitle(
        context: TypeDataContext,
        typeData: BaseTypeData,
        titleProvider: (typeData: BaseTypeData) -> String
    ): String {
        return buildString {
            append(titleProvider(typeData))
            if (typeData.typeParameters.isNotEmpty()) {
                append("<")
                typeData.typeParameters
                    .mapNotNull { (_, typeParam) -> typeParam.type.resolve(context) }
                    .joinToString(",") { titleProvider(it) }
                    .also { append(it) }
                append(">")
            }
        }
    }

    //========== UTILS ==========================================================================

    private fun resolveSchemaDefinition(schema: JsonSchema): JsonObject {
        return if (schema.schema.properties.containsKey("\$ref") && schema.schema.properties["\$ref"] is JsonTextValue) {
            val ref = (schema.schema.properties["\$ref"] as JsonTextValue).value
            schema.getByRefOrThrow(ref)
        } else {
            schema.schema
        }
    }

    private fun setObjectField(node: JsonObject, key: String, value: JsonNode) {
        node.properties[key] = value
    }

    private fun setPropertyField(node: JsonObject, propertyName: String, key: String, value: JsonNode) {
        if (node.properties.containsKey("properties")) {
            val jsonProperties = (node.properties["properties"] as JsonObject).properties
            jsonProperties[propertyName]?.also { jsonProperty ->
                (jsonProperty as JsonObject).properties[key] = value
            }
        }
    }

}
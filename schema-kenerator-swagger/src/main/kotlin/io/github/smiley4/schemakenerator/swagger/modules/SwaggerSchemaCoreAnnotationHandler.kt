package io.github.smiley4.schemakenerator.swagger.modules

import io.github.smiley4.schemakenerator.core.annotations.SchemaDefault
import io.github.smiley4.schemakenerator.core.annotations.SchemaDeprecated
import io.github.smiley4.schemakenerator.core.annotations.SchemaDescription
import io.github.smiley4.schemakenerator.core.annotations.SchemaExample
import io.github.smiley4.schemakenerator.core.annotations.SchemaTitle
import io.github.smiley4.schemakenerator.core.data.AnnotationData
import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.ObjectTypeData
import io.github.smiley4.schemakenerator.core.data.PropertyData
import io.github.smiley4.schemakenerator.swagger.schema.SwaggerSchema
import io.swagger.v3.oas.models.media.Schema

class SwaggerSchemaCoreAnnotationHandler {

    fun appendTitle(schemas: Collection<SwaggerSchema>): List<SwaggerSchema> {
        schemas.forEach { appendTitle(it) }
        return schemas.toList()
    }

    fun appendDescription(schemas: Collection<SwaggerSchema>): List<SwaggerSchema> {
        schemas.forEach { appendDescription(it) }
        return schemas.toList()
    }

    fun appendDeprecated(schemas: Collection<SwaggerSchema>): List<SwaggerSchema> {
        schemas.forEach { appendDeprecated(it) }
        return schemas.toList()
    }

    fun appendExamples(schemas: Collection<SwaggerSchema>): List<SwaggerSchema> {
        schemas.forEach { appendExamples(it) }
        return schemas.toList()
    }

    fun appendDefaults(schemas: Collection<SwaggerSchema>): List<SwaggerSchema> {
        schemas.forEach { appendDefaults(it) }
        return schemas.toList()
    }

    //===== TITLE ==================================================

    private fun appendTitle(schema: SwaggerSchema) {
        if (schema.schema.title == null) {
            determineTitle(schema.typeData)?.also { title ->
                schema.schema.title = title
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

    private fun appendDescription(schema: SwaggerSchema) {
        if (schema.schema.description == null) {
            determineDescription(schema.typeData)?.also { description ->
                schema.schema.description = description
            }
        }
        iterateProperties(schema) { prop, data ->
            determineDescription(data)?.also { description ->
                prop.description = description
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

    private fun appendDeprecated(schema: SwaggerSchema) {
        if (schema.schema.deprecated == null) {
            determineDeprecated(schema.typeData)?.also { deprecated ->
                schema.schema.deprecated = deprecated
            }
        }
        iterateProperties(schema) { prop, data ->
            determineDeprecated(data)?.also { deprecated ->
                prop.deprecated = deprecated
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

    private fun appendExamples(schema: SwaggerSchema) {
        if (schema.schema.examples == null) {
            determineExamples(schema.typeData)?.also { examples ->
                examples.forEach {
                    @Suppress("UNCHECKED_CAST")
                    (schema.schema as Schema<Any?>).addExample(it)
                }
            }
        }
        iterateProperties(schema) { prop, data ->
            determineExamples(data)?.also { examples ->
                examples.forEach {
                    @Suppress("UNCHECKED_CAST")
                    (prop as Schema<Any?>).addExample(it)
                }
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

    private fun appendDefaults(schema: SwaggerSchema) {
        if (schema.schema.default == null) {
            determineDefaults(schema.typeData)?.also { default ->
                schema.schema.setDefault(default)
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

    private fun iterateProperties(schema: SwaggerSchema, action: (property: Schema<*>, data: PropertyData) -> Unit) {
        if (schema.typeData is ObjectTypeData && schema.schema.properties != null) {
            schema.schema.properties.forEach { (propKey, prop) ->
                schema.typeData.members.find { it.name == propKey }?.also { propertyData ->
                    action(prop, propertyData)
                }
            }
        }
    }

}
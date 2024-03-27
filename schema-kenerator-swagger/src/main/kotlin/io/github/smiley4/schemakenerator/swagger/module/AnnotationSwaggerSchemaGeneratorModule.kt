package io.github.smiley4.schemakenerator.swagger.module

import io.github.smiley4.schemakenerator.core.annotations.SchemaDefault
import io.github.smiley4.schemakenerator.core.annotations.SchemaDeprecated
import io.github.smiley4.schemakenerator.core.annotations.SchemaDescription
import io.github.smiley4.schemakenerator.core.annotations.SchemaExample
import io.github.smiley4.schemakenerator.core.annotations.SchemaTitle
import io.github.smiley4.schemakenerator.core.parser.BaseTypeData
import io.github.smiley4.schemakenerator.core.parser.ObjectTypeData
import io.github.smiley4.schemakenerator.core.parser.TypeDataContext
import io.github.smiley4.schemakenerator.core.parser.resolve
import io.github.smiley4.schemakenerator.swagger.SwaggerSchema
import io.github.smiley4.schemakenerator.swagger.SwaggerSchemaGenerator
import io.github.smiley4.schemakenerator.swagger.getByRefOrThrow
import io.swagger.v3.oas.models.examples.Example
import io.swagger.v3.oas.models.media.Schema

/**
 * Adds additional metadata to the json-schema
 * - title from [SchemaTitle] or detects the title of objects/classes automatically as specified in [AnnotationSwaggerSchemaGeneratorModule.autoTitle]
 * - example from [SchemaTitle]
 * - default from [SchemaDefault]
 * - deprecated from [SchemaDeprecated] or [Deprecated]
 */
class AnnotationSwaggerSchemaGeneratorModule(private val autoTitle: AutoTitle = AutoTitle.SIMPLE_NAME) : SwaggerSchemaGeneratorModule {

    companion object {
        enum class AutoTitle {
            NONE,
            SIMPLE_NAME,
            QUALIFIED_NAME
        }
    }

    override fun build(generator: SwaggerSchemaGenerator, context: TypeDataContext, typeData: BaseTypeData, depth: Int): SwaggerSchema? =
        null

    override fun enhance(
        generator: SwaggerSchemaGenerator,
        context: TypeDataContext,
        typeData: BaseTypeData,
        schema: SwaggerSchema,
        depth: Int
    ) {
        appendDescription(typeData, resolveSchemaDefinition(schema))
        appendExample(typeData, resolveSchemaDefinition(schema))
        appendDefault(typeData, resolveSchemaDefinition(schema))
        appendDeprecated(typeData, resolveSchemaDefinition(schema))
        appendTitle(context, typeData, resolveSchemaDefinition(schema))
    }

    //========== DESCRIPTION ====================================================================

    private fun appendDescription(typeData: BaseTypeData, schema: Schema<*>) {
        appendObjectDescription(typeData, schema)
        if (typeData is ObjectTypeData) {
            appendPropertyDescription(typeData, schema)
        }
    }

    private fun appendObjectDescription(typeData: BaseTypeData, schema: Schema<*>) {
        typeData.annotations
            .find { it.name === SchemaDescription::class.qualifiedName }
            ?.also { schema.description = it.values["description"].toString() }
    }

    private fun appendPropertyDescription(typeData: ObjectTypeData, schema: Schema<*>) {
        typeData.members.forEach { member ->
            member.annotations.find { it.name === SchemaDescription::class.qualifiedName }?.also { annotation ->
                schema.properties[member.name]?.also { it.description = annotation.values["description"].toString() }
            }
        }
    }

    //========== EXAMPLE ========================================================================

    private fun appendExample(typeData: BaseTypeData, schema: Schema<*>) {
        appendObjectExample(typeData, schema)
        if (typeData is ObjectTypeData) {
            appendPropertyExample(typeData, schema)
        }
    }

    private fun appendObjectExample(typeData: BaseTypeData, schema: Schema<*>) {
        val examples: List<Any?> = typeData.annotations
            .filter { it.name === SchemaExample::class.qualifiedName }
            .map { it.values["example"] }
        if (examples.isNotEmpty()) {
            examples.forEach { example ->
                @Suppress("UNCHECKED_CAST")
                (schema as Schema<Any?>).addExample(Example().also { it.value = example })
            }
        }
    }

    private fun appendPropertyExample(typeData: ObjectTypeData, schema: Schema<*>) {
        typeData.members.forEach { member ->
            val examples: List<Any?> = member.annotations
                .filter { it.name === SchemaExample::class.qualifiedName }
                .map { it.values["example"] }
            if (examples.isNotEmpty()) {
                examples.forEach { example ->
                    schema.properties[member.name]?.also { prop -> prop.addExample(Example().also { it.value = example }) }
                }
            }
        }
    }

    //========== DEFAULT ========================================================================

    private fun appendDefault(typeData: BaseTypeData, schema: Schema<*>) {
        appendObjectDefault(typeData, schema)
        if (typeData is ObjectTypeData) {
            appendPropertyDefault(typeData, schema)
        }
    }

    private fun appendObjectDefault(typeData: BaseTypeData, schema: Schema<*>) {
        typeData.annotations
            .find { it.name === SchemaDefault::class.qualifiedName }
            ?.also { schema.setDefault(it.values["default"]) }
    }

    private fun appendPropertyDefault(typeData: ObjectTypeData, schema: Schema<*>) {
        typeData.members.forEach { member ->
            member.annotations.find { it.name === SchemaDefault::class.qualifiedName }?.also { annotation ->
                schema.properties[member.name]?.also { prop -> prop.setDefault(annotation.values["default"]) }
            }
        }
    }

    //========== DEPRECATED =====================================================================

    private fun appendDeprecated(typeData: BaseTypeData, schema: Schema<*>) {
        appendObjectDeprecated(typeData, schema)
        if (typeData is ObjectTypeData) {
            appendPropertyDeprecated(typeData, schema)
        }
    }

    private fun appendObjectDeprecated(typeData: BaseTypeData, schema: Schema<*>) {
        typeData.annotations
            .find { it.name === Deprecated::class.qualifiedName }
            ?.also { schema.deprecated = true }
        typeData.annotations
            .find { it.name === SchemaDeprecated::class.qualifiedName }
            ?.also { schema.deprecated = it.values["deprecated"] as Boolean }
    }

    private fun appendPropertyDeprecated(typeData: ObjectTypeData, schema: Schema<*>) {
        typeData.members.forEach { member ->
            member.annotations.find { it.name === Deprecated::class.qualifiedName }?.also {
                schema.properties[member.name]?.also { prop -> prop.deprecated = true }
            }
            member.annotations.find { it.name === SchemaDeprecated::class.qualifiedName }?.also { annotation ->
                schema.properties[member.name]?.also { prop -> prop.deprecated = annotation.values["deprecated"] as Boolean }
            }
        }
    }

    //========== TITLE ==========================================================================

    private fun appendTitle(context: TypeDataContext, typeData: BaseTypeData, schema: Schema<*>) {
        buildObjectTitle(context, typeData)?.also { title ->
            schema.title = title
        }
        typeData.annotations
            .find { it.name === SchemaTitle::class.qualifiedName }
            ?.also { schema.title = it.values["title"].toString() }
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

    private fun resolveSchemaDefinition(schema: SwaggerSchema): Schema<*> {
        return if (schema.schema.`$ref` !== null) {
            schema.getByRefOrThrow(schema.schema.`$ref`)
        } else {
            schema.schema
        }
    }

}
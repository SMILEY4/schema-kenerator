package io.github.smiley4.schemakenerator.swagger

import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.MemberData
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.swagger.data.CompiledSwaggerSchema
import io.github.smiley4.schemakenerator.swagger.data.RefType
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema
import io.github.smiley4.schemakenerator.swagger.data.TitleType
import io.github.smiley4.schemakenerator.swagger.generator.DefaultSwaggerSchemaGenerationModule
import io.github.smiley4.schemakenerator.swagger.generator.SwaggerSchemaGenerationModule
import io.github.smiley4.schemakenerator.swagger.generator.SwaggerSchemaGeneratorImpl
import io.swagger.v3.oas.models.media.Schema


/**
 * Generates swagger schemas from the given type data. All types in the schema are provisionally referenced by the full type-id.
 * Result needs to be "compiled" to get the final swagger schema.
 */
fun Bundle<TypeData>.generateSwaggerSchema(configBlock: SwaggerSchemaGenerationStepConfig.() -> Unit = {}): Bundle<SwaggerSchema> {
    val config = SwaggerSchemaGenerationStepConfig().apply(configBlock)
    return SwaggerSchemaGeneratorImpl(config.buildCustomModules()).process(this)
}


/**
 * Adds an automatically determined title to schemas.
 * @param type the type of the title
 */
fun Bundle<SwaggerSchema>.withTitle(type: TitleType = TitleType.FULL): Bundle<SwaggerSchema> {
    return withTitle(
        when (type) {
            TitleType.FULL -> TitleBuilder.BUILDER_FULL
            TitleType.SIMPLE -> TitleBuilder.BUILDER_SIMPLE
            TitleType.OPENAPI_FULL -> TitleBuilder.BUILDER_OPENAPI_FULL
            TitleType.OPENAPI_SIMPLE -> TitleBuilder.BUILDER_OPENAPI_SIMPLE
        }
    )
}


/**
 * Adds an automatically determined title to schemas.
 * @param builder the function building the title for the given type
 */
fun Bundle<SwaggerSchema>.withTitle(builder: (type: TypeData, types: Map<TypeId, TypeData>) -> String): Bundle<SwaggerSchema> {
    return SwaggerSchemaTitleStep(builder).process(this)
}


/**
 * Add support for the following schema-kenerator-core annotations:
 * - [io.github.smiley4.schemakenerator.core.annotations.Optional]
 * - [io.github.smiley4.schemakenerator.core.annotations.Required]
 * - [io.github.smiley4.schemakenerator.core.annotations.Default]
 * - [io.github.smiley4.schemakenerator.core.annotations.Deprecated] and [kotlin.Deprecated]
 * - [io.github.smiley4.schemakenerator.core.annotations.Description]
 * - [io.github.smiley4.schemakenerator.core.annotations.Example]
 * - [io.github.smiley4.schemakenerator.core.annotations.Title]
 * - [io.github.smiley4.schemakenerator.core.annotations.Format]
 * - [io.github.smiley4.schemakenerator.core.annotations.Type]
 * Add this step after schema generation and before schema compilation.
 */
fun Bundle<SwaggerSchema>.handleCoreAnnotations(): Bundle<SwaggerSchema> {
    return this
        .let { SwaggerSchemaCoreAnnotationOptionalAndRequiredStep().process(this) }
        .let { SwaggerSchemaCoreAnnotationDefaultStep().process(this) }
        .let { SwaggerSchemaCoreAnnotationDeprecatedStep().process(this) }
        .let { SwaggerSchemaCoreAnnotationDescriptionStep().process(this) }
        .let { SwaggerSchemaCoreAnnotationExamplesStep().process(this) }
        .let { SwaggerSchemaCoreAnnotationTitleStep().process(this) }
        .let { SwaggerSchemaCoreAnnotationFormatStep().process(this) }
        .let { SwaggerSchemaCoreAnnotationTypeStep().process(this) }
}


/**
 * Add support for the following swagger annotations:
 * - [io.swagger.v3.oas.annotations.media.Schema]
 *      - on types
 *           - title
 *           - description
 *      - on properties
 *           - name
 *           - title
 *           - description
 *           - example
 *           - hidden
 *           - allowableValues
 *           - defaultValue
 *           - accessMode
 *           - minLength
 *           - maxLength,
 *           - format
 *           - minimum
 *           - maximum
 *           - exclusiveMaximum
 *           - exclusiveMinimum
 * - [io.swagger.v3.oas.annotations.media.ArraySchema]
 *      - minItems
 *      - maxItems
 *      - uniqueItems
 * Add this step after schema generation and before schema compilation.
 */
fun Bundle<SwaggerSchema>.handleSchemaAnnotations(): Bundle<SwaggerSchema> {
    return this
        .let { SwaggerSchemaAnnotationStep().process(this) }
        .let { SwaggerArraySchemaAnnotationStep().process(this) }
}


/**
 * Merge the attributes of a property into the referenced type.
 */
fun Bundle<SwaggerSchema>.mergePropertyAttributesIntoType(): Bundle<SwaggerSchema> {
    return SwaggerMergePropertyAttributesStep().process(this)
}


/**
 * Resolves references in generated swagger schemas by inlining them.
 * @param explicitNullTypes whether to explicitly add "null" as a type to nullable fields.
 */
fun Bundle<SwaggerSchema>.compileInlining(explicitNullTypes: Boolean = true): CompiledSwaggerSchema {
    return SwaggerSchemaCompileInlineStep(explicitNullTypes).compile(this)
}


/**
 * Resolves references in generated swagger schemas by collecting them in the components-section and referencing them.
 * @param explicitNullTypes whether to explicitly add "null" as a type to nullable fields.
 * @param pathType the type of the schema reference path
 */
fun Bundle<SwaggerSchema>.compileReferencing(
    explicitNullTypes: Boolean = true,
    pathType: RefType = RefType.OPENAPI_FULL
): CompiledSwaggerSchema {
    return compileReferencing(
        explicitNullTypes,
        when (pathType) {
            RefType.FULL -> TitleBuilder.BUILDER_FULL
            RefType.SIMPLE -> TitleBuilder.BUILDER_SIMPLE
            RefType.OPENAPI_FULL -> TitleBuilder.BUILDER_OPENAPI_FULL
            RefType.OPENAPI_SIMPLE -> TitleBuilder.BUILDER_OPENAPI_SIMPLE
        }
    )
}


/**
 * Resolves references in generated swagger schemas by collecting them in the components-section and referencing them.
 * @param explicitNullTypes whether to explicitly add "null" as a type to nullable fields.
 * @param builder builds the path to reference the type, i.e. which "name" to use
 */
fun Bundle<SwaggerSchema>.compileReferencing(
    explicitNullTypes: Boolean = true,
    builder: (type: TypeData, types: Map<TypeId, TypeData>) -> String
): CompiledSwaggerSchema {
    return SwaggerSchemaCompileReferenceStep(explicitNullTypes, builder).compile(this)
}


/**
 * Resolves references in generated swagger schemas by collecting them in the components-section and referencing them.
 * @param explicitNullTypes whether to explicitly add "null" as a type to nullable fields.
 * @param pathType the type of the schema reference path
 */
fun Bundle<SwaggerSchema>.compileReferencingRoot(
    explicitNullTypes: Boolean = true,
    pathType: RefType = RefType.OPENAPI_FULL
): CompiledSwaggerSchema {
    return compileReferencingRoot(
        explicitNullTypes,
        when (pathType) {
            RefType.FULL -> TitleBuilder.BUILDER_FULL
            RefType.SIMPLE -> TitleBuilder.BUILDER_SIMPLE
            RefType.OPENAPI_FULL -> TitleBuilder.BUILDER_OPENAPI_FULL
            RefType.OPENAPI_SIMPLE -> TitleBuilder.BUILDER_OPENAPI_SIMPLE
        }
    )
}


/**
 * Resolves references in generated swagger schemas by collecting them in the components-section and referencing them.
 * @param explicitNullTypes whether to explicitly add "null" as a type to nullable fields.
 * @param builder builds the path to reference the type, i.e. which "name" to use
 */
fun Bundle<SwaggerSchema>.compileReferencingRoot(
    explicitNullTypes: Boolean = true,
    builder: (type: TypeData, types: Map<TypeId, TypeData>) -> String
): CompiledSwaggerSchema {
    return SwaggerSchemaCompileReferenceRootStep(explicitNullTypes, builder).compile(this)
}


/**
 * Provide a function that is called for each type and swagger schema.
 * Can be used to manually manipulate the generated swagger schema.
 */
fun Bundle<SwaggerSchema>.customizeTypes(
    action: (typeData: TypeData, typeSchema: Schema<*>) -> Unit
): Bundle<SwaggerSchema> {
    return SwaggerSchemaCustomizeStep().customizeTypes(this, action)
}


/**
 * Provide a function that is called for each property. Can be used to manually manipulate the generated swagger schema.
 */
fun Bundle<SwaggerSchema>.customizeProperties(
    action: (propertyData: MemberData, propertySchema: Schema<*>) -> Unit
): Bundle<SwaggerSchema> {
    return SwaggerSchemaCustomizeStep().customizeProperties(this, action)
}


enum class OptionalHandling {
    /**
     * Handle optional parameters as "required" in the schema
     */
    REQUIRED,


    /**
     * Handle optional parameters as not required in the schema
     */
    NON_REQUIRED
}

class SwaggerSchemaGenerationStepConfig {

    /**
     * How to handle optional parameters
     *
     * Example:
     * ```
     * class MyExample(val someValue: String = "hello")
     * ```
     * - with `optionalHandling = REQUIRED` => "someValue" is required (because is not nullable)
     * - with `optionalHandling = NON_REQUIRED` => "someValue" is not required (because a default value is provided)
     */
    var optionalHandling: OptionalHandling = OptionalHandling.REQUIRED


    /**
     * whether to allow special values like "NaN", "Infinity", "-Infinity" for floating point types (i.e. float and Double)
     */
    var allowSpecialFloatingPointValues: Boolean = false


    /**
     * Whether to handle maps with complex key types as arrays instead. Valid array items are items of type key or value of the map.
     */
    var mapsWithStructuredKeysAsArrays: Boolean = false

    val customModules = mutableListOf<SwaggerSchemaGenerationModule>()

    internal fun buildCustomModules(): List<SwaggerSchemaGenerationModule> {
        val allModules = listOf(
            DefaultSwaggerSchemaGenerationModule(
                allowSpecialFloatingPointValues = allowSpecialFloatingPointValues,
                optionalAsNonRequired = optionalHandling == OptionalHandling.NON_REQUIRED,
                mapsWithStructuredKeysAsArrays = mapsWithStructuredKeysAsArrays,
            )
        ) + customModules
        return allModules.reversed()
    }


    /**
     * Add a custom schema generation module.
     */
    fun custom(module: SwaggerSchemaGenerationModule) {
        customModules.add(module)
    }

}

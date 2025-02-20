package io.github.smiley4.schemakenerator.swagger

import io.github.smiley4.schemakenerator.core.data.MemberData
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeDataGroup
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.swagger.data.CompiledSwaggerSchemaData
import io.github.smiley4.schemakenerator.swagger.data.IntermediateSwaggerSchemaData
import io.github.smiley4.schemakenerator.swagger.data.RefType
import io.github.smiley4.schemakenerator.swagger.data.TitleType
import io.github.smiley4.schemakenerator.swagger.generator.DefaultSwaggerSchemaGenerationModule
import io.github.smiley4.schemakenerator.swagger.generator.SwaggerSchemaGenerationModule
import io.github.smiley4.schemakenerator.swagger.generator.SwaggerSchemaGeneratorImpl
import io.swagger.v3.oas.models.media.Schema

object SwaggerSteps {

    /**
     * Generates swagger schemas from the given type data. All types in the schema are provisionally referenced by the full type-id.
     * Result needs to be "compiled" to get the final swagger schema.
     */
    fun TypeDataGroup.generateSwaggerSchema(configBlock: SwaggerSchemaGenerationStepConfig.() -> Unit = {}): IntermediateSwaggerSchemaData {
        val config = SwaggerSchemaGenerationStepConfig().apply(configBlock)
        return SwaggerSchemaGeneratorImpl(config.buildCustomModules()).process(this)
    }


    /**
     * Adds an automatically determined title to schemas.
     * @param type the type of the title
     */
    fun IntermediateSwaggerSchemaData.withTitle(type: TitleType = TitleType.FULL): IntermediateSwaggerSchemaData {
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
    fun IntermediateSwaggerSchemaData.withTitle(builder: (type: TypeData, types: Map<TypeId, TypeData>) -> String): IntermediateSwaggerSchemaData {
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
    fun IntermediateSwaggerSchemaData.handleCoreAnnotations(): IntermediateSwaggerSchemaData {
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
    fun IntermediateSwaggerSchemaData.handleSchemaAnnotations(): IntermediateSwaggerSchemaData {
        return this
            .let { SwaggerSchemaAnnotationStep().process(this) }
            .let { SwaggerArraySchemaAnnotationStep().process(this) }
    }


    /**
     * Merge the attributes of a property into the referenced type.
     */
    fun IntermediateSwaggerSchemaData.mergePropertyAttributesIntoType(): IntermediateSwaggerSchemaData {
        return SwaggerMergePropertyAttributesStep().process(this)
    }


    /**
     * Resolves references in generated swagger schemas by inlining them.
     * @param explicitNullTypes whether to explicitly add "null" as a type to nullable fields.
     */
    fun IntermediateSwaggerSchemaData.compileInlining(explicitNullTypes: Boolean = true): CompiledSwaggerSchemaData {
        return SwaggerSchemaCompileInlineStep(explicitNullTypes).compile(this)
    }


    /**
     * Resolves references in generated swagger schemas by collecting them in the components-section and referencing them.
     * @param explicitNullTypes whether to explicitly add "null" as a type to nullable fields.
     * @param pathType the type of the schema reference path
     */
    fun IntermediateSwaggerSchemaData.compileReferencing(
        explicitNullTypes: Boolean = true,
        pathType: RefType = RefType.OPENAPI_FULL
    ): CompiledSwaggerSchemaData {
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
    fun IntermediateSwaggerSchemaData.compileReferencing(
        explicitNullTypes: Boolean = true,
        builder: (type: TypeData, types: Map<TypeId, TypeData>) -> String
    ): CompiledSwaggerSchemaData {
        return SwaggerSchemaCompileReferenceStep(explicitNullTypes, builder).compile(this)
    }


    /**
     * Resolves references in generated swagger schemas by collecting them in the components-section and referencing them.
     * @param explicitNullTypes whether to explicitly add "null" as a type to nullable fields.
     * @param pathType the type of the schema reference path
     */
    fun IntermediateSwaggerSchemaData.compileReferencingRoot(
        explicitNullTypes: Boolean = true,
        pathType: RefType = RefType.OPENAPI_FULL
    ): CompiledSwaggerSchemaData {
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
    fun IntermediateSwaggerSchemaData.compileReferencingRoot(
        explicitNullTypes: Boolean = true,
        builder: (type: TypeData, types: Map<TypeId, TypeData>) -> String
    ): CompiledSwaggerSchemaData {
        return SwaggerSchemaCompileReferenceRootStep(explicitNullTypes, builder).compile(this)
    }


    /**
     * Provide a function that is called for each type and swagger schema.
     * Can be used to manually manipulate the generated swagger schema.
     */
    fun IntermediateSwaggerSchemaData.customizeTypes(
        action: (typeData: TypeData, typeSchema: Schema<*>) -> Unit
    ): IntermediateSwaggerSchemaData {
        return SwaggerSchemaCustomizeStep().customizeTypes(this, action)
    }


    /**
     * Provide a function that is called for each property. Can be used to manually manipulate the generated swagger schema.
     */
    fun IntermediateSwaggerSchemaData.customizeProperties(
        action: (propertyData: MemberData, propertySchema: Schema<*>) -> Unit
    ): IntermediateSwaggerSchemaData {
        return SwaggerSchemaCustomizeStep().customizeProperties(this, action)
    }


    enum class RequiredHandling {
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
         * - with `optionals = REQUIRED` => "someValue" is required (because is not nullable)
         * - with `optionals = NON_REQUIRED` => "someValue" is not required (because a default value is provided)
         */
        var optionals: RequiredHandling = RequiredHandling.REQUIRED


        /**
         * How to handle nullable parameters
         *
         * Example:
         * ```
         * class MyExample(val someValue: String?)
         * ```
         * - with `nullables = REQUIRED` => "someValue" is required (but can be either a string value or "null")
         * - with `nullables = NON_REQUIRED` => "someValue" is not required (but "null" as value is still valid)
         */
        var nullables: RequiredHandling = RequiredHandling.NON_REQUIRED


        /**
         * whether to allow special values like "NaN", "Infinity", "-Infinity" for floating point types (i.e. float and Double)
         */
        var allowSpecialFloatingPointValues: Boolean = false


        /**
         * Whether to handle maps with complex key types as arrays instead. Valid array items are items of type key or value of the map.
         */
        var mapsWithStructuredKeysAsArrays: Boolean = false

        var customModules = mutableListOf<SwaggerSchemaGenerationModule>()

        internal fun buildCustomModules(): List<SwaggerSchemaGenerationModule> {
            val allModules = listOf(
                DefaultSwaggerSchemaGenerationModule(
                    nullableAsNonRequired = nullables == RequiredHandling.NON_REQUIRED,
                    optionalAsNonRequired = optionals == RequiredHandling.NON_REQUIRED,
                    allowSpecialFloatingPointValues = allowSpecialFloatingPointValues,
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

}

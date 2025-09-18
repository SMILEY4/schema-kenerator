package io.github.smiley4.schemakenerator.jsonschema

import io.github.smiley4.schemakenerator.core.data.MemberData
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeDataGroup
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.jsonschema.data.CompiledJsonSchemaData
import io.github.smiley4.schemakenerator.jsonschema.data.IntermediateJsonSchemaData
import io.github.smiley4.schemakenerator.jsonschema.data.RefType
import io.github.smiley4.schemakenerator.jsonschema.data.TitleType
import io.github.smiley4.schemakenerator.jsonschema.generator.DefaultJsonSchemaGeneratorModule
import io.github.smiley4.schemakenerator.jsonschema.generator.JsonSchemaGeneratorImpl
import io.github.smiley4.schemakenerator.jsonschema.generator.JsonSchemaGeneratorModule
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonNode

object JsonSchemaSteps {

    /**
     * Generates json schemas from the given type data. All types in the schema are provisionally referenced by the full type-id.
     * Result needs to be "compiled" to get the final json schema.
     */
    fun TypeDataGroup.generateJsonSchema(configBlock: JsonSchemaGenerationStepConfig.() -> Unit = {}): IntermediateJsonSchemaData {
        val config = JsonSchemaGenerationStepConfig().apply(configBlock)
        return JsonSchemaGeneratorImpl(config.buildCustomModules()).process(this)
    }


    /**
     * Adds an automatically determined title to schemas.
     * @param type the type of the title
     */
    fun IntermediateJsonSchemaData.withTitle(type: TitleType = TitleType.FULL): IntermediateJsonSchemaData {
        return withTitle(
            when (type) {
                TitleType.FULL -> TitleBuilder.BUILDER_FULL
                TitleType.SIMPLE -> TitleBuilder.BUILDER_SIMPLE
                TitleType.MINIMAL -> TitleBuilder.BUILDER_MINIMAL
            }
        )
    }


    /**
     * Adds an automatically determined title to schemas.
     * @param builder the function building the title for the given type
     */
    fun IntermediateJsonSchemaData.withTitle(
        builder: (type: TypeData, types: Map<TypeId, TypeData>) -> String
    ): IntermediateJsonSchemaData {
        return JsonSchemaTitleStep(builder).process(this)
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
     * - [io.github.smiley4.schemakenerator.core.annotations.Min]
     * - [io.github.smiley4.schemakenerator.core.annotations.ExclusiveMin]
     * - [io.github.smiley4.schemakenerator.core.annotations.Max]
     * - [io.github.smiley4.schemakenerator.core.annotations.ExclusiveMax]
     * - [io.github.smiley4.schemakenerator.core.annotations.MinLength]
     * - [io.github.smiley4.schemakenerator.core.annotations.MaxLength]
     * - [io.github.smiley4.schemakenerator.core.annotations.Pattern]
     * Add this step after schema generation and before schema compilation.
     */
    fun IntermediateJsonSchemaData.handleCoreAnnotations(): IntermediateJsonSchemaData {
        return this
            .let { JsonSchemaCoreAnnotationOptionalAndRequiredStep().process(this) }
            .let { JsonSchemaCoreAnnotationDefaultStep().process(this) }
            .let { JsonSchemaCoreAnnotationDeprecatedStep().process(this) }
            .let { JsonSchemaCoreAnnotationDescriptionStep().process(this) }
            .let { JsonSchemaCoreAnnotationExamplesStep().process(this) }
            .let { JsonSchemaCoreAnnotationTitleStep().process(this) }
            .let { JsonSchemaCoreAnnotationFormatStep().process(this) }
            .let { JsonSchemaCoreAnnotationTypeStep().process(this) }
            .let { JsonSchemaCoreAnnotationMinMaxStep().process(this) }
            .let { JsonSchemaCoreAnnotationMinMaxLengthStep().process(this) }
            .let { JsonSchemaCoreAnnotationPatternStep().process(this) }
    }


    /**
     * Merge the attributes of a property into the referenced type.
     */
    fun IntermediateJsonSchemaData.mergePropertyAttributesIntoType(): IntermediateJsonSchemaData {
        return JsonMergePropertyAttributesStep().process(this)
    }


    /**
     * Resolves references in generated json schemas by inlining them.
     * @param explicitNullTypes whether to explicitly add "null" as a type to nullable fields.
     */
    fun IntermediateJsonSchemaData.compileInlining(explicitNullTypes: Boolean = true): CompiledJsonSchemaData {
        return JsonSchemaCompileInlineStep(explicitNullTypes).compile(this)
    }


    /**
     * Resolves references in generated json schemas by collecting them in the components-section and referencing them.
     * @param explicitNullTypes whether to explicitly add "null" as a type to nullable fields.
     * @param pathType the type of the schema reference path
     * @param definitionsPath the path to place referenced schemas (default: $defs)
     */
    fun IntermediateJsonSchemaData.compileReferencing(
        explicitNullTypes: Boolean = true,
        pathType: RefType = RefType.FULL,
        definitionsPath: String = "${'$'}defs"
    ): CompiledJsonSchemaData {
        return compileReferencing(
            explicitNullTypes,
            when (pathType) {
                RefType.FULL -> TitleBuilder.BUILDER_FULL
                RefType.SIMPLE -> TitleBuilder.BUILDER_SIMPLE
                RefType.MINIMAL -> TitleBuilder.BUILDER_MINIMAL
            },
            definitionsPath
        )
    }


    /**
     * Resolves references in generated json schemas by collecting them in the components-section and referencing them.
     * @param explicitNullTypes whether to explicitly add "null" as a type to nullable fields.
     * @param builder builds the path to reference the type, i.e. which "name" to use
     * @param definitionsPath the path to place referenced schemas (default: $defs)
     */
    fun IntermediateJsonSchemaData.compileReferencing(
        explicitNullTypes: Boolean = true,
        builder: (type: TypeData, types: Map<TypeId, TypeData>) -> String,
        definitionsPath: String = "${'$'}defs"
    ): CompiledJsonSchemaData {
        return JsonSchemaCompileReferenceStep(explicitNullTypes, builder, definitionsPath).compile(this)
    }


    /**
     * Resolves references in generated json schemas by collecting them in the components-section and referencing them.
     * @param explicitNullTypes whether to explicitly add "null" as a type to nullable fields.
     * @param pathType the type of the schema reference path
     * @param definitionsPath the path to place referenced schemas (default: $defs)
     */
    fun IntermediateJsonSchemaData.compileReferencingRoot(
        explicitNullTypes: Boolean = true,
        pathType: RefType = RefType.FULL,
        definitionsPath: String = "${'$'}defs"
    ): CompiledJsonSchemaData {
        return compileReferencingRoot(
            explicitNullTypes,
            when (pathType) {
                RefType.FULL -> TitleBuilder.BUILDER_FULL
                RefType.SIMPLE -> TitleBuilder.BUILDER_SIMPLE
                RefType.MINIMAL -> TitleBuilder.BUILDER_MINIMAL
            },
            definitionsPath
        )
    }


    /**
     * Resolves references in generated json schemas by collecting them in the components-section and referencing them.
     * @param explicitNullTypes whether to explicitly add "null" as a type to nullable fields.
     * @param builder builds the path to reference the type, i.e. which "name" to use
     * @param definitionsPath the path (prefix) of referenced schemas (default: $defs)
     */
    fun IntermediateJsonSchemaData.compileReferencingRoot(
        explicitNullTypes: Boolean = true,
        builder: (type: TypeData, types: Map<TypeId, TypeData>) -> String,
        definitionsPath: String = "${'$'}defs"
    ): CompiledJsonSchemaData {
        return JsonSchemaCompileReferenceRootStep(explicitNullTypes, builder, definitionsPath).compile(this)
    }


    /**
     * Merge referenced schemas into the definitions section of the root schema, creating a single json object.
     * @param definitionsPath the path to place referenced schemas (default: $defs). Must match the value specified in the compile step.
     */
    fun CompiledJsonSchemaData.merge(definitionsPath: String = "${'$'}defs"): CompiledJsonSchemaData {
        return JsonSchemaMergeStep(definitionsPath).merge(this)
    }


    /**
     * Provide a function that is called for each type and json schema.
     * Can be used to manually manipulate the generated json schema.
     */
    fun IntermediateJsonSchemaData.customizeTypes(action: (typeData: TypeData, typeSchema: JsonNode) -> Unit): IntermediateJsonSchemaData {
        return JsonSchemaCustomizeStep().customizeTypes(this, action)
    }


    /**
     * Provide a function that is called for each property. Can be used to manually manipulate the generated json schema.
     */
    fun IntermediateJsonSchemaData.customizeProperties(
        action: (memberData: MemberData, propertySchema: JsonNode) -> Unit
    ): IntermediateJsonSchemaData {
        return JsonSchemaCustomizeStep().customizeProperties(this, action)
    }


    @Deprecated("Use 'RequiredHandling' instead")
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

    fun RequiredHandling.toOptionalHandling(): OptionalHandling {
        return when (this) {
            RequiredHandling.REQUIRED -> OptionalHandling.REQUIRED
            RequiredHandling.NON_REQUIRED -> OptionalHandling.NON_REQUIRED
        }
    }

    fun OptionalHandling.toRequiredHandling(): RequiredHandling {
        return when (this) {
            OptionalHandling.REQUIRED -> RequiredHandling.REQUIRED
            OptionalHandling.NON_REQUIRED -> RequiredHandling.NON_REQUIRED
        }
    }

    class JsonSchemaGenerationStepConfig {

        /**
         * How to handle optional properties
         *
         * Example:
         * ```
         * class MyExample(val someValue: String = "hello")
         * ```
         * - with `optionalHandling = REQUIRED` => "someValue" is required (because is not nullable)
         * - with `optionalHandling = NON_REQUIRED` => "someValue" is not required (because a default value is provided)
         */
        @Deprecated("use 'optionals' instead")
        var optionalHandling: OptionalHandling
            get() = optionals.toOptionalHandling()
            set(value) {
                optionals = value.toRequiredHandling()
            }


        /**
         * How to handle optional properties
         *
         * Example:
         * ```
         * class MyExample(val someValue: String = "hello")
         * ```
         * - with `optionalHandling = REQUIRED` => "someValue" is required (because is not nullable)
         * - with `optionalHandling = NON_REQUIRED` => "someValue" is not required (because a default value is provided)
         */
        var optionals = RequiredHandling.REQUIRED


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

        val customModules = mutableListOf<JsonSchemaGeneratorModule>()

        internal fun buildCustomModules(): List<JsonSchemaGeneratorModule> {
            val allModules = listOf(
                DefaultJsonSchemaGeneratorModule(
                    nullableAsNonRequired = nullables == RequiredHandling.NON_REQUIRED,
                    optionalAsNonRequired = optionals == RequiredHandling.NON_REQUIRED
                )
            ) + customModules
            return allModules.reversed()
        }


        /**
         * Add a custom schema generation module.
         */
        fun custom(module: JsonSchemaGeneratorModule) {
            customModules.add(module)
        }

    }

}

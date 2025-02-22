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
            }
        )
    }


    /**
     * Adds an automatically determined title to schemas.
     * @param builder the function building the title for the given type
     */
    fun IntermediateJsonSchemaData.withTitle(builder: (type: TypeData, types: Map<TypeId, TypeData>) -> String): IntermediateJsonSchemaData {
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
    }


    /**
     * Resolves references in generated json schemas by inlining them.
     */
    fun IntermediateJsonSchemaData.compileInlining(): CompiledJsonSchemaData {
        return JsonSchemaCompileInlineStep().compile(this)
    }


    /**
     * Resolves references in generated json schemas by collecting them in the components-section and referencing them.
     * @param pathType the type of the schema reference path
     */
    fun IntermediateJsonSchemaData.compileReferencing(pathType: RefType = RefType.FULL): CompiledJsonSchemaData {
        return compileReferencing(
            when (pathType) {
                RefType.FULL -> TitleBuilder.BUILDER_FULL
                RefType.SIMPLE -> TitleBuilder.BUILDER_SIMPLE
            }
        )
    }


    /**
     * Resolves references in generated json schemas by collecting them in the components-section and referencing them.
     * @param builder builds the path to reference the type, i.e. which "name" to use
     */
    fun IntermediateJsonSchemaData.compileReferencing(builder: (type: TypeData, types: Map<TypeId, TypeData>) -> String): CompiledJsonSchemaData {
        return JsonSchemaCompileReferenceStep(builder).compile(this)
    }


    /**
     * Resolves references in generated json schemas by collecting them in the components-section and referencing them.
     * @param pathType the type of the schema reference path
     */
    fun IntermediateJsonSchemaData.compileReferencingRoot(pathType: RefType = RefType.FULL): CompiledJsonSchemaData {
        return compileReferencingRoot(
            when (pathType) {
                RefType.FULL -> TitleBuilder.BUILDER_FULL
                RefType.SIMPLE -> TitleBuilder.BUILDER_SIMPLE
            }
        )
    }


    /**
     * Resolves references in generated json schemas by collecting them in the components-section and referencing them.
     * @param builder builds the path to reference the type, i.e. which "name" to use
     */
    fun IntermediateJsonSchemaData.compileReferencingRoot(
        builder: (type: TypeData, types: Map<TypeId, TypeData>) -> String
    ): CompiledJsonSchemaData {
        return JsonSchemaCompileReferenceRootStep(builder).compile(this)
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
    fun IntermediateJsonSchemaData.customizeProperties(action: (memberData: MemberData, propertySchema: JsonNode) -> Unit): IntermediateJsonSchemaData {
        return JsonSchemaCustomizeStep().customizeProperties(this, action)
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
        var optionalHandling = OptionalHandling.REQUIRED

        val customModules = mutableListOf<JsonSchemaGeneratorModule>()

        internal fun buildCustomModules(): List<JsonSchemaGeneratorModule> {
            val allModules = listOf(
                DefaultJsonSchemaGeneratorModule(
                    optionalAsNonRequired = optionalHandling == OptionalHandling.NON_REQUIRED
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

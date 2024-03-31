package io.github.smiley4.schemakenerator.swagger.module

import io.github.smiley4.schemakenerator.core.parser.BaseTypeData
import io.github.smiley4.schemakenerator.core.parser.TypeDataContext
import io.github.smiley4.schemakenerator.swagger.SwaggerSchema
import io.github.smiley4.schemakenerator.swagger.SwaggerSchemaGenerator
import io.swagger.v3.oas.models.media.Schema

/**
 * Processes swagger-annotations and appends metadata to the schema
 * - title from [Schema.title]
 * - description from [Schema.description]
 * - default value from [Schema.defaultValue]
 * - examples from [Schema.example] and [Schema.examples]
 * - deprecated from [Schema.deprecated]
 */
class SwaggerAnnotationsModule : AbstractAnnotationsModule(){

    override fun build(generator: SwaggerSchemaGenerator, context: TypeDataContext, typeData: BaseTypeData, depth: Int): SwaggerSchema? =
        null

    override fun enhance(
        generator: SwaggerSchemaGenerator,
        context: TypeDataContext,
        typeData: BaseTypeData,
        schema: SwaggerSchema,
        depth: Int
    ) {
        processAnnotation(
            schema = schema,
            typeData = typeData,
            type = io.swagger.v3.oas.annotations.media.Schema::class,
            key = "title",
            processProperties = true,
            valueTransformer = { value -> value?.let { it as String } },
            action = { typeSchema, value -> typeSchema.title = value }
        )
        processAnnotation(
            schema = schema,
            typeData = typeData,
            type = io.swagger.v3.oas.annotations.media.Schema::class,
            key = "description",
            processProperties = true,
            valueTransformer = { value -> value?.let { it as String } },
            action = { typeSchema, value -> typeSchema.description = value }
        )
        processAnnotation(
            schema = schema,
            typeData = typeData,
            type = io.swagger.v3.oas.annotations.media.Schema::class,
            key = "defaultValue",
            processProperties = true,
            valueTransformer = { value -> value?.let { it as String } },
            action = { typeSchema, value -> typeSchema.setDefault(value) }
        )
        processAnnotation(
            schema = schema,
            typeData = typeData,
            type = io.swagger.v3.oas.annotations.media.Schema::class,
            key = "example",
            processProperties = true,
            valueTransformer = { value -> value?.let { it as String } },
            action = { typeSchema, value ->
                @Suppress("UNCHECKED_CAST")
                (typeSchema as Schema<Any>).addExample(value)
            }
        )
        processAnnotation(
            schema = schema,
            typeData = typeData,
            type = io.swagger.v3.oas.annotations.media.Schema::class,
            key = "examples",
            processProperties = true,
            valueTransformer = { value ->
                value?.let {
                    @Suppress("UNCHECKED_CAST")
                    (it as Array<String>).toList()
                }
            },
            action = { typeSchema, value ->
                value?.forEach {
                    @Suppress("UNCHECKED_CAST")
                    (typeSchema as Schema<Any>).addExample(it)
                }
            }
        )
        processAnnotation(
            schema = schema,
            typeData = typeData,
            type = io.swagger.v3.oas.annotations.media.Schema::class,
            key = "deprecated",
            processProperties = true,
            valueTransformer = { value -> value?.let { it as Boolean } },
            action = { typeSchema, value -> typeSchema.deprecated = value }
        )
    }

}
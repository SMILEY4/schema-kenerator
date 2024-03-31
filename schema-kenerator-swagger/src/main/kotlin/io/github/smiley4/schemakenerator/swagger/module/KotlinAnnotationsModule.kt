package io.github.smiley4.schemakenerator.swagger.module

import io.github.smiley4.schemakenerator.core.parser.BaseTypeData
import io.github.smiley4.schemakenerator.core.parser.TypeDataContext
import io.github.smiley4.schemakenerator.swagger.SwaggerSchema
import io.github.smiley4.schemakenerator.swagger.SwaggerSchemaGenerator

/**
 * Processes kotlin-annotation and appends metadata to the schema
 * - deprecated from [Deprecated]
 */
class KotlinAnnotationsModule : AbstractAnnotationsModule() {

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
            type = Deprecated::class,
            key = null,
            processProperties = true,
            valueTransformer = {},
            action = { typeSchema, _ -> typeSchema.deprecated = true }
        )
    }

}
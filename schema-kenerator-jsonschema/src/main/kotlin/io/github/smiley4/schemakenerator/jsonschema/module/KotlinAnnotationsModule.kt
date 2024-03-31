package io.github.smiley4.schemakenerator.jsonschema.module

import io.github.smiley4.schemakenerator.core.parser.BaseTypeData
import io.github.smiley4.schemakenerator.core.parser.TypeDataContext
import io.github.smiley4.schemakenerator.jsonschema.JsonSchema
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaGenerator
import io.github.smiley4.schemakenerator.jsonschema.json.JsonBooleanValue

/**
 * Processes kotlin-annotation and appends metadata to the schema
 * - deprecated from [Deprecated]
 */
class KotlinAnnotationsModule : AbstractAnnotationsModule() {

    override fun build(generator: JsonSchemaGenerator, context: TypeDataContext, typeData: BaseTypeData, depth: Int): JsonSchema? =
        null

    override fun enhance(
        generator: JsonSchemaGenerator,
        context: TypeDataContext,
        typeData: BaseTypeData,
        schema: JsonSchema,
        depth: Int
    ) {
        processAnnotation(
            schema = schema,
            typeData = typeData,
            type = Deprecated::class,
            key = null,
            processProperties = true,
            valueTransformer = {},
            action = { typeSchema, _ -> typeSchema.properties["deprecated"] = JsonBooleanValue(true) }
        )
    }

}
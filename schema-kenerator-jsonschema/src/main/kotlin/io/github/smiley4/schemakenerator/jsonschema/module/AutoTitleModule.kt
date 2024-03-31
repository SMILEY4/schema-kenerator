package io.github.smiley4.schemakenerator.jsonschema.module

import io.github.smiley4.schemakenerator.core.parser.BaseTypeData
import io.github.smiley4.schemakenerator.core.parser.TypeDataContext
import io.github.smiley4.schemakenerator.core.parser.resolve
import io.github.smiley4.schemakenerator.jsonschema.JsonSchema
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaGenerator
import io.github.smiley4.schemakenerator.jsonschema.getDefinition
import io.github.smiley4.schemakenerator.jsonschema.json.JsonTextValue

/**
 * Automatically detects and appends a title to the generated schema
 */
class AutoTitleModule(private val type: AutoTitleType = AutoTitleType.SIMPLE_NAME) : JsonSchemaGeneratorModule {

    companion object {
        enum class AutoTitleType {
            NONE,
            SIMPLE_NAME,
            QUALIFIED_NAME
        }
    }

    override fun build(generator: JsonSchemaGenerator, context: TypeDataContext, typeData: BaseTypeData, depth: Int): JsonSchema? =
        null

    override fun enhance(
        generator: JsonSchemaGenerator,
        context: TypeDataContext,
        typeData: BaseTypeData,
        schema: JsonSchema,
        depth: Int
    ) {
        val definition = schema.getDefinition()
        buildObjectTitle(context, typeData)?.also { title ->
            definition.properties["title"] = JsonTextValue(title)
        }
    }

    private fun buildObjectTitle(context: TypeDataContext, typeData: BaseTypeData): String? {
        return when (type) {
            AutoTitleType.NONE -> null
            AutoTitleType.SIMPLE_NAME -> buildMergedTitle(context, typeData) { it.simpleName }
            AutoTitleType.QUALIFIED_NAME -> buildMergedTitle(context, typeData) { it.qualifiedName }
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

}
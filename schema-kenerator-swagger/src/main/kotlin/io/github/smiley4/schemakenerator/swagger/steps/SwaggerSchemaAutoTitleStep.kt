package io.github.smiley4.schemakenerator.swagger.steps

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema
import io.github.smiley4.schemakenerator.swagger.data.TitleType

/**
 * Adds an automatically determined title to schemas.
 * @param type the type of the title
 */
class SwaggerSchemaAutoTitleStep(val type: TitleType = TitleType.FULL) {

    fun process(bundle: Bundle<SwaggerSchema>): Bundle<SwaggerSchema> {
        val typeDataMap = bundle.buildTypeDataMap()
        return bundle.also { schema ->
            process(schema.data, typeDataMap)
            schema.supporting.forEach { process(it, typeDataMap) }
        }
    }

    private fun process(schema: SwaggerSchema, typeDataMap: Map<TypeId, BaseTypeData>) {
        if (schema.swagger.title == null) {
            schema.swagger.title = determineTitle(schema.typeData, typeDataMap)
        }
    }

    private fun determineTitle(typeData: BaseTypeData, typeDataMap: Map<TypeId, BaseTypeData>): String {
        return when (type) {
            TitleType.FULL -> typeData.qualifiedName
            TitleType.SIMPLE -> typeData.simpleName
        }.let {
            if (typeData.typeParameters.isNotEmpty()) {
                val paramString = typeData.typeParameters
                    .map { (_, param) -> determineTitle(typeDataMap[param.type]!!, typeDataMap) }
                    .joinToString(",")
                "$it<$paramString>"
            } else {
                it
            }
        }.let {
            it + (typeData.id.additionalId?.let { a -> "#$a" } ?: "")
        }
    }

}

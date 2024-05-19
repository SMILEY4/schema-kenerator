package io.github.smiley4.schemakenerator.core.steps

import io.github.smiley4.schemakenerator.core.annotations.SchemaName
import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.Bundle

/**
 * Changes the [BaseTypeData.qualifiedName] and [BaseTypeData.simpleName] to the name specified with a [SchemaName]
 */
class RenameTypesStep {

    fun process(bundle: Bundle<BaseTypeData>): Bundle<BaseTypeData> {
        return bundle.also { schema ->
            process(schema.data)
            schema.supporting.forEach { process(it) }
        }
    }

    private fun process(data: BaseTypeData) {
        data.annotations
            .find { it.name == SchemaName::class.qualifiedName }
            ?.let {
                val name = it.values["name"] as String
                val qualifiedName = it.values["qualifiedName"] as String
                name to (qualifiedName.ifEmpty { null })
            }
            ?.also { (name, qualifiedName) ->
                data.simpleName = name
                data.qualifiedName = qualifiedName ?: name
            }
    }

}
package io.github.smiley4.schemakenerator.jsonschema.steps

import old.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.Bundle
import old.TypeId
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchema

abstract class AbstractJsonSchemaStep {

    fun process(bundle: Bundle<JsonSchema>): Bundle<JsonSchema> {
        val typeDataMap = bundle.buildTypeDataMap()
        return bundle.also { schema ->
            process(schema.data, typeDataMap)
            schema.supporting.forEach { process(it, typeDataMap) }
        }
    }

    protected abstract fun process(schema: JsonSchema, typeDataMap: Map<TypeId, BaseTypeData>)

}

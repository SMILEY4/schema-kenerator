package io.github.smiley4.schemakenerator.jsonschema

import io.github.smiley4.schemakenerator.core.parser.TypeDataContext
import io.github.smiley4.schemakenerator.core.parser.TypeRef
import io.github.smiley4.schemakenerator.core.parser.idStr
import io.github.smiley4.schemakenerator.core.parser.resolve
import io.github.smiley4.schemakenerator.core.schema.SchemaGenerator
import io.github.smiley4.schemakenerator.jsonschema.json.JsonNode
import io.github.smiley4.schemakenerator.jsonschema.json.JsonObject
import io.github.smiley4.schemakenerator.jsonschema.module.StandardJsonSchemaGeneratorModule
import io.github.smiley4.schemakenerator.jsonschema.module.JsonSchemaGeneratorModule
import io.github.smiley4.schemakenerator.jsonschema.schema.JsonSchema

class JsonSchemaGenerator : SchemaGenerator<JsonNode> {

    private val schema = JsonSchema()

    private val modules = mutableListOf<JsonSchemaGeneratorModule>()

    fun withModule(module: JsonSchemaGeneratorModule): JsonSchemaGenerator {
        this.modules.add(module)
        return this
    }

    override fun generate(typeRef: TypeRef, context: TypeDataContext): JsonObject {
        val type = typeRef.resolve(context) ?: throw IllegalArgumentException("TypeRef could not be resolved: ${typeRef.idStr()}")

        var schema = schema.nullSchema()
        for (module in modules.reversed()) {
            val result = module.build(this, context, type)
            if (result != null) {
                schema = result
                break
            }
        }

        for (module in modules) {
            module.enhance(this, context, type, schema)
        }

        return schema
    }

}

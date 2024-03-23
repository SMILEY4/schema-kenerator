package io.github.smiley4.schemakenerator.jsonschema

import io.github.smiley4.schemakenerator.core.parser.TypeDataContext
import io.github.smiley4.schemakenerator.core.parser.TypeRef
import io.github.smiley4.schemakenerator.core.parser.idStr
import io.github.smiley4.schemakenerator.core.parser.resolve
import io.github.smiley4.schemakenerator.core.schema.SchemaGenerator
import io.github.smiley4.schemakenerator.jsonschema.json.obj
import io.github.smiley4.schemakenerator.jsonschema.module.JsonSchemaGeneratorModule
import io.github.smiley4.schemakenerator.jsonschema.schema.JsonSchemaUtils

class JsonSchemaGenerator : SchemaGenerator<JsonSchema> {

    private val modules = mutableListOf<JsonSchemaGeneratorModule>()

    fun withModule(module: JsonSchemaGeneratorModule): JsonSchemaGenerator {
        this.modules.add(module)
        return this
    }

    override fun generate(typeRef: TypeRef, context: TypeDataContext, depth: Int): JsonSchema {
        val type = typeRef.resolve(context) ?: throw IllegalArgumentException("TypeRef could not be resolved: ${typeRef.idStr()}")

        var schema = JsonSchema(obj { }, mutableMapOf())
        for (module in modules.reversed()) {
            val result = module.build(this, context, type, depth)
            if (result != null) {
                schema = result
                break
            }
        }

        for (module in modules) {
            module.enhance(this, context, type, schema, depth)
        }

        return schema
    }

}

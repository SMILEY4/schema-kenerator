package io.github.smiley4.schemakenerator.swagger

import io.github.smiley4.schemakenerator.core.parser.TypeParserContext
import io.github.smiley4.schemakenerator.core.parser.TypeRef
import io.github.smiley4.schemakenerator.core.parser.idStr
import io.github.smiley4.schemakenerator.core.parser.resolve
import io.github.smiley4.schemakenerator.core.schema.SchemaGenerator
import io.github.smiley4.schemakenerator.swagger.module.BaseSwaggerSchemaGeneratorModule
import io.github.smiley4.schemakenerator.swagger.module.SwaggerSchemaGeneratorModule
import io.github.smiley4.schemakenerator.swagger.swagger.SwaggerSchema
import io.swagger.v3.oas.models.media.Schema

class SwaggerSchemaGenerator : SchemaGenerator<Schema<*>> {

    private val schema = SwaggerSchema()

    private val modules = mutableListOf<SwaggerSchemaGeneratorModule>().also {
        it.add(BaseSwaggerSchemaGeneratorModule())
    }

    fun withModule(module: SwaggerSchemaGeneratorModule): SwaggerSchemaGenerator {
        this.modules.add(module)
        return this
    }

    override fun generate(typeRef: TypeRef, context: TypeParserContext): Schema<*> {
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
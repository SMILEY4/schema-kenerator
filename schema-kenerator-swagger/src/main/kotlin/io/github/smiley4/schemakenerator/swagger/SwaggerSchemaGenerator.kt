package io.github.smiley4.schemakenerator.swagger

import io.github.smiley4.schemakenerator.core.parser.TypeDataContext
import io.github.smiley4.schemakenerator.core.parser.TypeRef
import io.github.smiley4.schemakenerator.core.parser.idStr
import io.github.smiley4.schemakenerator.core.parser.resolve
import io.github.smiley4.schemakenerator.core.schema.SchemaGenerator
import io.github.smiley4.schemakenerator.swagger.module.SwaggerSchemaGeneratorModule
import io.swagger.v3.oas.models.media.Schema

class SwaggerSchemaGenerator : SchemaGenerator<SwaggerSchema> {

    private val modules = mutableListOf<SwaggerSchemaGeneratorModule>()

    fun withModule(module: SwaggerSchemaGeneratorModule): SwaggerSchemaGenerator {
        this.modules.add(module)
        return this
    }

    fun withModules(vararg modules: SwaggerSchemaGeneratorModule): SwaggerSchemaGenerator {
        this.modules.addAll(modules)
        return this
    }

    fun withModules(modules: List<SwaggerSchemaGeneratorModule>): SwaggerSchemaGenerator {
        this.modules.addAll(modules)
        return this
    }

    override fun generate(typeRef: TypeRef, context: TypeDataContext, depth: Int): SwaggerSchema {
        val type = typeRef.resolve(context) ?: throw IllegalArgumentException("TypeRef could not be resolved: ${typeRef.idStr()}")

        var schema = SwaggerSchema(Schema<Any>(), mutableMapOf())
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
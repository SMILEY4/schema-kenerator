package io.github.smiley4.schemakenerator.swagger.generator

import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema

interface SwaggerSchemaGenerator {
    fun generate(typeData: TypeData, typeDataList: List<TypeData>): SwaggerSchema
}

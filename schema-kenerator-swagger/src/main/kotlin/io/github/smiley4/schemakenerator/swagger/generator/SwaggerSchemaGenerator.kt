package io.github.smiley4.schemakenerator.swagger.generator

import io.github.smiley4.schemakenerator.core.data.TypeData
import io.swagger.v3.oas.models.media.Schema

interface SwaggerSchemaGenerator {
    fun generate(typeData: TypeData, typeDataList: List<TypeData>): Schema<*>
}

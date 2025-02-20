package io.github.smiley4.schemakenerator.swagger.data

import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.swagger.v3.oas.models.media.Schema

/**
 * Contains all data about generated intermediate swagger schemas for a given (root) [TypeData].
 */
class IntermediateSwaggerSchemaData(
    val rootId: TypeId,
    val data: Map<TypeId, SwaggerSchemaData>
) {

    /**
     * the root [SwaggerSchemaData]
     */
    val root: SwaggerSchemaData
        get() = data[rootId]
            ?: throw IllegalStateException("${IntermediateSwaggerSchemaData::class.qualifiedName} does not contain root with id $rootId")


    /**
     * the root [TypeData]
     */
    val rootTypeData: TypeData
        get() = data[rootId]?.typeData
            ?: throw IllegalStateException("${IntermediateSwaggerSchemaData::class.qualifiedName} does not contain root with id $rootId")


    /**
     * the root [Schema]
     */
    val rootSchema: Schema<*>
        get() = data[rootId]?.swagger
            ?: throw IllegalStateException("${IntermediateSwaggerSchemaData::class.qualifiedName} does not contain root with id $rootId")


    /**
     * All contained schemas together with type data
     */
    val entries: List<SwaggerSchemaData>
        get() = data.values.toList()


    /**
     * All contained swagger schemas
     */
    val schemas: List<Schema<*>>
        get() = data.values.map { it.swagger }


    /**
     * all contained type data
     */
    val typeData: List<TypeData>
        get() = data.values.map { it.typeData }


    /**
     * all contained type data
     */
    val typeDataById: Map<TypeId, TypeData>
        get() = data.values.map { it.typeData }.associateBy { it.id }


    /**
     * @return the contained [SwaggerSchemaData] with the given [TypeId] or null.
     */
    operator fun get(typeId: TypeId): SwaggerSchemaData? = data[typeId]

}
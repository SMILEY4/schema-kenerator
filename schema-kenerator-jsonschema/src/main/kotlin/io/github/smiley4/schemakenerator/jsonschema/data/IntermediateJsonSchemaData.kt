package io.github.smiley4.schemakenerator.jsonschema.data

import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonNode

/**
 * Contains all data about generated intermediate json schemas for a given (root) [TypeData].
 */
class IntermediateJsonSchemaData(
    val rootId: TypeId,
    val data: Map<TypeId, JsonSchemaData>
) {

    /**
     * the root [JsonSchemaData]
     */
    val root: JsonSchemaData
        get() = data[rootId]
            ?: throw IllegalStateException("${IntermediateJsonSchemaData::class.qualifiedName} does not contain root with id $rootId")


    /**
     * the root [TypeData]
     */
    val rootTypeData: TypeData
        get() = data[rootId]?.typeData
            ?: throw IllegalStateException("${IntermediateJsonSchemaData::class.qualifiedName} does not contain root with id $rootId")


    /**
     * the root [JsonNode]
     */
    val rootSchema: JsonNode
        get() = data[rootId]?.json
            ?: throw IllegalStateException("${IntermediateJsonSchemaData::class.qualifiedName} does not contain root with id $rootId")


    /**
     * All contained schemas together with type data
     */
    val entries: List<JsonSchemaData>
        get() = data.values.toList()


    /**
     * All contained json schemas
     */
    val schemas: List<JsonNode>
        get() = data.values.map { it.json }


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
     * @return the contained [JsonSchemaData] with the given [TypeId] or null.
     */
    operator fun get(typeId: TypeId): JsonSchemaData? = data[typeId]

}

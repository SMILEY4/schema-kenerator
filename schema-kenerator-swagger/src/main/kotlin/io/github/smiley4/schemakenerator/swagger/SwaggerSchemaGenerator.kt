package io.github.smiley4.schemakenerator.swagger

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.CollectionTypeData
import io.github.smiley4.schemakenerator.core.data.EnumTypeData
import io.github.smiley4.schemakenerator.core.data.MapTypeData
import io.github.smiley4.schemakenerator.core.data.ObjectTypeData
import io.github.smiley4.schemakenerator.core.data.PrimitiveTypeData
import io.github.smiley4.schemakenerator.core.data.PropertyData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.core.data.WildcardTypeData
import io.github.smiley4.schemakenerator.swagger.schema.SwaggerSchema
import io.github.smiley4.schemakenerator.swagger.schema.SwaggerSchemaUtils
import io.swagger.v3.oas.models.media.Schema
import java.math.BigDecimal

/**
 * Generates swagger-schemas from [BaseTypeData]. All types in the schema are referenced by type-id
 */
class SwaggerSchemaGenerator {

    private val schema = SwaggerSchemaUtils()

    fun generate(types: Collection<BaseTypeData>): List<SwaggerSchema> {
        return types.map { generate(it, types) }
    }

    private fun generate(typeData: BaseTypeData, typeDataList: Collection<BaseTypeData>): SwaggerSchema {
        if (typeData is ObjectTypeData && typeData.subtypes.isNotEmpty()) {
            return buildWithSubtypes(typeData)
        }
        return when (typeData) {
            is PrimitiveTypeData -> buildPrimitiveSchema(typeData)
            is EnumTypeData -> buildEnumSchema(typeData)
            is CollectionTypeData -> buildCollectionSchema(typeData)
            is MapTypeData -> buildMapSchema(typeData)
            is ObjectTypeData -> buildObjectSchema(typeData, typeDataList)
            is WildcardTypeData -> buildAnySchema()
            else -> SwaggerSchema(
                schema = schema.nullSchema(),
                typeId = TypeId.unknown()
            )
        }
    }

    private fun buildAnySchema(): SwaggerSchema {
        return SwaggerSchema(schema.anyObjectSchema(), TypeId.wildcard())
    }

    private fun buildPrimitiveSchema(typeData: PrimitiveTypeData): SwaggerSchema {
        return when (typeData.qualifiedName) {
            Number::class.qualifiedName -> schema.numberSchema(false)
            Byte::class.qualifiedName -> schema.numericSchema(
                integer = true,
                min = BigDecimal.valueOf(Byte.MIN_VALUE.toLong()),
                max = BigDecimal.valueOf(Byte.MAX_VALUE.toLong()),
            )
            Short::class.qualifiedName -> schema.numericSchema(
                integer = true,
                min = BigDecimal.valueOf(Short.MIN_VALUE.toLong()),
                max = BigDecimal.valueOf(Short.MAX_VALUE.toLong()),
            )
            Int::class.qualifiedName -> schema.int32Schema()
            Long::class.qualifiedName -> schema.int64Schema()
            UByte::class.qualifiedName -> schema.numericSchema(
                integer = true,
                min = BigDecimal.valueOf(UByte.MIN_VALUE.toLong()),
                max = BigDecimal.valueOf(UByte.MAX_VALUE.toLong()),
            )
            UShort::class.qualifiedName -> schema.numericSchema(
                integer = true,
                min = BigDecimal.valueOf(UShort.MIN_VALUE.toLong()),
                max = BigDecimal.valueOf(UShort.MAX_VALUE.toLong()),
            )
            UInt::class.qualifiedName -> schema.numericSchema(
                integer = true,
                min = BigDecimal.valueOf(UInt.MIN_VALUE.toLong()),
                max = BigDecimal.valueOf(UInt.MAX_VALUE.toLong()),
            )
            ULong::class.qualifiedName -> schema.numberSchema(true)
            Float::class.qualifiedName -> schema.floatSchema()
            Double::class.qualifiedName -> schema.doubleSchema()
            Boolean::class.qualifiedName -> schema.booleanSchema()
            Char::class.qualifiedName -> schema.stringSchema(
                min = 1,
                max = 1,
            )
            String::class.qualifiedName -> schema.stringSchema(
                min = null,
                max = null,
            )
            Any::class.qualifiedName -> schema.anyObjectSchema()
            Unit::class.qualifiedName -> schema.nullSchema()
            else -> schema.nullSchema()
        }.let {
            SwaggerSchema(
                schema = it,
                typeId = typeData.id
            )
        }
    }

    private fun buildEnumSchema(typeData: EnumTypeData): SwaggerSchema {
        return SwaggerSchema(
            schema = schema.enumSchema(typeData.enumConstants),
            typeId = typeData.id
        )
    }

    private fun buildCollectionSchema(typeData: CollectionTypeData): SwaggerSchema {
        return SwaggerSchema(
            schema = schema.arraySchema(schema.referenceSchema(typeData.itemType.type)),
            typeId = typeData.id
        )
    }

    private fun buildMapSchema(typeData: MapTypeData): SwaggerSchema {
        return SwaggerSchema(
            schema = schema.mapObjectSchema(schema.referenceSchema(typeData.valueType.type)),
            typeId = typeData.id
        )
    }

    private fun buildWithSubtypes(typeData: ObjectTypeData): SwaggerSchema {
        return SwaggerSchema(
            schema = schema.subtypesSchema(
                typeData.subtypes.map { schema.referenceSchema(it.full()) }
            ),
            typeId = typeData.id
        )
    }
    
    private fun buildObjectSchema(typeData: ObjectTypeData, typeDataList: Collection<BaseTypeData>): SwaggerSchema {
        
        val requiredProperties = mutableSetOf<String>()
        val propertySchemas = mutableMapOf<String, Schema<*>>()

        collectMembers(typeData, typeDataList).forEach { member ->
            propertySchemas[member.name] = schema.referenceSchema(member.type)
            if (!member.nullable) {
                requiredProperties.add(member.name)
            }
        }

        return SwaggerSchema(
            schema = schema.objectSchema(propertySchemas, requiredProperties),
            typeId = typeData.id
        )
    }

    private fun collectMembers(typeData: ObjectTypeData, typeDataList: Collection<BaseTypeData>): List<PropertyData> {
        return buildList {
            addAll(typeData.members)
            typeData.supertypes.forEach { supertypeId ->
                val supertype = typeDataList.find { it.id == supertypeId }
                if (supertype is ObjectTypeData) {
                    addAll(collectMembers(supertype, typeDataList))
                }
            }
        }
    }
    
}

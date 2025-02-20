package io.github.smiley4.schemakenerator.swagger.generator

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.github.smiley4.schemakenerator.core.AbstractAddDiscriminatorStep
import io.github.smiley4.schemakenerator.core.data.MemberData
import io.github.smiley4.schemakenerator.core.data.MemberKind
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.core.data.find
import io.github.smiley4.schemakenerator.swagger.SwaggerSchemaUtils
import io.swagger.v3.oas.models.media.Schema
import java.math.BigDecimal

class DefaultSwaggerSchemaGenerationModule(
    private val optionalAsNonRequired: Boolean,
    private val nullableAsNonRequired: Boolean,
    private val allowSpecialFloatingPointValues: Boolean,
    private val mapsWithStructuredKeysAsArrays: Boolean,
) : SwaggerSchemaGenerationModule {

    private val schema = SwaggerSchemaUtils()

    override fun applies(typeData: TypeData) = true

    override fun generate(context: SwaggerSchemaGenerationModule.Context): Schema<*> {
        if (context.typeData.subtypes.isNotEmpty()) {
            return buildWithSubtypes(context.typeData, context.knownTypeData)
        }
        return when {
            context.typeData.enumData != null -> buildEnumSchema(context.typeData)
            context.typeData.collectionData != null -> buildCollectionSchema(context.typeData)
            context.typeData.mapData != null -> buildMapSchema(context.typeData, context.knownTypeData)
            context.typeData.id == TypeId.WILDCARD -> buildAnySchema()
            context.typeData.members.isNotEmpty() -> buildObjectSchema(context)
            else -> buildPrimitiveSchema(context.typeData) ?: buildObjectSchema(context)
        }
    }

    private fun buildAnySchema(): Schema<*> {
        return schema.anyObjectSchema()
    }

    private fun buildPrimitiveSchema(typeData: TypeData): Schema<*>? {
        return when (typeData.identifyingName.full) {
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
            Float::class.qualifiedName -> schema.floatSchema(allowSpecialFloatingPointValues)
            Double::class.qualifiedName -> schema.doubleSchema(allowSpecialFloatingPointValues)
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
            else -> null
        }
    }

    private fun buildEnumSchema(typeData: TypeData): Schema<*> {
        return schema.enumSchema(
                values = typeData.enumData?.constants ?: emptyList()
            )
    }

    private fun buildCollectionSchema(typeData: TypeData): Schema<*> {
        return schema.arraySchema(
                items = schema.referenceSchema(typeData.collectionData!!.itemType.type),
                uniqueItems = typeData.collectionData!!.unique
        )
    }

    private fun buildMapSchema(typeData: TypeData, knownTypeData: List<TypeData>): Schema<*> {
        if(mapsWithStructuredKeysAsArrays) {
            val keyType = knownTypeData.find(typeData.mapData!!.keyType.type)!!
            if(keyType.members.isNotEmpty()) {
                return schema.arraySchema(
                        items = Schema<Any>().also { itemSchema ->
                            itemSchema.anyOf = listOf(
                                schema.referenceSchema(typeData.mapData!!.keyType.type),
                                schema.referenceSchema(typeData.mapData!!.valueType.type),
                            )
                        },
                        uniqueItems = false
                )
            }
        }
        return schema.mapObjectSchema(
                valueSchema = schema.referenceSchema(typeData.mapData!!.valueType.type)
        )
    }

    private fun buildWithSubtypes(typeData: TypeData, typeDataList: Collection<TypeData>): Schema<*> {
        return schema.subtypesSchema(
                subtypes = typeData.subtypes.map { schema.referenceSchema(it) },
                discriminator = getDiscriminatorName(typeData),
                discriminatorMapping = discriminatorMapping(typeData, typeDataList)
        )
    }

    private fun discriminatorMapping(typeData: TypeData, typeDataList: Collection<TypeData>): Map<TypeId, String> {
        return buildMap {
            typeData.subtypes.forEach { subtypeId ->
                val subtype = typeDataList.find { it.id == subtypeId }!!
                // hint: default = qualified name (or from @SerialName) -> already covers kotlinx behaviour
                var name = subtype.descriptiveName.full
                val jsonTypeInfo = typeData.annotations.find { it.name == JsonTypeInfo::class.qualifiedName }
                val jsonSubTypes = typeData.annotations.find { it.name == JsonSubTypes::class.qualifiedName }
                if (jsonTypeInfo != null) {
                    val mode = jsonTypeInfo.values["use"].toString()
                    when (mode) {
                        "CLASS" -> name = subtype.descriptiveName.full
                        "MINIMAL_CLASS" -> Unit
                        "NAME" -> jsonSubTypes?.also {
                            @Suppress("UNCHECKED_CAST")
                            val subtypeInfo = it.values["value"] as Array<JsonSubTypes.Type>
                            name = subtypeInfo.find { i -> i.value.qualifiedName == subtype.descriptiveName.full }?.name ?: ""
                        }
                        "SIMPLE_NAME" -> name = subtype.descriptiveName.short
                        "NONE" -> Unit
                        "DEDUCTION" -> Unit
                        "CUSTOM" -> Unit
                    }
                }
                this[subtypeId] = name
            }
        }
    }

    private fun getDiscriminatorName(typeData: TypeData): String? {
        val property = typeData.members.find { member ->
            member.annotations.any { it.name == AbstractAddDiscriminatorStep.MARKER_ANNOTATION_NAME }
        }
        return property?.name
    }


    private fun buildObjectSchema(context: SwaggerSchemaGenerationModule.Context): Schema<*> {
        if (context.typeData.isInlineValue) {
            return buildInlineObjectSchema(context)
        }

        val requiredProperties = mutableSetOf<String>()
        val propertySchemas = mutableMapOf<String, Schema<*>>()

        collectMembers(context.typeData, context.knownTypeData).forEach { member ->
            propertySchemas[member.name] = schema.referenceSchema(member.type)
            propertySchemas[member.name]?.also {
                if (member.nullable) {
                    it.nullable = member.nullable
                }
            }
            val nullable = member.nullable && nullableAsNonRequired
            val optional = member.optional && optionalAsNonRequired
            if (!nullable && !optional) {
                requiredProperties.add(member.name)
            }
        }

        return schema.objectSchema(propertySchemas, requiredProperties)
    }

    private fun buildInlineObjectSchema(context: SwaggerSchemaGenerationModule.Context): Schema<*> {
        val inlineType = context.typeData.members.first { it.kind == MemberKind.PROPERTY }
        val inlineTypeData = context.knownTypeData.find { it.id == inlineType.type }
            ?: throw NoSuchElementException("Could not find type-data for inline type ${inlineType.type}")
        return context.generate(inlineTypeData)
    }

    private fun collectMembers(typeData: TypeData, typeDataList: Collection<TypeData>): List<MemberData> {
        return buildList {
            typeData.supertypes.forEach { supertypeId ->
                val supertype = typeDataList.find { it.id == supertypeId }
                if (supertype != null) {
                    addAll(collectMembers(supertype, typeDataList))
                }
            }
            addAll(typeData.members)
        }
    }

}

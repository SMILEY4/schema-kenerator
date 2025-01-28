package io.github.smiley4.schemakenerator.jsonschema.generator

import io.github.smiley4.schemakenerator.core.typedata.MemberData
import io.github.smiley4.schemakenerator.core.typedata.MemberKind
import io.github.smiley4.schemakenerator.core.typedata.TypeData
import io.github.smiley4.schemakenerator.core.typedata.TypeId
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchema
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonNode
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaUtils

class DefaultJsonSchemaGeneratorModule(
    private val optionalAsNonRequired: Boolean = false
) : JsonSchemaGeneratorModule {

    private val schemaUtils = JsonSchemaUtils()


    override fun applies(typeData: TypeData) = true

    override fun generate(context: JsonSchemaGeneratorModule.Context): JsonSchema {
        if (context.typeData.subtypes.isNotEmpty()) {
            return buildWithSubtypes(context.typeData)
        }
        return when {
            context.typeData.enumData != null -> buildEnumSchema(context.typeData)
            context.typeData.collectionData != null -> buildCollectionSchema(context.typeData)
            context.typeData.mapData != null -> buildMapSchema(context.typeData)
            context.typeData.id == TypeId.WILDCARD -> buildAnySchema()
            context.typeData.members.isNotEmpty() -> buildObjectSchema(context)
            else -> buildPrimitiveSchema(context.typeData) ?: buildObjectSchema(context)
        }
    }


    private fun buildAnySchema(): JsonSchema {
        return JsonSchema(schemaUtils.anyObjectSchema(), TypeData.createWildcard())
    }


    @Suppress("LongMethod")
    private fun buildPrimitiveSchema(typeData: TypeData): JsonSchema? {
        return when (typeData.identifyingName.full) {
            Number::class.qualifiedName -> schemaUtils.numericSchema(
                integer = false,
                min = null,
                max = null,
            )
            Byte::class.qualifiedName -> schemaUtils.numericSchema(
                integer = true,
                min = Byte.MIN_VALUE,
                max = Byte.MAX_VALUE,
            )
            Short::class.qualifiedName -> schemaUtils.numericSchema(
                integer = true,
                min = Short.MIN_VALUE,
                max = Short.MAX_VALUE,
            )
            Int::class.qualifiedName -> schemaUtils.numericSchema(
                integer = true,
                min = Int.MIN_VALUE,
                max = Int.MAX_VALUE,
            )
            Long::class.qualifiedName -> schemaUtils.numericSchema(
                integer = true,
                min = Long.MIN_VALUE,
                max = Long.MAX_VALUE,
            )
            UByte::class.qualifiedName -> schemaUtils.numericSchema(
                integer = true,
                min = UByte.MIN_VALUE.toLong(),
                max = UByte.MAX_VALUE.toLong(),
            )
            UShort::class.qualifiedName -> schemaUtils.numericSchema(
                integer = true,
                min = UShort.MIN_VALUE.toLong(),
                max = UShort.MAX_VALUE.toLong(),
            )
            UInt::class.qualifiedName -> schemaUtils.numericSchema(
                integer = true,
                min = UInt.MIN_VALUE.toLong(),
                max = UInt.MAX_VALUE.toLong(),
            )
            ULong::class.qualifiedName -> schemaUtils.numericSchema(
                integer = true,
                min = null,
                max = null,
            )
            Float::class.qualifiedName -> schemaUtils.numericSchema(
                integer = false,
                min = Float.MIN_VALUE,
                max = Float.MAX_VALUE,
            )
            Double::class.qualifiedName -> schemaUtils.numericSchema(
                integer = false,
                min = Double.MIN_VALUE,
                max = Double.MAX_VALUE,
            )
            Boolean::class.qualifiedName -> schemaUtils.booleanSchema()
            Char::class.qualifiedName -> schemaUtils.stringSchema(
                min = 1,
                max = 1,
            )
            String::class.qualifiedName -> schemaUtils.stringSchema(
                min = null,
                max = null,
            )
            Any::class.qualifiedName -> schemaUtils.anyObjectSchema()
            Unit::class.qualifiedName -> schemaUtils.nullSchema()
            else -> null
        }?.let {
            JsonSchema(
                json = it,
                typeData = typeData
            )
        }
    }

    private fun buildEnumSchema(typeData: TypeData): JsonSchema {
        return JsonSchema(
            json = schemaUtils.enumSchema(
                values = typeData.enumData?.constants ?: emptyList()
            ),
            typeData = typeData
        )
    }

    private fun buildCollectionSchema(typeData: TypeData): JsonSchema {
        return JsonSchema(
            json = schemaUtils.arraySchema(
                items = schemaUtils.referenceSchema(typeData.collectionData!!.itemType.type),
                uniqueItems = typeData.collectionData?.unique ?: false
            ),
            typeData = typeData
        )
    }

    private fun buildMapSchema(typeData: TypeData): JsonSchema {
        return JsonSchema(
            json = schemaUtils.mapObjectSchema(
                values = schemaUtils.referenceSchema(typeData.mapData!!.valueType.type)
            ),
            typeData = typeData
        )
    }

    private fun buildWithSubtypes(typeData: TypeData): JsonSchema {
        return JsonSchema(
            json = schemaUtils.subtypesSchema(
                subtypes = typeData.subtypes.map { schemaUtils.referenceSchema(it) }
            ),
            typeData = typeData
        )
    }

    private fun buildObjectSchema(context: JsonSchemaGeneratorModule.Context): JsonSchema {
        if (context.typeData.isInlineValue) {
            return buildInlineObjectSchema(context)
        }

        val requiredProperties = mutableSetOf<String>()
        val propertySchemas = mutableMapOf<String, JsonNode>()

        collectMembers(context.typeData, context.knownTypeData).forEach { member ->
            propertySchemas[member.name] = schemaUtils.referenceSchema(member.type)
            val nullable = member.nullable
            val optional = member.optional && optionalAsNonRequired
            if (!nullable && !optional) {
                requiredProperties.add(member.name)
            }
        }

        return JsonSchema(
            json = schemaUtils.objectSchema(propertySchemas, requiredProperties),
            typeData = context.typeData
        )
    }

    private fun buildInlineObjectSchema(context: JsonSchemaGeneratorModule.Context): JsonSchema {
        val inlineType = context.typeData.members.first { it.kind == MemberKind.PROPERTY }
        val inlineTypeData = context.knownTypeData.find { it.id == inlineType.type }
            ?: throw NoSuchElementException("Could not find type-data for inline type ${inlineType.type}")
        val inlineTypeSchema = context.generate(inlineTypeData)
        return JsonSchema(
            json = inlineTypeSchema.json,
            typeData = context.typeData
        )
    }

    private fun collectMembers(typeData: TypeData, typeDataList: Collection<TypeData>): List<MemberData> {
        return buildList {
            addAll(typeData.members)
            typeData.supertypes.forEach { supertypeId ->
                typeDataList
                    .find { it.id == supertypeId }
                    ?.also { supertype -> addAll(collectMembers(supertype, typeDataList)) }
            }
        }
    }
}
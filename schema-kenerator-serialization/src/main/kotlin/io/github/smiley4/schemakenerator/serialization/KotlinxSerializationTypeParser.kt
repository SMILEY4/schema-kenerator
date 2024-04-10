package io.github.smiley4.schemakenerator.serialization

import io.github.smiley4.schemakenerator.core.parser.BaseTypeData
import io.github.smiley4.schemakenerator.core.parser.CollectionTypeData
import io.github.smiley4.schemakenerator.core.parser.EnumTypeData
import io.github.smiley4.schemakenerator.core.parser.InlineTypeRef
import io.github.smiley4.schemakenerator.core.parser.MapTypeData
import io.github.smiley4.schemakenerator.core.parser.ObjectTypeData
import io.github.smiley4.schemakenerator.core.parser.PrimitiveTypeData
import io.github.smiley4.schemakenerator.core.parser.PropertyData
import io.github.smiley4.schemakenerator.core.parser.PropertyType
import io.github.smiley4.schemakenerator.core.parser.TypeDataContext
import io.github.smiley4.schemakenerator.core.parser.TypeId
import io.github.smiley4.schemakenerator.core.parser.TypeParser
import io.github.smiley4.schemakenerator.core.parser.TypeRef
import io.github.smiley4.schemakenerator.core.parser.Visibility
import io.github.smiley4.schemakenerator.core.parser.WildcardTypeData
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.elementDescriptors
import kotlinx.serialization.descriptors.elementNames
import kotlinx.serialization.serializerOrNull
import kotlin.reflect.KClass
import kotlin.reflect.KType


@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
class KotlinxSerializationTypeParser(
    config: KotlinxSerializationTypeParserConfigBuilder.() -> Unit = {},
    context: TypeDataContext = TypeDataContext()
) : TypeParser<KotlinxSerializationTypeParserConfig>(KotlinxSerializationTypeParserConfigBuilder().apply(config).build(), context) {

    inline fun <reified T> parse(): TypeRef = this.parse(getKType<T>())


    override fun parse(type: KType): TypeRef {
        if (config.clearContext) {
            context.clear()
        }
        if (type.classifier is KClass<*>) {
            return (type.classifier as KClass<*>).serializerOrNull()
                ?.let { parseInternal(it.descriptor) }
                ?: parseInternalAsWildcard()
        } else {
            throw Exception("Type is not a class.")
        }
    }

    private fun parseInternal(descriptor: SerialDescriptor): TypeRef {
        val type = parseAsBaseTypeData(descriptor)
//        context.add(type)
        return InlineTypeRef(type)
    }

    private fun parseInternalAsWildcard(): TypeRef {
        val type = WildcardTypeData()
        return InlineTypeRef(type)
    }


    private fun parseAsBaseTypeData(descriptor: SerialDescriptor): BaseTypeData {
        return when (descriptor.serialName) {
            Unit::class.qualifiedName -> parsePrimitive(descriptor)
            UByte::class.qualifiedName -> parsePrimitive(descriptor)
            UShort::class.qualifiedName -> parsePrimitive(descriptor)
            UInt::class.qualifiedName -> parsePrimitive(descriptor)
            ULong::class.qualifiedName -> parsePrimitive(descriptor)
            else -> when (descriptor.kind) {
                StructureKind.LIST -> parseList(descriptor)
                StructureKind.MAP -> parseMap(descriptor)
                StructureKind.CLASS -> parseClass(descriptor)
                StructureKind.OBJECT -> parseObject(descriptor)
                PolymorphicKind.OPEN -> parseSealed(descriptor)
                PolymorphicKind.SEALED -> parseSealed(descriptor)
                PrimitiveKind.BOOLEAN -> parsePrimitive(descriptor)
                PrimitiveKind.BYTE -> parsePrimitive(descriptor)
                PrimitiveKind.CHAR -> parsePrimitive(descriptor)
                PrimitiveKind.DOUBLE -> parsePrimitive(descriptor)
                PrimitiveKind.FLOAT -> parsePrimitive(descriptor)
                PrimitiveKind.INT -> parsePrimitive(descriptor)
                PrimitiveKind.LONG -> parsePrimitive(descriptor)
                PrimitiveKind.SHORT -> parsePrimitive(descriptor)
                PrimitiveKind.STRING -> parsePrimitive(descriptor)
                SerialKind.ENUM -> parseEnum(descriptor)
                SerialKind.CONTEXTUAL -> parseClass(descriptor)
            }
        }
    }

    private fun parsePrimitive(descriptor: SerialDescriptor): BaseTypeData {
        val id = TypeId.build(descriptor.serialName)

        parseCustom(id, descriptor)?.also {
            return@parsePrimitive it
        }

        return PrimitiveTypeData(
            id = id,
            simpleName = descriptor.simpleName(),
            qualifiedName = descriptor.qualifiedName(),
        )
    }


    private fun parseList(descriptor: SerialDescriptor): BaseTypeData {
        val itemDescriptor = descriptor.getElementDescriptor(0)
        val itemType = parseInternal(itemDescriptor)

        val id = TypeId.build(descriptor.serialName, listOf(itemType))

        parseCustom(id, descriptor)?.also {
            return@parseList it
        }

        return CollectionTypeData(
            id = id,
            simpleName = descriptor.simpleName(),
            qualifiedName = descriptor.qualifiedName(),
            itemType = PropertyData(
                name = "item",
                type = itemType,
                nullable = itemDescriptor.isNullable,
                kind = PropertyType.PROPERTY,
                visibility = Visibility.PUBLIC,
            ),
        )
    }


    private fun parseMap(descriptor: SerialDescriptor): BaseTypeData {
        val keyDescriptor = descriptor.getElementDescriptor(0)
        val valueDescriptor = descriptor.getElementDescriptor(1)
        val keyType = parseInternal(keyDescriptor)
        val valueType = parseInternal(valueDescriptor)

        val id = TypeId.build(descriptor.serialName, listOf(keyType, valueType))
        parseCustom(id, descriptor)?.also {
            return@parseMap it
        }

        return MapTypeData(
            id = id,
            simpleName = descriptor.simpleName(),
            qualifiedName = descriptor.qualifiedName(),
            keyType = PropertyData(
                name = "key",
                type = keyType,
                nullable = keyDescriptor.isNullable,
                kind = PropertyType.PROPERTY,
                visibility = Visibility.PUBLIC,

                ),
            valueType = PropertyData(
                name = "value",
                type = valueType,
                nullable = valueDescriptor.isNullable,
                kind = PropertyType.PROPERTY,
                visibility = Visibility.PUBLIC,
            ),
        )
    }


    private fun parseClass(descriptor: SerialDescriptor): BaseTypeData {

        val id = TypeId.build(descriptor.serialName)
        parseCustom(id, descriptor)?.also {
            return@parseClass it
        }

        val members = buildList {
            for (i in 0..<descriptor.elementsCount) {
                val fieldDescriptor = descriptor.getElementDescriptor(i)
                val fieldName = descriptor.getElementName(i)
                val fieldType = parseInternal(fieldDescriptor)
                add(
                    PropertyData(
                        name = fieldName,
                        type = fieldType,
                        nullable = fieldDescriptor.isNullable,
                        kind = PropertyType.PROPERTY,
                        visibility = Visibility.PUBLIC,
                    )
                )
            }
        }
        return ObjectTypeData(
            id = id,
            simpleName = descriptor.simpleName(),
            qualifiedName = descriptor.qualifiedName(),
            members = members.toMutableList(),
        )
    }


    private fun parseSealed(descriptor: SerialDescriptor): BaseTypeData {

        val id = TypeId.build(descriptor.serialName)
        parseCustom(id, descriptor)?.also {
            return@parseSealed it
        }

        val subTypes = descriptor.elementDescriptors.toList()[1].elementDescriptors.map { parseInternal(it) }
        return ObjectTypeData(
            id = id,
            simpleName = descriptor.simpleName(),
            qualifiedName = descriptor.qualifiedName(),
            subtypes = subTypes.toMutableList(),
        )
    }


    private fun parseObject(descriptor: SerialDescriptor): BaseTypeData {

        val id = TypeId.build(descriptor.serialName)
        parseCustom(id, descriptor)?.also {
            return@parseObject it
        }

        return ObjectTypeData(
            id = id,
            simpleName = descriptor.simpleName(),
            qualifiedName = descriptor.qualifiedName(),
        )
    }


    private fun parseEnum(descriptor: SerialDescriptor): BaseTypeData {

        val id = TypeId.build(descriptor.serialName)
        parseCustom(id, descriptor)?.also {
            return@parseEnum it
        }

        return EnumTypeData(
            id = id,
            simpleName = descriptor.simpleName(),
            qualifiedName = descriptor.qualifiedName(),
            enumConstants = descriptor.elementNames.toMutableList(),
        )
    }


    private fun parseCustom(id: TypeId, descriptor: SerialDescriptor): BaseTypeData? {
        val customParser = getCustomParser(descriptor)
        if (customParser != null) {
            return customParser.parse(id, descriptor)
        }
        return null
    }

    private fun getCustomParser(descriptor: SerialDescriptor) = config.customParsers[descriptor.serialName] ?: config.customParser

}
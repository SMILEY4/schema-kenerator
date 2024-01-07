package io.github.smiley4.schemakenerator.serialization

import io.github.smiley4.schemakenerator.core.parser.CollectionTypeData
import io.github.smiley4.schemakenerator.core.parser.EnumTypeData
import io.github.smiley4.schemakenerator.core.parser.MapTypeData
import io.github.smiley4.schemakenerator.core.parser.ObjectTypeData
import io.github.smiley4.schemakenerator.core.parser.PrimitiveTypeData
import io.github.smiley4.schemakenerator.core.parser.PropertyData
import io.github.smiley4.schemakenerator.core.parser.TypeId
import io.github.smiley4.schemakenerator.core.parser.TypeParser
import io.github.smiley4.schemakenerator.core.parser.TypeParserContext
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
    context: TypeParserContext = TypeParserContext()
) : TypeParser<KotlinxSerializationTypeParserConfig>(KotlinxSerializationTypeParserConfigBuilder().apply(config).build(), context) {

    inline fun <reified T> parse(): TypeId = this.parse(getKType<T>())


    override fun parse(type: KType): TypeId {
        if (type.classifier is KClass<*>) {
            return (type.classifier as KClass<*>).serializerOrNull()
                ?.let { parse(it.descriptor) }
                ?: context.add(WildcardTypeData())
        } else {
            throw Exception("Type is not a class.")
        }
    }


    private fun parse(descriptor: SerialDescriptor): TypeId {
        if (descriptor.serialName == "kotlin.Unit") {
            return parsePrimitive(descriptor)
        }
        return when (descriptor.kind) {
            StructureKind.LIST -> parseList(descriptor)
            StructureKind.MAP -> parseMap(descriptor)
            StructureKind.CLASS -> parseClass(descriptor)
            StructureKind.OBJECT -> parseObject(descriptor)
            PolymorphicKind.OPEN -> parseOpen(descriptor)
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


    private fun parsePrimitive(descriptor: SerialDescriptor): TypeId {
        val id = TypeId.build(descriptor.serialName)
        val customParser = config.customParsers[descriptor.serialName]
        if(customParser != null)  {
            return customParser.parse(id, descriptor).let { context.add(it) }
        }
        return PrimitiveTypeData(
            id = id,
            simpleName = toSimpleName(descriptor.serialName),
            qualifiedName = descriptor.serialName,
        ).let { context.add(it) }
    }

    private fun parseList(descriptor: SerialDescriptor): TypeId {
        val itemDescriptor = descriptor.getElementDescriptor(0)
        val itemType = parse(itemDescriptor)

        val id = TypeId.build(descriptor.serialName, listOf(itemType))
        val customParser = config.customParsers[descriptor.serialName]
        if(customParser != null)  {
            return customParser.parse(id, descriptor).let { context.add(it) }
        }

        return CollectionTypeData(
            id = id,
            simpleName = toSimpleName(descriptor.serialName),
            qualifiedName = descriptor.serialName,
            itemType = PropertyData(
                name = "item",
                type = itemType,
                nullable = itemDescriptor.isNullable
            ),
        ).let { context.add(it) }
    }

    private fun parseMap(descriptor: SerialDescriptor): TypeId {
        val keyDescriptor = descriptor.getElementDescriptor(0)
        val valueDescriptor = descriptor.getElementDescriptor(1)
        val keyType = parse(keyDescriptor)
        val valueType = parse(valueDescriptor)

        val id = TypeId.build(descriptor.serialName, listOf(valueType, keyType))
        val customParser = config.customParsers[descriptor.serialName]
        if(customParser != null)  {
            return customParser.parse(id, descriptor).let { context.add(it) }
        }

        return MapTypeData(
            id = id,
            simpleName = toSimpleName(descriptor.serialName),
            qualifiedName = descriptor.serialName,
            keyType = PropertyData(
                name = "key",
                type = keyType,
                nullable = keyDescriptor.isNullable
            ),
            valueType = PropertyData(
                name = "value",
                type = valueType,
                nullable = valueDescriptor.isNullable
            ),
        ).let { context.add(it) }
    }

    private fun parseClass(descriptor: SerialDescriptor): TypeId {

        val id = TypeId.build(descriptor.serialName)
        val customParser = config.customParsers[descriptor.serialName]
        if(customParser != null)  {
            return customParser.parse(id, descriptor).let { context.add(it) }
        }

        val members = buildList {
            for (i in 0..<descriptor.elementsCount) {
                val fieldDescriptor = descriptor.getElementDescriptor(i)
                val fieldName = descriptor.getElementName(i)
                val fieldType = parse(fieldDescriptor)
                add(
                    PropertyData(
                        name = fieldName,
                        type = fieldType,
                        nullable = fieldDescriptor.isNullable
                    )
                )
            }
        }
        return ObjectTypeData(
            id = id,
            simpleName = toSimpleName(descriptor.serialName),
            qualifiedName = descriptor.serialName,
            members = members,
        ).let { context.add(it) }
    }

    private fun parseOpen(descriptor: SerialDescriptor): TypeId {
        return parseSealed(descriptor)
    }

    private fun parseSealed(descriptor: SerialDescriptor): TypeId {

        val id = TypeId.build(descriptor.serialName)
        val customParser = config.customParsers[descriptor.serialName]
        if(customParser != null)  {
            return customParser.parse(id, descriptor).let { context.add(it) }
        }

        val subTypes = descriptor.elementDescriptors.toList()[1].elementDescriptors.map { parse(it) }
        return ObjectTypeData(
            id = id,
            simpleName = toSimpleName(descriptor.serialName),
            qualifiedName = descriptor.serialName,
            subtypes = subTypes,
        ).let { context.add(it) }
    }

    private fun parseObject(descriptor: SerialDescriptor): TypeId {

        val id = TypeId.build(descriptor.serialName)
        val customParser = config.customParsers[descriptor.serialName]
        if(customParser != null)  {
            return customParser.parse(id, descriptor).let { context.add(it) }
        }

        return ObjectTypeData(
            id = id,
            simpleName = toSimpleName(descriptor.serialName),
            qualifiedName = descriptor.serialName,
        ).let { context.add(it) }
    }

    private fun parseEnum(descriptor: SerialDescriptor): TypeId {

        val id = TypeId.build(descriptor.serialName)
        val customParser = config.customParsers[descriptor.serialName]
        if(customParser != null)  {
            return customParser.parse(id, descriptor).let { context.add(it) }
        }

        return EnumTypeData(
            id = id,
            simpleName = toSimpleName(descriptor.serialName),
            qualifiedName = descriptor.serialName,
            enumConstants = descriptor.elementNames.toList(),
        ).let { context.add(it) }
    }

    private fun toSimpleName(serialName: String): String {
        return serialName.split(".").last().replace("?", "")
    }

}
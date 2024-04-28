@file:OptIn(ExperimentalSerializationApi::class, InternalSerializationApi::class)

package io.github.smiley4.schemakenerator.serialization

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.CollectionTypeData
import io.github.smiley4.schemakenerator.core.data.EnumTypeData
import io.github.smiley4.schemakenerator.core.data.MapTypeData
import io.github.smiley4.schemakenerator.core.data.ObjectTypeData
import io.github.smiley4.schemakenerator.core.data.PrimitiveTypeData
import io.github.smiley4.schemakenerator.core.data.PropertyData
import io.github.smiley4.schemakenerator.core.data.PropertyType
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.core.data.Visibility
import io.github.smiley4.schemakenerator.core.data.WildcardTypeData
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


class KotlinxSerializationTypeProcessor {

    fun process(types: Collection<KType>): List<BaseTypeData> {
        val typeData = mutableListOf<BaseTypeData>()
        types.forEach { process(it, typeData) }
        return typeData.reversed()
    }

    private fun process(type: KType, typeData: MutableList<BaseTypeData>) {
        if (type.classifier is KClass<*>) {
            (type.classifier as KClass<*>).serializerOrNull()
                ?.let { parse(it.descriptor, typeData) }
                ?: parseWildcard(typeData)
        } else {
            throw Exception("Type is not a class.")
        }
    }

    private fun parse(descriptor: SerialDescriptor, typeData: MutableList<BaseTypeData>): BaseTypeData {
        return when (descriptor.cleanSerialName()) {
            Unit::class.qualifiedName -> parsePrimitive(descriptor, typeData)
            UByte::class.qualifiedName -> parsePrimitive(descriptor, typeData)
            UShort::class.qualifiedName -> parsePrimitive(descriptor, typeData)
            UInt::class.qualifiedName -> parsePrimitive(descriptor, typeData)
            ULong::class.qualifiedName -> parsePrimitive(descriptor, typeData)
            else -> when (descriptor.kind) {
                StructureKind.LIST -> parseList(descriptor, typeData)
                StructureKind.MAP -> parseMap(descriptor, typeData)
                StructureKind.CLASS -> parseClass(descriptor, typeData)
                StructureKind.OBJECT -> parseObject(descriptor, typeData)
                PolymorphicKind.OPEN -> parseSealed(descriptor, typeData)
                PolymorphicKind.SEALED -> parseSealed(descriptor, typeData)
                PrimitiveKind.BOOLEAN -> parsePrimitive(descriptor, typeData)
                PrimitiveKind.BYTE -> parsePrimitive(descriptor, typeData)
                PrimitiveKind.CHAR -> parsePrimitive(descriptor, typeData)
                PrimitiveKind.DOUBLE -> parsePrimitive(descriptor, typeData)
                PrimitiveKind.FLOAT -> parsePrimitive(descriptor, typeData)
                PrimitiveKind.INT -> parsePrimitive(descriptor, typeData)
                PrimitiveKind.LONG -> parsePrimitive(descriptor, typeData)
                PrimitiveKind.SHORT -> parsePrimitive(descriptor, typeData)
                PrimitiveKind.STRING -> parsePrimitive(descriptor, typeData)
                SerialKind.ENUM -> parseEnum(descriptor, typeData)
                SerialKind.CONTEXTUAL -> parseClass(descriptor, typeData)
            }
        }
    }

    private fun parseWildcard(typeData: MutableList<BaseTypeData>): BaseTypeData {
        val type = WildcardTypeData()
        return typeData.find(type.id) ?: type.also { typeData.add(it) }
    }

    private fun parsePrimitive(descriptor: SerialDescriptor, typeData: MutableList<BaseTypeData>): BaseTypeData {
        val id = TypeId.build(descriptor.cleanSerialName())
        return typeData.find(id)
            ?: PrimitiveTypeData(
                id = id,
                simpleName = descriptor.simpleName(),
                qualifiedName = descriptor.qualifiedName(),
            ).also { typeData.add(it) }
    }

    private fun parseList(descriptor: SerialDescriptor, typeData: MutableList<BaseTypeData>): BaseTypeData {
        val itemDescriptor = descriptor.getElementDescriptor(0)
        val itemType = parse(itemDescriptor, typeData)
        val id = TypeId.build(descriptor.cleanSerialName(), listOf(itemType.id))
        return typeData.find(id)
            ?: CollectionTypeData(
                id = id,
                simpleName = descriptor.simpleName(),
                qualifiedName = descriptor.qualifiedName(),
                itemType = PropertyData(
                    name = "item",
                    type = itemType.id,
                    nullable = itemDescriptor.isNullable,
                    kind = PropertyType.PROPERTY,
                    visibility = Visibility.PUBLIC,
                ),
            ).also { typeData.add(it) }
    }

    private fun parseMap(descriptor: SerialDescriptor, typeData: MutableList<BaseTypeData>): BaseTypeData {
        val keyDescriptor = descriptor.getElementDescriptor(0)
        val valueDescriptor = descriptor.getElementDescriptor(1)
        val keyType = parse(keyDescriptor, typeData)
        val valueType = parse(valueDescriptor, typeData)
        val id = TypeId.build(descriptor.cleanSerialName(), listOf(keyType.id, valueType.id))
        return typeData.find(id)
            ?: MapTypeData(
                id = id,
                simpleName = descriptor.simpleName(),
                qualifiedName = descriptor.qualifiedName(),
                keyType = PropertyData(
                    name = "key",
                    type = keyType.id,
                    nullable = keyDescriptor.isNullable,
                    kind = PropertyType.PROPERTY,
                    visibility = Visibility.PUBLIC,

                    ),
                valueType = PropertyData(
                    name = "value",
                    type = valueType.id,
                    nullable = valueDescriptor.isNullable,
                    kind = PropertyType.PROPERTY,
                    visibility = Visibility.PUBLIC,
                ),
            ).also { typeData.add(it) }
    }

    private fun parseClass(descriptor: SerialDescriptor, typeData: MutableList<BaseTypeData>): BaseTypeData {
        val id = getUniqueId(descriptor, emptyList(), typeData) // generate unique for each object since generic types cannot be respected in id
        return typeData.find(id)
            ?: ObjectTypeData(
                id = id,
                simpleName = descriptor.simpleName(),
                qualifiedName = descriptor.qualifiedName(),
                members = buildList {
                    for (i in 0..<descriptor.elementsCount) {
                        val fieldDescriptor = descriptor.getElementDescriptor(i)
                        val fieldName = descriptor.getElementName(i)
                        val fieldType = parse(fieldDescriptor, typeData)
                        add(
                            PropertyData(
                                name = fieldName,
                                type = fieldType.id,
                                nullable = fieldDescriptor.isNullable,
                                kind = PropertyType.PROPERTY,
                                visibility = Visibility.PUBLIC,
                            )
                        )
                    }
                }.toMutableList(),
            ).also { typeData.add(it) }
    }

    private fun parseSealed(descriptor: SerialDescriptor, typeData: MutableList<BaseTypeData>): BaseTypeData {
        val id = getUniqueId(descriptor, emptyList(), typeData) // generate unique for each object since generic types cannot be respected in id
        return typeData.find(id)
            ?: ObjectTypeData(
                id = id,
                simpleName = descriptor.simpleName(),
                qualifiedName = descriptor.qualifiedName(),
                subtypes = descriptor.elementDescriptors
                    .toList()[1].elementDescriptors
                    .map { parse(it, typeData).id }
                    .toMutableList(),
            ).also { typeData.add(it) }
    }


    private fun parseObject(descriptor: SerialDescriptor, typeData: MutableList<BaseTypeData>): BaseTypeData {
        val id = TypeId.build(descriptor.cleanSerialName())
        return typeData.find(id)
            ?: ObjectTypeData(
                id = id,
                simpleName = descriptor.simpleName(),
                qualifiedName = descriptor.qualifiedName(),
            ).also { typeData.add(it) }
    }


    private fun parseEnum(descriptor: SerialDescriptor, typeData: MutableList<BaseTypeData>): BaseTypeData {
        val id = TypeId.build(descriptor.cleanSerialName())
        return typeData.find(id)
            ?: EnumTypeData(
                id = id,
                simpleName = descriptor.simpleName(),
                qualifiedName = descriptor.qualifiedName(),
                enumConstants = descriptor.elementNames.toMutableList(),
            ).also { typeData.add(it) }
    }

    private fun getUniqueId(descriptor: SerialDescriptor, typeParameters: List<TypeId>, typeData: MutableList<BaseTypeData>): TypeId {
        return if (typeData.find(TypeId.build(descriptor.cleanSerialName(), typeParameters)) != null) {
            TypeId.build(descriptor.cleanSerialName(), typeParameters, true)
        } else {
            TypeId.build(descriptor.cleanSerialName(), typeParameters)
        }
    }

    private fun Collection<BaseTypeData>.find(id: TypeId): BaseTypeData? {
        return this.find { it.id.full() == id.full() }
    }

    private fun SerialDescriptor.cleanSerialName() = this.serialName.replace("?", "")

}
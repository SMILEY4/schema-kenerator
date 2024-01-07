package io.github.smiley4.schemakenerator.parser.reflection

import io.github.smiley4.schemakenerator.getMembersSafe
import io.github.smiley4.schemakenerator.parser.core.*
import kotlin.reflect.*

class PropertyParser(private val typeParser: ReflectionTypeParser) {

    fun parse(
        clazz: KClass<*>,
        resolvedTypeParameters: Map<String, TypeParameterData>,
        supertypes: List<TypeId>
    ): List<PropertyData> {
        return if (BASE_TYPES.contains(clazz)) {
            emptyList()
        } else {
            clazz.getMembersSafe()
                .asSequence()
                .filter { member ->
                    typeParser.config.propertyFilters
                        .map { it.filterProperty(member) }
                        .filter { it !== PropertyFilterResult.DO_NOT_CARE }
                        .any { it == PropertyFilterResult.REMOVE }
                }
                .mapNotNull { member ->
                    when (member) {
                        is KProperty<*> -> parseProperty(member, resolvedTypeParameters)
                        is KFunction<*> -> parseFunction(member, resolvedTypeParameters)
                        else -> null
                    }
                }
                .filter { !checkIsSupertypeMember(it, supertypes) }
                .filter { member ->
                    typeParser.config.propertyFilters
                        .map { it.filterProperty(member) }
                        .filter { it !== PropertyFilterResult.DO_NOT_CARE }
                        .any { it == PropertyFilterResult.REMOVE }
                }
                .distinctBy { it.name }
                .toList()
        }
    }

    private fun parseProperty(
        member: KProperty<*>,
        resolvedTypeParameters: Map<String, TypeParameterData>
    ): PropertyData {
        return PropertyData(
            name = member.name,
            type = resolveMemberType(member.returnType, resolvedTypeParameters),
            nullable = member.returnType.isMarkedNullable,
            kind = PropertyType.PROPERTY,
            visibility = if (member.visibility == KVisibility.PUBLIC) Visibility.PUBLIC else Visibility.HIDDEN
        )
    }

    private fun parseFunction(
        member: KFunction<*>,
        resolvedTypeParameters: Map<String, TypeParameterData>
    ): PropertyData {
        return PropertyData(
            name = member.name,
            type = resolveMemberType(member.returnType, resolvedTypeParameters),
            nullable = member.returnType.isMarkedNullable,
            kind = PropertyType.FUNCTION,
            visibility = if (member.visibility == KVisibility.PUBLIC) Visibility.PUBLIC else Visibility.HIDDEN
        )
    }

    private fun resolveMemberType(type: KType, providedTypeParameters: Map<String, TypeParameterData>): TypeId {
        return when (val classifier = type.classifier) {
            is KClass<*> -> {
                typeParser.getClassParser().parse(type, classifier, providedTypeParameters)
            }
            is KTypeParameter -> {
                providedTypeParameters[classifier.name]?.type ?: TypeId.wildcard()
            }
            else -> {
                throw Exception("Unhandled classifier type")
            }
        }
    }

    private fun checkIsSupertypeMember(member: PropertyData, supertypes: List<TypeId>): Boolean {
        val resolvedSupertypes =
            supertypes.mapNotNull { typeParser.context.getData(it) }.filterIsInstance<ObjectTypeData>()
        val supertypeMembers = resolvedSupertypes.flatMap { it.members }
        return supertypeMembers.any { it.name == member.name && it.type.id == member.type.id && it.nullable == member.nullable }
    }

}
package io.github.smiley4.schemakenerator.reflection.parsers

import io.github.smiley4.schemakenerator.core.parser.ObjectTypeData
import io.github.smiley4.schemakenerator.core.parser.PropertyData
import io.github.smiley4.schemakenerator.core.parser.PropertyFilter
import io.github.smiley4.schemakenerator.core.parser.PropertyType
import io.github.smiley4.schemakenerator.core.parser.TypeParameterData
import io.github.smiley4.schemakenerator.core.parser.TypeRef
import io.github.smiley4.schemakenerator.core.parser.Visibility
import io.github.smiley4.schemakenerator.core.parser.idStr
import io.github.smiley4.schemakenerator.core.parser.resolve
import io.github.smiley4.schemakenerator.reflection.getMembersSafe
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.KTypeParameter
import kotlin.reflect.KVisibility

class PropertyParser(private val typeParser: ReflectionTypeParser) {

    fun parse(clazz: KClass<*>, resolvedTypeParameters: Map<String, TypeParameterData>, supertypes: List<TypeRef>): List<PropertyData> {
        return if (typeParser.config.primitiveTypes.contains(clazz)) {
            emptyList()
        } else {
            clazz.getMembersSafe()
                .asSequence()
                .filter { PropertyFilter.applyFilters(it, typeParser.config.propertyFilters) }
                .mapNotNull { member ->
                    when (member) {
                        is KProperty<*> -> parseProperty(member, resolvedTypeParameters)
                        is KFunction<*> -> parseFunction(member, resolvedTypeParameters)
                        else -> null
                    }
                }
                .filter { !checkIsSupertypeMember(it, supertypes) }
                .filter { PropertyFilter.applyFilters(it, typeParser.config.propertyFilters) }
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

    private fun resolveMemberType(type: KType, providedTypeParameters: Map<String, TypeParameterData>): TypeRef {
        return when (val classifier = type.classifier) {
            is KClass<*> -> {
                typeParser.getClassParser().parse(type, classifier, providedTypeParameters)
            }
            is KTypeParameter -> {
                providedTypeParameters[classifier.name]?.type ?: throw Exception("No type parameter provided")
            }
            else -> {
                throw Exception("Unhandled classifier type")
            }
        }
    }

    private fun checkIsSupertypeMember(member: PropertyData, supertypes: List<TypeRef>): Boolean {
        val resolvedSupertypes = supertypes
            .mapNotNull { it.resolve(typeParser.context) }
            .filterIsInstance<ObjectTypeData>()
        val supertypeMembers = resolvedSupertypes.flatMap { it.members }
        return supertypeMembers.any { it.name == member.name && it.type.idStr() == member.type.idStr() && it.nullable == member.nullable }
    }

}
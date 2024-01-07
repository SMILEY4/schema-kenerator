package io.github.smiley4.schemakenerator.reflection

import io.github.smiley4.schemakenerator.core.parser.ObjectTypeData
import io.github.smiley4.schemakenerator.core.parser.PropertyData
import io.github.smiley4.schemakenerator.core.parser.TypeId
import io.github.smiley4.schemakenerator.core.parser.TypeParameterData
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.KTypeParameter

class MemberParser(private val typeParser: ReflectionTypeParser) {

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
                .mapNotNull { member ->
                    when (member) {
                        is KProperty<*> -> PropertyData(
                            name = member.name,
                            type = resolveMemberType(member.returnType, resolvedTypeParameters),
                            nullable = member.returnType.isMarkedNullable
                        )
                        is KFunction<*> -> PropertyData(
                            name = member.name,
                            type = resolveMemberType(member.returnType, resolvedTypeParameters),
                            nullable = member.returnType.isMarkedNullable
                        )
                        else -> null
                    }
                }
                .filter { !checkIsSupertypeMember(it, supertypes) }
                .distinctBy { it.name + "" + it.type.id }
                .toList()
        }
    }

    private fun resolveMemberType(type: KType, providedTypeParameters: Map<String, TypeParameterData>): TypeId {
        return when (val classifier = type.classifier) {
            is KClass<*> -> {
                typeParser.getClassParser().parse(type, classifier, providedTypeParameters)
            }
            is KTypeParameter -> {
                providedTypeParameters[classifier.name]!!.type
            }
            else -> {
                throw Exception("Unhandled classifier type")
            }
        }
    }

    private fun checkIsSupertypeMember(member: PropertyData, supertypes: List<TypeId>): Boolean {
        val resolvedSupertypes = supertypes.mapNotNull { typeParser.context.getData(it) }.filterIsInstance<ObjectTypeData>()
        val supertypeMembers = resolvedSupertypes.flatMap { it.members }
        return supertypeMembers.any { it.name == member.name && it.type.id == member.type.id && it.nullable == member.nullable }
    }

}
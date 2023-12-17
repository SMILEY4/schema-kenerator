package io.github.smiley4.schemakenerator.parser.reflection

import io.github.smiley4.schemakenerator.getMembersSafe
import io.github.smiley4.schemakenerator.parser.core.BASE_TYPES
import io.github.smiley4.schemakenerator.parser.data.MemberData
import io.github.smiley4.schemakenerator.parser.data.TypeParameterData
import io.github.smiley4.schemakenerator.parser.data.TypeRef
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.KTypeParameter

class MemberParser(private val typeParser: TypeReflectionParser) {

    fun parse(
        clazz: KClass<*>,
        resolvedTypeParameters: Map<String, TypeParameterData>,
        supertypes: List<TypeRef>
    ): List<MemberData> {
        return if (BASE_TYPES.contains(clazz)) {
            emptyList()
        } else {
            clazz.getMembersSafe()
                .onEach { println(it) }
                .filterIsInstance<KProperty<*>>()
                .filter { typeParser.getConfig().memberFilter.filterProperty(it) }
                .map { member ->
                    MemberData(
                        name = member.name,
                        type = resolveMemberType(member.returnType, resolvedTypeParameters),
                        nullable = member.returnType.isMarkedNullable
                    )
                }
                .filter { !checkIsSupertypeMember(it, supertypes) }
                .filter { typeParser.getConfig().memberFilter.filterProperty(it, typeParser.getContext()) }
        }
    }

    private fun resolveMemberType(type: KType, providedTypeParameters: Map<String, TypeParameterData>): TypeRef {
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

    private fun checkIsSupertypeMember(member: MemberData, supertypes: List<TypeRef>): Boolean {
        val resolvedSupertypes = supertypes.mapNotNull { typeParser.getContext().getData(it) }
        val supertypeMembers = resolvedSupertypes.flatMap { it.members }
        return supertypeMembers.any { it.name == member.name && it.type.id == member.type.id && it.nullable == member.nullable }
    }

}
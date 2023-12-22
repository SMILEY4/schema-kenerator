package io.github.smiley4.schemakenerator.parser.reflection

import io.github.smiley4.schemakenerator.parser.core.TypeId
import io.github.smiley4.schemakenerator.parser.core.TypeParameterData
import kotlin.reflect.KClass
import kotlin.reflect.KType

class SupertypeParser(private val typeParser: ReflectionTypeParser) {

    fun parse(clazz: KClass<*>, resolvedTypeParameters: Map<String, TypeParameterData>): List<TypeId> {
        return if (BASE_TYPES.contains(clazz)) {
            emptyList()
        } else {
            clazz.supertypes
                .map { resolveSupertype(it, resolvedTypeParameters) }
        }
    }

    private fun resolveSupertype(type: KType, providedTypeParameters: Map<String, TypeParameterData>): TypeId {
        return when (val classifier = type.classifier) {
            is KClass<*> -> {
                typeParser.getClassParser().parse(type, classifier, providedTypeParameters)
            }
            else -> {
                throw Exception("Unhandled classifier type")
            }
        }
    }

}
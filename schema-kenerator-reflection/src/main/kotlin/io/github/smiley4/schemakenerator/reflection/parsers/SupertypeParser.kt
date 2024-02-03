package io.github.smiley4.schemakenerator.reflection.parsers

import io.github.smiley4.schemakenerator.core.parser.TypeParameterData
import io.github.smiley4.schemakenerator.core.parser.TypeRef
import kotlin.reflect.KClass
import kotlin.reflect.KType

class SupertypeParser(private val typeParser: ReflectionTypeParser) {

    fun parse(clazz: KClass<*>, resolvedTypeParameters: Map<String, TypeParameterData>): List<TypeRef> {
        return if (typeParser.config.primitiveTypes.contains(clazz)) {
            emptyList()
        } else {
            clazz.supertypes
                .filter { !typeParser.config.ignoreSupertypes.contains(it.classifier) }
                .map { resolveSupertype(it, resolvedTypeParameters) }
        }
    }

    private fun resolveSupertype(type: KType, providedTypeParameters: Map<String, TypeParameterData>): TypeRef {
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
package io.github.smiley4.schemakenerator.reflection

import io.github.smiley4.schemakenerator.core.parser.EnumTypeData
import io.github.smiley4.schemakenerator.core.parser.ObjectTypeData
import io.github.smiley4.schemakenerator.core.parser.TypeId
import io.github.smiley4.schemakenerator.core.parser.TypeParameterData
import kotlin.reflect.KClass
import kotlin.reflect.KType

class ClassParser(private val typeParser: ReflectionTypeParser) {

    fun parse(type: KType, clazz: KClass<*>, providedTypeParameters: Map<String, TypeParameterData>): TypeId {

        // resolve all type parameters
        val resolvedTypeParameters = typeParser.getTypeParameterParser().parse(type, clazz, providedTypeParameters)

        // check if the same type with the same type parameters has already been resolved -> reuse existing
        val id = TypeId.build(clazz.qualifiedName ?: "?", resolvedTypeParameters.values.map { it.type })
        if (typeParser.context.has(id)) {
            return id
        }

        // check custom parsers
        val customParser = typeParser.config.customParsers[clazz] ?: typeParser.config.customParser
        if (customParser != null) {
            val customParserResult = customParser.parse(id, clazz)
            if (customParserResult != null) {
                return customParserResult.let { typeParser.context.add(it) }
            }
        }

        // add placeholder to break out of infinite recursions
        typeParser.context.reserve(id)

        // collect more information
        val supertypes = typeParser.getSupertypeParser().parse(clazz, resolvedTypeParameters)
        val members = typeParser.getPropertyParser().parse(clazz, resolvedTypeParameters, supertypes)
        val enumValues = typeParser.getEnumValueParser().parse(clazz)

        // add type to context and return its id
        if (enumValues.isEmpty()) {
            return ObjectTypeData(
                id = id,
                simpleName = clazz.simpleName!!,
                qualifiedName = clazz.qualifiedName!!,
                typeParameters = resolvedTypeParameters,
                subtypes = emptyList(),
                supertypes = supertypes,
                members = members,
            ).let {
                typeParser.context.add(it)
            }
        } else {
            return EnumTypeData(
                id = id,
                simpleName = clazz.simpleName!!,
                qualifiedName = clazz.qualifiedName!!,
                typeParameters = resolvedTypeParameters,
                subtypes = emptyList(),
                supertypes = supertypes,
                members = members,
                enumConstants = enumValues
            ).let {
                typeParser.context.add(it)
            }
        }

    }

}
package io.github.smiley4.schemakenerator.reflection

import io.github.smiley4.schemakenerator.core.parser.AnnotationData
import io.github.smiley4.schemakenerator.core.parser.BaseTypeData
import io.github.smiley4.schemakenerator.core.parser.CollectionTypeData
import io.github.smiley4.schemakenerator.core.parser.EnumTypeData
import io.github.smiley4.schemakenerator.core.parser.MapTypeData
import io.github.smiley4.schemakenerator.core.parser.ObjectTypeData
import io.github.smiley4.schemakenerator.core.parser.PrimitiveTypeData
import io.github.smiley4.schemakenerator.core.parser.PropertyData
import io.github.smiley4.schemakenerator.core.parser.PropertyType
import io.github.smiley4.schemakenerator.core.parser.TypeId
import io.github.smiley4.schemakenerator.core.parser.TypeParameterData
import io.github.smiley4.schemakenerator.core.parser.Visibility
import io.github.smiley4.schemakenerator.core.parser.WildcardTypeData
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.KTypeParameter
import kotlin.reflect.KTypeProjection
import kotlin.reflect.KVisibility
import kotlin.reflect.full.starProjectedType

class ReflectionTypeProcessor(
    private val includeGetters: Boolean = false,
    private val includeWeakGetters: Boolean = false,
    private val includeFunctions: Boolean = false,
) {

    private val primitiveTypes = setOf<KClass<*>>(
        Number::class,
        Byte::class,
        Short::class,
        Int::class,
        Long::class,
        UByte::class,
        UShort::class,
        UInt::class,
        ULong::class,
        Float::class,
        Double::class,
        Boolean::class,
        Char::class,
        String::class,
        Any::class,
        Unit::class,
    )

    fun process(types: Collection<KType>): List<BaseTypeData> {
        val typeData = mutableListOf<BaseTypeData>()
        types.forEach { process(it, typeData) }
        return typeData.reversed()
    }


    private fun process(type: KType, typeData: MutableList<BaseTypeData>) {
        if (type.classifier is KClass<*>) {
            parseClass(type, type.classifier as KClass<*>, mapOf(), typeData)
        } else {
            throw Exception("Type is not a class.")
        }
    }

    // ====== CLASS ====================================================

    private fun parseClass(
        type: KType,
        clazz: KClass<*>,
        typeParameters: Map<String, TypeParameterData>,
        typeData: MutableList<BaseTypeData>
    ): BaseTypeData {

        // resolve type parameters, i.e. generic types
        val resolvedTypeParameters = parseTypeParameters(type, clazz, typeParameters, typeData)

        // build id & check if type already parsed
        val id = TypeId.build(clazz.qualifiedName ?: "?", resolvedTypeParameters.values.map { it.type })
        val existing = typeData.find { it.id == id }
        if (existing != null) {
            return existing
        }

        // add placeholder to break out of some infinite recursions
        typeData.add(BaseTypeData.placeholder(id))

        // determine class type, i.e. whether type is primitive, class, enum, collection, map, ...
        val classType = determineClassType(clazz)

        // collect supertypes
        val supertypes = if (classType == ClassType.OBJECT) {
            parseSupertypes(clazz, resolvedTypeParameters, typeData).map { it.id }
        } else {
            emptyList()
        }

        // collect subtypes
        val subtypes = if (classType == ClassType.OBJECT) {
            clazz.sealedSubclasses.map { parseClass(it.starProjectedType, it, typeParameters, typeData).id }
        } else {
            emptyList()
        }

        // collect enum constants
        val enumValues = if (classType == ClassType.ENUM) {
            parseEnum(clazz)
        } else {
            emptyList()
        }

        // collect member information
        val members = if (classType == ClassType.OBJECT) {
            parseProperties(clazz, resolvedTypeParameters, typeData)
        } else {
            emptyList()
        }

        // collect annotation information
        val annotations = parseAnnotation(clazz)

        return when (classType) {
            ClassType.PRIMITIVE -> PrimitiveTypeData(
                id = id,
                simpleName = clazz.simpleName!!,
                qualifiedName = clazz.qualifiedName!!,
                typeParameters = resolvedTypeParameters.toMutableMap(),
                annotations = annotations.toMutableList()
            )
            ClassType.OBJECT -> ObjectTypeData(
                id = id,
                simpleName = clazz.simpleName!!,
                qualifiedName = clazz.qualifiedName!!,
                typeParameters = resolvedTypeParameters.toMutableMap(),
                subtypes = subtypes.toMutableList(),
                supertypes = supertypes.toMutableList(),
                members = members.toMutableList(),
                annotations = annotations.toMutableList()
            )
            ClassType.ENUM -> EnumTypeData(
                id = id,
                simpleName = clazz.simpleName!!,
                qualifiedName = clazz.qualifiedName!!,
                typeParameters = resolvedTypeParameters.toMutableMap(),
                enumConstants = enumValues.toMutableList(),
                annotations = annotations.toMutableList()
            )
            ClassType.COLLECTION -> CollectionTypeData(
                id = id,
                simpleName = clazz.simpleName!!,
                qualifiedName = clazz.qualifiedName!!,
                typeParameters = resolvedTypeParameters.toMutableMap(),
                annotations = annotations.toMutableList(),
                itemType = resolvedTypeParameters["E"]?.let {
                    PropertyData(
                        name = "item",
                        type = it.type,
                        nullable = it.nullable,
                        visibility = Visibility.PUBLIC,
                        kind = PropertyType.PROPERTY,
                        annotations = emptyList()
                    )
                } ?: resolvedTypeParameters["T"]?.let {
                    PropertyData(
                        name = "item",
                        type = it.type,
                        nullable = it.nullable,
                        visibility = Visibility.PUBLIC,
                        kind = PropertyType.PROPERTY,
                        annotations = emptyList()
                    )
                }
                ?: unknownPropertyData("item", typeData)
            )
            ClassType.MAP -> MapTypeData(
                id = id,
                simpleName = clazz.simpleName!!,
                qualifiedName = clazz.qualifiedName!!,
                typeParameters = resolvedTypeParameters.toMutableMap(),
                annotations = annotations.toMutableList(),
                keyType = resolvedTypeParameters["K"]?.let {
                    PropertyData(
                        name = "key",
                        type = it.type,
                        nullable = it.nullable,
                        visibility = Visibility.PUBLIC,
                        kind = PropertyType.PROPERTY,
                        annotations = emptyList()
                    )
                } ?: unknownPropertyData("item", typeData),
                valueType = resolvedTypeParameters["V"]?.let {
                    PropertyData(
                        name = "value",
                        type = it.type,
                        nullable = it.nullable,
                        visibility = Visibility.PUBLIC,
                        kind = PropertyType.PROPERTY,
                        annotations = emptyList()
                    )
                } ?: unknownPropertyData("item", typeData)
            )
        }.also { result ->
            typeData.removeIf { it.id == result.id }
            typeData.add(result)
        }
    }

    // ====== TYPE PARAMETERS ==========================================

    private fun parseTypeParameters(
        type: KType,
        clazz: KClass<*>,
        providedTypeParameters: Map<String, TypeParameterData>,
        typeData: MutableList<BaseTypeData>
    ): Map<String, TypeParameterData> {
        return buildMap {
            for (index in type.arguments.indices) {
                val name = clazz.typeParameters[index].name
                val argType = type.arguments[index]
                this[name] = TypeParameterData(
                    name = name,
                    type = resolveTypeProjection(argType, providedTypeParameters, typeData).id,
                    nullable = argType.type?.isMarkedNullable ?: false,
                )
            }
        }
    }

    private fun resolveTypeProjection(
        typeProjection: KTypeProjection,
        providedTypeParameters: Map<String, TypeParameterData>,
        typeData: MutableList<BaseTypeData>
    ): BaseTypeData {
        if (typeProjection.type == null) {
            return resolveWildcard(typeData)
        }
        return when (val classifier = typeProjection.type?.classifier) {
            is KClass<*> -> {
                parseClass(typeProjection.type!!, classifier, providedTypeParameters, typeData)
            }
            is KTypeParameter -> {
                typeData.find { it.id == providedTypeParameters[classifier.name]?.type } ?: throw Exception("No type parameter provided")
            }
            else -> {
                throw Exception("Unhandled classifier type")
            }
        }
    }

    private fun resolveWildcard(typeData: MutableList<BaseTypeData>): BaseTypeData {
        val type = WildcardTypeData()
        return typeData.find { it.id == type.id }
            ?: type.also { typeData.add(it) }
    }

    // ====== PROPERTIES ===============================================

    private fun parseProperties(
        clazz: KClass<*>,
        resolvedTypeParameters: Map<String, TypeParameterData>,
        typeData: MutableList<BaseTypeData>
    ): List<PropertyData> {
        return clazz.getMembersSafe()
            .asSequence()
            .filter { filterMember(it) }
            .mapNotNull { member ->
                when (member) {
                    is KProperty<*> -> parseProperty(member, resolvedTypeParameters, typeData)
                    is KFunction<*> -> parseFunction(member, resolvedTypeParameters, typeData)
                    else -> null
                }
            }
            .distinctBy { it.name }
            .toList()
    }

    private fun filterMember(member: KCallable<*>): Boolean {
        if (member is KFunction<*>) {
            return when(determineFunctionPropertyType(member)) {
                FunctionPropertyType.GETTER -> includeGetters
                FunctionPropertyType.WEAK_GETTER -> includeWeakGetters
                FunctionPropertyType.FUNCTION -> includeFunctions
            }
        } else {
            return true
        }
    }

    private fun parseProperty(
        member: KProperty<*>,
        resolvedTypeParameters: Map<String, TypeParameterData>,
        typeData: MutableList<BaseTypeData>
    ): PropertyData {
        return PropertyData(
            name = member.name,
            type = resolveMemberType(member.returnType, resolvedTypeParameters, typeData).id,
            nullable = member.returnType.isMarkedNullable,
            annotations = parseAnnotation(member),
            kind = PropertyType.PROPERTY,
            visibility = if (member.visibility == KVisibility.PUBLIC) Visibility.PUBLIC else Visibility.HIDDEN
        )
    }

    private fun parseFunction(
        member: KFunction<*>,
        resolvedTypeParameters: Map<String, TypeParameterData>,
        typeData: MutableList<BaseTypeData>
    ): PropertyData {
        return PropertyData(
            name = member.name,
            type = resolveMemberType(member.returnType, resolvedTypeParameters, typeData).id,
            nullable = member.returnType.isMarkedNullable,
            annotations = parseAnnotation(member),
            kind = PropertyType.FUNCTION,
            visibility = if (member.visibility == KVisibility.PUBLIC) Visibility.PUBLIC else Visibility.HIDDEN
        )
    }

    private fun resolveMemberType(
        type: KType,
        providedTypeParameters: Map<String, TypeParameterData>,
        typeData: MutableList<BaseTypeData>
    ): BaseTypeData {
        return when (val classifier = type.classifier) {
            is KClass<*> -> {
                parseClass(type, classifier, providedTypeParameters, typeData)
            }
            is KTypeParameter -> {
                typeData.find { it.id == providedTypeParameters[classifier.name]?.type } ?: throw Exception("No type parameter provided")
            }
            else -> {
                throw Exception("Unhandled classifier type")
            }
        }
    }

    // ====== SUPERTYPES ==============================================

    fun parseSupertypes(
        clazz: KClass<*>,
        resolvedTypeParameters: Map<String, TypeParameterData>,
        typeData: MutableList<BaseTypeData>
    ): List<BaseTypeData> {
        return clazz.supertypes.map { resolveSupertype(it, resolvedTypeParameters, typeData) }
    }

    private fun resolveSupertype(
        type: KType,
        providedTypeParameters: Map<String, TypeParameterData>,
        typeData: MutableList<BaseTypeData>
    ): BaseTypeData {
        return when (val classifier = type.classifier) {
            is KClass<*> -> {
                parseClass(type, classifier, providedTypeParameters, typeData)
            }
            else -> {
                throw Exception("Unhandled classifier type")
            }
        }
    }

    // ====== ENUM =====================================================

    private fun parseEnum(clazz: KClass<*>): List<String> {
        return clazz.java.enumConstants?.map { it.toString() } ?: emptyList()
    }

    // ====== ANNOTATION ===============================================

    private fun parseAnnotation(clazz: KClass<*>): List<AnnotationData> {
        return unwrapAnnotations(clazz.annotations).map { parseAnnotation(it) }
    }

    private fun parseAnnotation(property: KProperty<*>): List<AnnotationData> {
        return unwrapAnnotations(property.annotations).map { parseAnnotation(it) }
    }

    private fun parseAnnotation(property: KFunction<*>): List<AnnotationData> {
        return unwrapAnnotations(property.annotations).map { parseAnnotation(it) }
    }

    private fun unwrapAnnotations(annotations: List<Annotation>): List<Annotation> {
        // "repeatable" annotations are wrapped in a container class and need to be unwrapped
        return annotations.flatMap { annotation ->
            if (isAnnotationContainer(annotation)) {
                unwrapContainer(annotation)
            } else {
                listOf(annotation)
            }
        }
    }

    private fun isAnnotationContainer(annotation: Annotation): Boolean {
        return annotation.annotationClass.java.declaredAnnotations
            .map { it.annotationClass.qualifiedName }
            .contains("kotlin.jvm.internal.RepeatableContainer")
    }

    private fun unwrapContainer(annotation: Annotation): List<Annotation> {
        try {
            // A repeatable annotation container must have a method "value" returning the array of repeated annotations.
            val valueMethod = annotation.javaClass.getMethod("value")
            @Suppress("UNCHECKED_CAST")
            return (valueMethod(annotation) as Array<Annotation>).asList()
        } catch (e: Exception) {
            return emptyList()
        }
    }

    private fun parseAnnotation(annotation: Annotation): AnnotationData {
        return AnnotationData(
            name = annotation.annotationClass.qualifiedName ?: "",
            annotation = annotation,
            values = annotation.annotationClass.members
                .filterIsInstance<KProperty<*>>()
                .associate { it.name to it.getter.call(annotation) }
                .toMutableMap()
        )
    }

    // ====== CLASS TYPE DECIDER =======================================

    private fun determineClassType(clazz: KClass<*>): ClassType {
        return when {
            primitiveTypes.contains(clazz) -> ClassType.PRIMITIVE
            isEnum(clazz) -> ClassType.ENUM
            isCollection(clazz) -> ClassType.COLLECTION
            isMap(clazz) -> ClassType.MAP
            else -> ClassType.OBJECT
        }
    }


    private fun isEnum(clazz: KClass<*>): Boolean {
        return clazz.java.enumConstants !== null
    }

    private fun isCollection(clazz: KClass<*>): Boolean {
        return if (clazz.qualifiedName == Collection::class.qualifiedName || clazz.qualifiedName == Array::class.qualifiedName) {
            true
        } else {
            clazz.supertypes
                .asSequence()
                .mapNotNull { it.classifier }
                .map { it as KClass<*> }
                .any { isCollection(it) }
        }
    }

    private fun isMap(clazz: KClass<*>): Boolean {
        return if (clazz.qualifiedName == Map::class.qualifiedName) {
            true
        } else {
            clazz.supertypes
                .asSequence()
                .mapNotNull { it.classifier }
                .map { it as KClass<*> }
                .any { isMap(it) }
        }
    }

    // ====== FUNCTION TYPE DECIDER ====================================

    private fun determineFunctionPropertyType(function: KFunction<*>): FunctionPropertyType {
        if (function.returnType == Unit::class || function.parameters.isNotEmpty()) {
            return FunctionPropertyType.FUNCTION
        }
        if (function.name.startsWith("get") || function.name.startsWith("is")) {
            return FunctionPropertyType.GETTER
        }
        return FunctionPropertyType.WEAK_GETTER
    }

    // ====== UTILS ====================================================

    private fun unknownPropertyData(name: String, typeData: MutableList<BaseTypeData>): PropertyData {
        if (typeData.none { it.id == TypeId.wildcard() }) {
            typeData.add(WildcardTypeData())
        }
        return PropertyData(
            name = name,
            type = TypeId.wildcard(),
            nullable = false,
            visibility = Visibility.PUBLIC,
            kind = PropertyType.PROPERTY,
            annotations = emptyList()
        )
    }

}
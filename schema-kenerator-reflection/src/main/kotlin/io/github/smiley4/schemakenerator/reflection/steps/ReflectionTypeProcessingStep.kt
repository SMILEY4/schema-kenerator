package io.github.smiley4.schemakenerator.reflection.steps

import io.github.smiley4.schemakenerator.core.data.AnnotationData
import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.CollectionTypeData
import io.github.smiley4.schemakenerator.core.data.EnumTypeData
import io.github.smiley4.schemakenerator.core.data.MapTypeData
import io.github.smiley4.schemakenerator.core.data.ObjectTypeData
import io.github.smiley4.schemakenerator.core.data.PlaceholderTypeData
import io.github.smiley4.schemakenerator.core.data.PrimitiveTypeData
import io.github.smiley4.schemakenerator.core.data.PropertyData
import io.github.smiley4.schemakenerator.core.data.PropertyType
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.core.data.TypeParameterData
import io.github.smiley4.schemakenerator.core.data.Visibility
import io.github.smiley4.schemakenerator.core.data.WildcardTypeData
import io.github.smiley4.schemakenerator.reflection.data.EnumConstType
import java.lang.reflect.Modifier
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.KTypeParameter
import kotlin.reflect.KTypeProjection
import kotlin.reflect.KVisibility
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.typeOf

/**
 * Processes the given type and extracts information about it using reflection.
 */
class ReflectionTypeProcessingStep(
    /**
     * Whether to include getters as members of classes (see [PropertyType.GETTER]).
     */
    private val includeGetters: Boolean = false,
    /**
     * Whether to include weak getters as members of classes (see [PropertyType.WEAK_GETTER]).
     */
    private val includeWeakGetters: Boolean = false,
    /**
     * Whether to include functions as members of classes (see [PropertyType.FUNCTION]).
     */
    private val includeFunctions: Boolean = false,
    /**
     * Whether to include hidden (e.g. private) members
     */
    private val includeHidden: Boolean = false,
    /**
     * Whether to include static members
     */
    private val includeStatic: Boolean = false,
    /**
     * The list of types that are considered "primitive types" and returned as [PrimitiveTypeData]
     */
    private val primitiveTypes: Collection<KClass<*>> = DEFAULT_PRIMITIVE_TYPES,
    /**
     * Use toString for enum values instead of the constant name
     */
    private val enumConstType: EnumConstType = EnumConstType.NAME,
    /**
     * custom processors for given types that overwrite the default behaviour
     */
    private val customProcessors: Map<KClass<*>, () -> BaseTypeData> = emptyMap(),
    /**
     * redirect types to other types, i.e. when a type is found as a key, the corresponding type will be processed instead
     */
    private val typeRedirects: Map<KType, KType> = DEFAULT_REDIRECTS
) {

    companion object {
        val DEFAULT_PRIMITIVE_TYPES = setOf<KClass<*>>(
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


        @OptIn(ExperimentalUnsignedTypes::class)
        val DEFAULT_REDIRECTS = mapOf(
            typeOf<BooleanArray>() to typeOf<Array<Boolean>>(),
            typeOf<ByteArray>() to typeOf<Array<Byte>>(),
            typeOf<UByteArray>() to typeOf<Array<UByte>>(),
            typeOf<ShortArray>() to typeOf<Array<Short>>(),
            typeOf<UShortArray>() to typeOf<Array<UShort>>(),
            typeOf<CharArray>() to typeOf<Array<Char>>(),
            typeOf<IntArray>() to typeOf<Array<Int>>(),
            typeOf<UIntArray>() to typeOf<Array<UInt>>(),
            typeOf<LongArray>() to typeOf<Array<Long>>(),
            typeOf<ULongArray>() to typeOf<Array<ULong>>(),
            typeOf<FloatArray>() to typeOf<Array<Float>>(),
            typeOf<DoubleArray>() to typeOf<Array<Double>>(),
        )
    }


    fun process(type: KType): Bundle<BaseTypeData> = process(Bundle(type, emptyList()))

    fun process(type: Bundle<KType>): Bundle<BaseTypeData> {
        val supportingTypeData = mutableListOf<BaseTypeData>()
        type.supporting.forEach { process(it, supportingTypeData) }

        val typeData = process(type.data, supportingTypeData)
        supportingTypeData.remove(typeData)

        return Bundle(
            data = typeData,
            supporting = supportingTypeData
        )
    }


    private fun process(type: KType, typeData: MutableList<BaseTypeData>): BaseTypeData {
        return if (typeRedirects.containsKey(type)) {
            process(typeRedirects[type]!!, typeData)
        } else if (type.classifier is KClass<*>) {
            parseClass(type, type.classifier as KClass<*>, mapOf(), typeData)
        } else {
            throw IllegalArgumentException("Type is not a class.")
        }
    }

    // ====== CLASS ====================================================

    @Suppress("CyclomaticComplexMethod", "LongMethod")
    private fun parseClass(
        type: KType,
        clazz: KClass<*>,
        typeParameters: Map<String, TypeParameterData>,
        typeData: MutableList<BaseTypeData>
    ): BaseTypeData {

        // check type redirects
        if (typeRedirects.containsKey(type)) {
            return process(typeRedirects[type]!!, typeData)
        }

        // check custom type processors
        if (customProcessors.containsKey(clazz)) {
            return customProcessors[clazz]!!.invoke().also { result ->
                typeData.removeIf { it.id == result.id }
                typeData.add(result)
            }
        }

        // resolve type parameters, i.e. generic types
        val resolvedTypeParameters = parseTypeParameters(type, clazz, typeParameters, typeData)

        // build id
        val id = TypeId.build(clazz.qualifiedName ?: "?", resolvedTypeParameters.values.map { it.type })

        // check if type already parsed
        val existing = typeData.find { it.id == id }
        if (existing != null) {
            return existing
        }

        // add placeholder to break out of some infinite recursions
        typeData.add(PlaceholderTypeData(id))

        // determine class type, i.e. whether type is primitive, class, enum, collection, map, ...
        val classType = determineClassType(type)

        // collect supertypes
        val supertypes = if (classType == TypeCategory.OBJECT) {
            parseSupertypes(clazz, resolvedTypeParameters, typeData).map { it.id }
        } else {
            emptyList()
        }

        // collect subtypes
        val subtypes = if (classType == TypeCategory.OBJECT) {
            clazz.sealedSubclasses.map { parseClass(it.starProjectedType, it, typeParameters, typeData).id }
        } else {
            emptyList()
        }

        // collect enum constants
        val enumValues = if (classType == TypeCategory.ENUM) {
            parseEnum(clazz)
        } else {
            emptyList()
        }

        // check if collection set of unique items
        val uniqueCollection = isCollectionUnique(type)

        // collect member information
        val members = if (classType == TypeCategory.OBJECT) {
            parseProperties(clazz, resolvedTypeParameters, typeData)
        } else {
            emptyList()
        }

        // collect annotation information
        val annotations = parseAnnotations(clazz)

        return when (classType) {
            TypeCategory.PRIMITIVE -> PrimitiveTypeData(
                id = id,
                simpleName = clazz.getSafeSimpleName(),
                qualifiedName = clazz.getSafeQualifiedName(),
                typeParameters = resolvedTypeParameters.toMutableMap(),
                annotations = annotations.toMutableList(),
                nullable = type.isMarkedNullable,
            )
            TypeCategory.OBJECT -> ObjectTypeData(
                id = id,
                simpleName = clazz.getSafeSimpleName(),
                qualifiedName = clazz.getSafeQualifiedName(),
                typeParameters = resolvedTypeParameters.toMutableMap(),
                subtypes = subtypes.toMutableList(),
                supertypes = supertypes.toMutableList(),
                members = members.toMutableList(),
                annotations = annotations.toMutableList(),
                nullable = type.isMarkedNullable,
                isInlineValue = clazz.isValue,
            )
            TypeCategory.ENUM -> EnumTypeData(
                id = id,
                simpleName = clazz.getSafeSimpleName(),
                qualifiedName = clazz.getSafeQualifiedName(),
                typeParameters = resolvedTypeParameters.toMutableMap(),
                enumConstants = enumValues.toMutableList(),
                annotations = annotations.toMutableList(),
                nullable = type.isMarkedNullable,
            )
            TypeCategory.COLLECTION -> CollectionTypeData(
                id = id,
                simpleName = clazz.getSafeSimpleName(),
                qualifiedName = clazz.getSafeQualifiedName(),
                typeParameters = resolvedTypeParameters.toMutableMap(),
                annotations = annotations.toMutableList(),
                nullable = type.isMarkedNullable,
                itemType = resolvedTypeParameters["E"]?.let {
                    PropertyData(
                        name = "item",
                        type = it.type,
                        nullable = it.nullable,
                        optional = false,
                        visibility = Visibility.PUBLIC,
                        kind = PropertyType.PROPERTY,
                        annotations = mutableListOf()
                    )
                } ?: resolvedTypeParameters["T"]?.let {
                    PropertyData(
                        name = "item",
                        type = it.type,
                        nullable = it.nullable,
                        optional = false,
                        visibility = Visibility.PUBLIC,
                        kind = PropertyType.PROPERTY,
                        annotations = mutableListOf()
                    )
                } ?: resolvedTypeParameters.entries.firstOrNull()?.let {
                    PropertyData(
                        name = "item",
                        type = it.value.type,
                        nullable = it.value.nullable,
                        optional = false,
                        visibility = Visibility.PUBLIC,
                        kind = PropertyType.PROPERTY,
                        annotations = mutableListOf()
                    )
                }
                ?: unknownPropertyData("item", typeData),
                unique = uniqueCollection
            )
            TypeCategory.MAP -> MapTypeData(
                id = id,
                simpleName = clazz.getSafeSimpleName(),
                qualifiedName = clazz.getSafeQualifiedName(),
                typeParameters = resolvedTypeParameters.toMutableMap(),
                annotations = annotations.toMutableList(),
                nullable = type.isMarkedNullable,
                keyType = resolvedTypeParameters["K"]?.let {
                    PropertyData(
                        name = "key",
                        type = it.type,
                        nullable = it.nullable,
                        optional = false,
                        visibility = Visibility.PUBLIC,
                        kind = PropertyType.PROPERTY,
                        annotations = mutableListOf()
                    )
                } ?: unknownPropertyData("item", typeData),
                valueType = resolvedTypeParameters["V"]?.let {
                    PropertyData(
                        name = "value",
                        type = it.type,
                        nullable = it.nullable,
                        optional = false,
                        visibility = Visibility.PUBLIC,
                        kind = PropertyType.PROPERTY,
                        annotations = mutableListOf()
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
        val namesProvided = clazz.typeParameters.size == type.arguments.size
        return buildMap {
            for (index in type.arguments.indices) {
                val name = if (namesProvided) clazz.typeParameters[index].name else "T"
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
                typeData.find { it.id == providedTypeParameters[classifier.name]?.type }
                    ?: throw IllegalArgumentException("No type parameter provided")
            }
            else -> {
                throw IllegalArgumentException("Unhandled classifier type")
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
                    is KProperty<*> -> parseProperty(member, resolvedTypeParameters, typeData, clazz)
                    is KFunction<*> -> parseFunction(member, resolvedTypeParameters, typeData)
                    else -> null
                }
            }
            .distinctBy { it.name }
            .toList()
    }

    private fun filterMember(member: KCallable<*>): Boolean {
        // check static
        if (!includeStatic) {
            when (member) {
                is KProperty<*> -> if (canAccessJavaField(member) && Modifier.isStatic(member.javaField?.modifiers ?: 0)) {
                    return false
                }
                is KFunction<*> -> if (canAccessJavaMethod(member) && Modifier.isStatic(member.javaMethod?.modifiers ?: 0)) {
                    return false
                }
            }
        }
        // check visibility
        val visibility = determinePropertyVisibility(member)
        when (visibility) {
            Visibility.PUBLIC -> Unit
            Visibility.HIDDEN -> if (!includeHidden) return false
        }
        // check function type
        return if (member is KFunction<*>) {
            when (determineFunctionPropertyType(member)) {
                PropertyType.GETTER -> includeGetters
                PropertyType.WEAK_GETTER -> includeWeakGetters
                PropertyType.FUNCTION -> includeFunctions
                else -> true
            }
        } else {
            true
        }
    }

    @Suppress("SwallowedException")
    private fun canAccessJavaField(property: KProperty<*>): Boolean {
        return try {
            property.javaField != null
        } catch (e: Throwable) {
            false
        }
    }

    @Suppress("SwallowedException")
    private fun canAccessJavaMethod(property: KFunction<*>): Boolean {
        return try {
            property.javaMethod != null
        } catch (e: Throwable) {
            false
        }
    }

    private fun parseProperty(
        member: KProperty<*>,
        resolvedTypeParameters: Map<String, TypeParameterData>,
        typeData: MutableList<BaseTypeData>,
        clazz: KClass<*>
    ): PropertyData {

        val isOptional = clazz.constructors.any { constructor ->
            val ctorParameter = constructor.parameters.find { parameter ->
                parameter.name == member.name && parameter.type == member.returnType
            }
            ctorParameter?.isOptional ?: false
        }
        val type = resolveMemberType(member.returnType, resolvedTypeParameters, typeData)
        return PropertyData(
            name = member.name,
            type = type.id,
            nullable = member.returnType.isMarkedNullable || type.nullable,
            optional = isOptional,
            annotations = parseAnnotations(member).toMutableList(),
            kind = PropertyType.PROPERTY,
            visibility = determinePropertyVisibility(member)
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
            optional = false,
            annotations = parseAnnotations(member).toMutableList(),
            kind = determineFunctionPropertyType(member),
            visibility = determinePropertyVisibility(member),
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
                typeData.find { it.id == providedTypeParameters[classifier.name]?.type }
                    ?: throw IllegalArgumentException("No type parameter provided")
            }
            else -> {
                throw IllegalArgumentException("Unhandled classifier type")
            }
        }
    }

    // ====== SUPERTYPES ==============================================

    private fun parseSupertypes(
        clazz: KClass<*>,
        resolvedTypeParameters: Map<String, TypeParameterData>,
        typeData: MutableList<BaseTypeData>
    ): List<BaseTypeData> {
        return clazz.supertypes
            .filter { it.classifier != Any::class }
            .map { resolveSupertype(it, resolvedTypeParameters, typeData) }
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
                throw IllegalArgumentException("Unhandled classifier type")
            }
        }
    }

    // ====== ENUM =====================================================

    private fun parseEnum(clazz: KClass<*>): List<String> {
        return clazz.java.enumConstants
            ?.map {
                when (enumConstType) {
                    EnumConstType.NAME -> (it as Enum<*>).name
                    EnumConstType.TO_STRING -> it.toString()
                }
            }
            ?: emptyList()
    }

    // ====== ANNOTATION ===============================================

    private fun parseAnnotations(clazz: KClass<*>): List<AnnotationData> {
        return unwrapAnnotations(clazz.annotations).map { parseAnnotation(it) }
    }

    private fun parseAnnotations(property: KProperty<*>): List<AnnotationData> {
        return buildList {
            addAll(unwrapAnnotations(property.javaField?.annotations?.toList() ?: emptyList()).map { parseAnnotation(it) })
            addAll(unwrapAnnotations(property.annotations).map { parseAnnotation(it) })
        }
    }

    private fun parseAnnotations(property: KFunction<*>): List<AnnotationData> {
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


    @Suppress("SwallowedException")
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
                .filter { it.javaField?.let { jf -> !Modifier.isStatic(jf.modifiers) } ?: true }
                .associate { it.name to it.getter.call(annotation) }
                .toMutableMap()
        )
    }

    // ====== CLASS TYPE DECIDER =======================================

    private fun determineClassType(type: KType): TypeCategory {
        return when {
            primitiveTypes.contains(type.classifier as KClass<*>) -> TypeCategory.PRIMITIVE
            isEnum(type) -> TypeCategory.ENUM
            isCollection(type) -> TypeCategory.COLLECTION
            isMap(type) -> TypeCategory.MAP
            else -> TypeCategory.OBJECT
        }
    }


    private fun isEnum(type: KType): Boolean {
        return (type.classifier as KClass<*>).java.enumConstants != null
    }

    private fun isCollection(type: KType): Boolean {
        return if (type.isSubtypeOf(typeOf<Collection<*>>()) || type.isSubtypeOf(typeOf<Array<*>>())) {
            true
        } else {
            (type.classifier as KClass<*>).supertypes.any { isCollection(it) }
        }
    }

    private fun isMap(type: KType): Boolean {
        val clazz = type.classifier as KClass<*>
        return if (clazz.qualifiedName == Map::class.qualifiedName) {
            true
        } else {
            clazz.supertypes.any { isMap(it) }
        }
    }

    // ====== UTILS ====================================================

    private fun determineFunctionPropertyType(function: KFunction<*>): PropertyType {
        if (function.returnType == Unit::class || function.parameters.any { it.name != null }) {
            return PropertyType.FUNCTION
        }
        if (function.name.startsWith("get") || function.name.startsWith("is")) {
            return PropertyType.GETTER
        }
        return PropertyType.WEAK_GETTER
    }

    private fun determinePropertyVisibility(member: KCallable<*>): Visibility {
        return if (member.visibility == KVisibility.PUBLIC) Visibility.PUBLIC else Visibility.HIDDEN
    }

    private fun isCollectionUnique(type: KType): Boolean {
        return type.isSubtypeOf(typeOf<Set<*>>())
    }

    private fun unknownPropertyData(name: String, typeData: MutableList<BaseTypeData>): PropertyData {
        if (typeData.none { it.id == TypeId.wildcard() }) {
            typeData.add(WildcardTypeData())
        }
        return PropertyData(
            name = name,
            type = TypeId.wildcard(),
            nullable = false,
            optional = false,
            visibility = Visibility.PUBLIC,
            kind = PropertyType.PROPERTY,
            annotations = mutableListOf()
        )
    }

    private fun KClass<*>.getSafeSimpleName(): String = this.simpleName ?: this.java.name

    /**
     * Qualified name might be null, e.g. for local classes
     */
    private fun KClass<*>.getSafeQualifiedName(): String = this.qualifiedName ?: this.java.name


    @Suppress("SwallowedException")
    private fun KClass<*>.getMembersSafe(): Collection<KCallable<*>> {
        /*
        Throws error for function-types (for unknown reasons). Catch and ignore this error

        Example:
        class MyClass(
            val myField: (v: Int) -> String
        )
        "Unknown origin of public abstract operator fun invoke(p1: P1):
        R defined in kotlin.Function1[FunctionInvokeDescriptor@162989f2]
        (class kotlin.reflect.jvm.internal.impl.builtins.functions.FunctionInvokeDescriptor)"
        */
        return try {
            this.members
        } catch (e: Throwable) {
            emptyList()
        }
    }


}

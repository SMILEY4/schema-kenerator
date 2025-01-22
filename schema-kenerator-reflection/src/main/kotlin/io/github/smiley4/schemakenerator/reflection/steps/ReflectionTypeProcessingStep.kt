package io.github.smiley4.schemakenerator.reflection.steps

import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.InputType
import io.github.smiley4.schemakenerator.core.data.KTypeInput
import io.github.smiley4.schemakenerator.core.typedata.AnnotationData
import io.github.smiley4.schemakenerator.core.typedata.CollectionData
import io.github.smiley4.schemakenerator.core.typedata.EnumData
import io.github.smiley4.schemakenerator.core.typedata.MapData
import io.github.smiley4.schemakenerator.core.typedata.MemberData
import io.github.smiley4.schemakenerator.core.typedata.MemberKind
import io.github.smiley4.schemakenerator.core.typedata.TypeData
import io.github.smiley4.schemakenerator.core.typedata.TypeId
import io.github.smiley4.schemakenerator.core.typedata.TypeName
import io.github.smiley4.schemakenerator.core.typedata.TypeParameterData
import io.github.smiley4.schemakenerator.core.typedata.Visibility
import io.github.smiley4.schemakenerator.core.typedata.WrappedTypeData
import io.github.smiley4.schemakenerator.core.typedata.WrappedTypeId
import io.github.smiley4.schemakenerator.core.typedata.find
import io.github.smiley4.schemakenerator.core.typedata.findOrThrow
import io.github.smiley4.schemakenerator.core.typedata.matches
import io.github.smiley4.schemakenerator.core.typedata.toWrappedTypeId
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
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.typeOf

/**
 * Processes the given type and extracts information about it using reflection.
 */
class ReflectionTypeProcessingStep(
    /**
     * Whether to include getters as members of classes (see [MemberKind.GETTER]).
     */
    private val includeGetters: Boolean = false,
    /**
     * Whether to include weak getters as members of classes (see [MemberKind.WEAK_GETTER]).
     */
    private val includeWeakGetters: Boolean = false,
    /**
     * Whether to include functions as members of classes (see [MemberKind.FUNCTION]).
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
     * The list of types that are considered "primitive types" and are returned with reduced information.
     */
    private val primitiveTypes: Collection<KClass<*>> = DEFAULT_PRIMITIVE_TYPES,
    /**
     * Use toString for enum values instead of the constant name
     */
    private val enumConstType: EnumConstType = EnumConstType.NAME,
    /**
     * custom processors for given types that overwrite the default behaviour
     */
    private val customProcessors: List<Pair<ReflectionTypeMatcher, ReflectionCustomProcessor>> = emptyList(),
    /**
     * redirect types to other types, i.e. when a type is found as a key, the corresponding type will be processed instead
     */
    private val typeRedirects: Map<KType, KType> = DEFAULT_REDIRECTS
) {

    companion object {

        enum class TypeCategory {
            PRIMITIVE,
            OBJECT,
            ENUM,
            COLLECTION,
            MAP
        }

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


    /**
     * Process the given input type
     */
    fun process(input: InputType): Bundle<TypeData> = process(Bundle(input, emptyList()))


    /**
     * Process the given input type bundle
     */
    fun process(input: Bundle<InputType>): Bundle<TypeData> {

        val knownTypeData = mutableListOf<TypeData>()

        // process supporting inputs
        input.supporting.forEach {
            when (it) {
                is KTypeInput -> process(it.kType, knownTypeData)
                else -> throw IllegalArgumentException("Unsupported input type '$it'.")
            }
        }

        // process main input
        val typeData = input.data.let {
            when (it) {
                is KTypeInput -> process(it.kType, knownTypeData)
                else -> throw IllegalArgumentException("Unsupported input type '$it'.")
            }
        }

        knownTypeData.remove(typeData.typeData)
        return Bundle(
            data = typeData.typeData,
            supporting = knownTypeData
        )
    }


    /**
     * Process the given type and adds new results to the given collection.
     * @param type the type to process
     * @param knownTypeData the already known type data. Adds new results to this collection.
     */
    private fun process(type: KType, knownTypeData: MutableList<TypeData>): WrappedTypeData {
        return if (typeRedirects.containsKey(type)) {
            process(typeRedirects[type]!!, knownTypeData)
        } else if (type.classifier is KClass<*>) {
            parseClass(type, type.classifier as KClass<*>, emptyList(), knownTypeData)
        } else {
            throw IllegalArgumentException("Type is not a class: '${type.classifier}'.")
        }
    }

    // ====== CLASS ====================================================

    /**
     * Parses the given type as class and adds the results to the given collection
     * @param type the input type to parse
     * @param clazz the input class to parse
     * @param knownTypeParameters already parsed type parameter data
     * @param knownTypeData the already known type data. Adds new results to this collection.
     */
    @Suppress("CyclomaticComplexMethod", "LongMethod")
    private fun parseClass(
        type: KType,
        clazz: KClass<*>,
        knownTypeParameters: List<TypeParameterData>,
        knownTypeData: MutableList<TypeData>
    ): WrappedTypeData {

        // check type redirects
        if (typeRedirects.containsKey(type)) {
            return process(typeRedirects[type]!!, knownTypeData)
        }

        // check custom type processors
        val customData = customProcessors
            .firstOrNull { (matcher, _) -> matcher(type, clazz) }
            ?.let { (_, processor) ->
                WrappedTypeData(
                    typeData = processor(),
                    nullable = false
                )
            }
            ?.also { result ->
                knownTypeData.removeIf {
                    it.matches(
                        other = result.typeData,
                        compareId = false,
                        compareIdentifyingName = true,
                        compareDescriptiveName = true,
                        compareTypeParameters = true,
                        compareMembers = false
                    )
                }
                knownTypeData.add(result.typeData)
            }
        if(customData != null) {
            return customData
        }

        // resolve type parameters (i.e. generic types)
        val resolvedTypeParameters = parseTypeParameters(type, clazz, knownTypeParameters, knownTypeData)

        // create basic information for this type
        val id = TypeId.create()
        val identifyingName = clazz.toTypeName()
        val descriptiveName = clazz.toTypeName()

        // check type has already been parsed
        val existing = knownTypeData.find { known -> known.matches(identifyingName, descriptiveName, resolvedTypeParameters) }
        if (existing != null) {
            return WrappedTypeData(
                typeData = existing,
                nullable = type.isMarkedNullable,
            )
        }

        // add placeholder to break out of some infinite recursions
        knownTypeData.add(TypeData.createPlaceholder(id, identifyingName, descriptiveName, resolvedTypeParameters))

        // determine type category, i.e. whether type is primitive, class, enum, collection, map, ...
        val typeCategory = determineTypeCategory(type)

        // collect supertypes
        val supertypes = if (typeCategory == TypeCategory.OBJECT) {
            parseSupertypes(clazz, resolvedTypeParameters, knownTypeData).map { it.id }
        } else {
            emptyList()
        }

        // collect subtypes
        val subtypes = if (typeCategory == TypeCategory.OBJECT) {
            clazz.sealedSubclasses.map { parseClass(it.starProjectedType, it, knownTypeParameters, knownTypeData).typeData.id }
        } else {
            emptyList()
        }

        // collect enum constants
        val enumConstants = if (typeCategory == TypeCategory.ENUM) {
            parseEnumConstants(clazz)
        } else {
            emptyList()
        }

        // check if collection is a set of unique items
        val uniqueCollection = areCollectionItemsUnique(type)

        // collect member information
        val members = if (typeCategory == TypeCategory.OBJECT) {
            parseMembers(clazz, resolvedTypeParameters, knownTypeData)
        } else {
            emptyList()
        }

        // collect annotation data
        val annotations = parseAnnotations(clazz)

        return when (typeCategory) {
            TypeCategory.PRIMITIVE -> TypeData(
                id = id,
                identifyingName = identifyingName,
                descriptiveName = descriptiveName,
                typeParameters = resolvedTypeParameters.toMutableList(),
                annotations = annotations.toMutableList(),
                subtypes = mutableListOf(),
                supertypes = mutableListOf(),
                members = mutableListOf(),
                isInlineValue = false,
                enumData = null,
                collectionData = null,
                mapData = null
            )
            TypeCategory.OBJECT -> TypeData(
                id = id,
                identifyingName = identifyingName,
                descriptiveName = descriptiveName,
                typeParameters = resolvedTypeParameters.toMutableList(),
                annotations = annotations.toMutableList(),
                subtypes = subtypes.toMutableList(),
                supertypes = supertypes.toMutableList(),
                members = members.toMutableList(),
                isInlineValue = clazz.isValue,
                enumData = null,
                collectionData = null,
                mapData = null
            )
            TypeCategory.ENUM -> TypeData(
                id = id,
                identifyingName = identifyingName,
                descriptiveName = descriptiveName,
                typeParameters = resolvedTypeParameters.toMutableList(),
                annotations = annotations.toMutableList(),
                subtypes = mutableListOf(),
                supertypes = mutableListOf(),
                members = mutableListOf(),
                isInlineValue = false,
                enumData = EnumData(
                    constants = enumConstants.toMutableList(),
                ),
                collectionData = null,
                mapData = null
            )
            TypeCategory.COLLECTION -> TypeData(
                id = id,
                identifyingName = identifyingName,
                descriptiveName = descriptiveName,
                typeParameters = resolvedTypeParameters.toMutableList(),
                annotations = annotations.toMutableList(),
                subtypes = mutableListOf(),
                supertypes = mutableListOf(),
                members = mutableListOf(),
                isInlineValue = false,
                enumData = null,
                collectionData = CollectionData(
                    itemType = getCollectionItemType(resolvedTypeParameters),
                    unique = uniqueCollection,
                ),
                mapData = null
            )
            TypeCategory.MAP -> TypeData(
                id = id,
                identifyingName = identifyingName,
                descriptiveName = descriptiveName,
                typeParameters = resolvedTypeParameters.toMutableList(),
                annotations = annotations.toMutableList(),
                subtypes = mutableListOf(),
                supertypes = mutableListOf(),
                members = mutableListOf(),
                isInlineValue = false,
                enumData = null,
                collectionData = null,
                mapData = MapData(
                    keyType = getMapKeyType(resolvedTypeParameters),
                    valueType = getMapValueType(resolvedTypeParameters),
                )
            )
        }.also { result ->
            knownTypeData.removeIf {
                it.matches(
                    other = result,
                    compareId = false,
                    compareIdentifyingName = true,
                    compareDescriptiveName = true,
                    compareTypeParameters = true,
                    compareMembers = false
                )
            }
            knownTypeData.add(result)
        }.let {
            WrappedTypeData(
                typeData = it,
                nullable = type.isMarkedNullable,
            )
        }
    }

    // ====== TYPE PARAMETERS ==========================================

    /**
     * Parses all type parameters of the given type
     * @param type the type to parse the type parameters of
     * @param clazz the class to parse the type parameters of
     * @param knownTypeParameters already parsed type parameter data
     * @param knownTypeData the already known type data. Adds new results to this collection.
     * @return the list of [TypeParameterData]
     */
    private fun parseTypeParameters(
        type: KType,
        clazz: KClass<*>,
        knownTypeParameters: List<TypeParameterData>,
        knownTypeData: MutableList<TypeData>
    ): List<TypeParameterData> {
        val classProvidesNames = clazz.typeParameters.size == type.arguments.size
        return type.arguments.indices.map { index ->
            val name = if (classProvidesNames) clazz.typeParameters[index].name else "T"
            val argType = type.arguments[index]
            val resolvedType = resolveTypeProjection(argType, knownTypeParameters, knownTypeData)
            TypeParameterData(
                name = name,
                type = resolvedType.id,
                nullable = (argType.type?.isMarkedNullable ?: false) || resolvedType.nullable,
            )
        }
    }


    /**
     * Resolves the given [KTypeProjection] to a [TypeData] with nullability information.
     * @param typeProjection the type projection to resolve
     * @param knownTypeParameters already parsed type parameter data
     * @param knownTypeData the already known type data. Adds new results to this collection.
     * @return the resolved [TypeData] with nullability information
     */
    private fun resolveTypeProjection(
        typeProjection: KTypeProjection,
        knownTypeParameters: List<TypeParameterData>,
        knownTypeData: MutableList<TypeData>
    ): WrappedTypeId {
        if (typeProjection.type == null) {
            return WrappedTypeId(
                id = resolveWildcard(knownTypeData).id,
                nullable = false
            )
        }
        return when (val classifier = typeProjection.type?.classifier) {
            is KClass<*> -> parseClass(typeProjection.type!!, classifier, knownTypeParameters, knownTypeData).toWrappedTypeId()
            is KTypeParameter -> {
                val typeParameter = knownTypeParameters.findOrThrow(classifier.name)
                WrappedTypeId(
                    id = knownTypeData.findOrThrow(typeParameter.type).id,
                    nullable = typeParameter.nullable
                )
            }
            else -> throw IllegalArgumentException("Unhandled classifier type: '$classifier'.")
        }
    }


    // ====== MEMBERS ==================================================

    /**
     * Parses members of the given class
     * @param clazz the class with members to parse
     * @param typeParameters the type parameters of the given class
     * @param knownTypeData the already known type data. Adds new results to this collection.
     * @return the list of members
     */
    private fun parseMembers(
        clazz: KClass<*>,
        typeParameters: List<TypeParameterData>,
        knownTypeData: MutableList<TypeData>
    ): List<MemberData> {
        return clazz.getMembersSafe()
            .asSequence()
            .filter { shouldIncludeMember(it) }
            .mapNotNull { member ->
                when (member) {
                    is KProperty<*> -> parseProperty(member, clazz, typeParameters, knownTypeData)
                    is KFunction<*> -> parseFunction(member, typeParameters, knownTypeData)
                    else -> null
                }
            }
            .distinctBy { it.name }
            .toList()
    }


    /**
     * @return whether the given type member should be included in the resulting type data
     */
    private fun shouldIncludeMember(member: KCallable<*>): Boolean {
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
            when (determineFunctionMemberKind(member)) {
                MemberKind.GETTER -> includeGetters
                MemberKind.WEAK_GETTER -> includeWeakGetters
                MemberKind.FUNCTION -> includeFunctions
                else -> true
            }
        } else {
            true
        }
    }


    /**
     * Utility method. Safely checks if we can even access the underlying java field.
     */
    @Suppress("SwallowedException")
    private fun canAccessJavaField(property: KProperty<*>): Boolean {
        return try {
            property.javaField != null
        } catch (e: Throwable) {
            false
        }
    }


    /**
     * Utility method. Safely checks if we can even access the underlying java method.
     */
    @Suppress("SwallowedException")
    private fun canAccessJavaMethod(property: KFunction<*>): Boolean {
        return try {
            property.javaMethod != null
        } catch (e: Throwable) {
            false
        }
    }


    /**
     * Parses the given member property
     * @param member the property
     * @param clazz the parent class
     * @param knownTypeParameters the type parameters of the parent type
     * @param knownTypeData the already known type data. Adds new results to this collection.
     * @return the parsed data
     */
    private fun parseProperty(
        member: KProperty<*>,
        clazz: KClass<*>,
        knownTypeParameters: List<TypeParameterData>,
        knownTypeData: MutableList<TypeData>,
    ): MemberData {

        val isOptional = clazz.constructors.any { constructor ->
            val ctorParameter = constructor.parameters.find { parameter ->
                parameter.name == member.name && parameter.type == member.returnType
            }
            ctorParameter?.isOptional ?: false
        }
        val type = resolveMemberType(member.returnType, knownTypeParameters, knownTypeData)
        return MemberData(
            name = member.name,
            type = type.id,
            nullable = member.returnType.isMarkedNullable || type.nullable,
            optional = isOptional,
            annotations = parseAnnotations(member, clazz).toMutableList(),
            kind = MemberKind.PROPERTY,
            visibility = determinePropertyVisibility(member)
        )
    }


    /**
     * Parses the given member function
     * @param member the function
     * @param knownTypeParameters the type parameters of the parent type
     * @param knownTypeData the already known type data. Adds new results to this collection.
     * @return the parsed data
     */
    private fun parseFunction(
        member: KFunction<*>,
        knownTypeParameters: List<TypeParameterData>,
        knownTypeData: MutableList<TypeData>
    ): MemberData {
        val type = resolveMemberType(member.returnType, knownTypeParameters, knownTypeData)
        return MemberData(
            name = member.name,
            type = type.id,
            nullable = member.returnType.isMarkedNullable || type.nullable,
            optional = false,
            annotations = parseAnnotations(member).toMutableList(),
            kind = determineFunctionMemberKind(member),
            visibility = determinePropertyVisibility(member),
        )
    }


    /**
     * Resolves the type of the given member (as wrapped [TypeData])
     * @param type the type of the member
     * @param knownTypeParameters already parsed type parameter data
     * @param knownTypeData the already known type data. Adds new results to this collection.
     * @return the resolved [TypeData] with nullability information
     */
    private fun resolveMemberType(
        type: KType,
        knownTypeParameters: List<TypeParameterData>,
        knownTypeData: MutableList<TypeData>
    ): WrappedTypeId {
        return when (val classifier = type.classifier) {
            is KClass<*> -> parseClass(type, classifier, knownTypeParameters, knownTypeData).toWrappedTypeId()
            is KTypeParameter -> {
                val typeParameter = knownTypeParameters.findOrThrow(classifier.name)
                WrappedTypeId(
                    id = knownTypeData.findOrThrow(typeParameter.type).id,
                    nullable = typeParameter.nullable
                )
            }
            else -> throw IllegalArgumentException("Unhandled classifier type")
        }
    }

    // ====== SUPERTYPES ==============================================

    /**
     * Parse the supertypes of the given class (e.g. all types the given type extends / inherits from).
     * @param clazz the class to find the supertypes of
     * @param typeParameters the parsed type parameters of the class
     * @param knownTypeData the already known type data. Adds new results to this collection.
     * @return the list of supertypes as [TypeData]
     */
    private fun parseSupertypes(
        clazz: KClass<*>,
        typeParameters: List<TypeParameterData>,
        knownTypeData: MutableList<TypeData>
    ): List<TypeData> {
        return clazz.supertypes
            .filter { it.classifier != Any::class }
            .map { resolveSupertype(it, typeParameters, knownTypeData).typeData }
    }


    /**
     * Resolves the given supertype of a class.
     * @param type the supertype
     * @param knownTypeParameters already parsed type parameter data
     * @param knownTypeData the already known type data. Adds new results to this collection.
     * @return the supertypes as wrapped [TypeData]
     */
    private fun resolveSupertype(
        type: KType,
        knownTypeParameters: List<TypeParameterData>,
        knownTypeData: MutableList<TypeData>
    ): WrappedTypeData {
        return when (val classifier = type.classifier) {
            is KClass<*> -> {
                parseClass(type, classifier, knownTypeParameters, knownTypeData)
            }
            else -> {
                throw IllegalArgumentException("Unhandled classifier type")
            }
        }
    }

    // ====== ENUM =====================================================

    /**
     * Parses the given enum class
     * @return the enum constants in a format specified in [enumConstType] (see [EnumConstType])
     */
    private fun parseEnumConstants(clazz: KClass<*>): List<String> {
        return clazz.java.enumConstants
            ?.map {
                when (enumConstType) {
                    EnumConstType.NAME -> (it as Enum<*>).name
                    EnumConstType.TO_STRING -> it.toString()
                }
            }
            ?: emptyList()
    }

    // ====== COLLECTION ===============================================

    /**
     * @return whether the give collection type may contain only unique items (i.e. whether it is a set)
     */
    private fun areCollectionItemsUnique(type: KType): Boolean {
        return type.isSubtypeOf(typeOf<Set<*>>())
    }


    /**
     * @return the [MemberData] representing the "item" type of a collection
     */
    private fun getCollectionItemType(resolvedTypeParameters: List<TypeParameterData>): MemberData {
        val typeParameter = resolvedTypeParameters.find("E")
            ?: resolvedTypeParameters.find("T")
            ?: resolvedTypeParameters.firstOrNull()
            ?: unknownTypeParameter("item")
        return MemberData(
            name = "item",
            type = typeParameter.type,
            nullable = typeParameter.nullable,
            optional = false,
            visibility = Visibility.PUBLIC,
            kind = MemberKind.PROPERTY,
            annotations = mutableListOf()
        )
    }

    // ====== COLLECTION ===============================================

    /**
     * @return the [MemberData] representing the "key" type of a map
     */
    private fun getMapKeyType(resolvedTypeParameters: List<TypeParameterData>): MemberData {
        val typeParameter = resolvedTypeParameters.find("K") ?: unknownTypeParameter("key")
        return MemberData(
            name = "key",
            type = typeParameter.type,
            nullable = typeParameter.nullable,
            optional = false,
            visibility = Visibility.PUBLIC,
            kind = MemberKind.PROPERTY,
            annotations = mutableListOf()
        )
    }


    /**
     * @return the [MemberData] representing the "value" type of a map
     */
    private fun getMapValueType(resolvedTypeParameters: List<TypeParameterData>): MemberData {
        val typeParameter = resolvedTypeParameters.find("V") ?: unknownTypeParameter("value")
        return MemberData(
            name = "value",
            type = typeParameter.type,
            nullable = typeParameter.nullable,
            optional = false,
            visibility = Visibility.PUBLIC,
            kind = MemberKind.PROPERTY,
            annotations = mutableListOf()
        )
    }

    // ====== ANNOTATION ===============================================

    /**
     * Parses the annotations on the given class.
     * @return the list of parsed annotation data
     */
    private fun parseAnnotations(clazz: KClass<*>): List<AnnotationData> {
        return unwrapAnnotations(clazz.annotations).map { parseAnnotation(it) }
    }


    /**
     * Parses the annotations on the given property (includes annotations on constructor parameters).
     * @return the list of parsed annotation data
     */
    private fun parseAnnotations(property: KProperty<*>, clazz: KClass<*>): List<AnnotationData> {
        return buildList {
            addAll(unwrapAnnotations(property.javaField?.annotations?.toList() ?: emptyList()).map { parseAnnotation(it) })
            addAll(unwrapAnnotations(property.annotations).map { parseAnnotation(it) })
            clazz.primaryConstructor?.parameters
                ?.find { it.name == property.name && it.type == property.returnType }?.annotations
                ?.also { annotations -> addAll(unwrapAnnotations(annotations).map { parseAnnotation(it) }) }
        }
    }


    /**
     * Parses the annotations on the given function.
     * @return the list of parsed annotation data
     */
    private fun parseAnnotations(property: KFunction<*>): List<AnnotationData> {
        return unwrapAnnotations(property.annotations).map { parseAnnotation(it) }
    }


    /**
     * Unwrap any "container" (i.e. "repeatable") annotation in the given list of annotations.
     * "repeatable" annotations are wrapped in a container class and need to be unwrapped
     * @return the flat list of all annotations. Each instance of a "repeatable" annotation is an item in the list.
     */
    private fun unwrapAnnotations(annotations: List<Annotation>): List<Annotation> {
        return annotations.flatMap { annotation ->
            if (isAnnotationContainer(annotation)) {
                unwrapContainer(annotation)
            } else {
                listOf(annotation)
            }
        }
    }


    /**
     * @return whether the given annotation is a "container" annotation containing multiple "repeatable" annotation of the same type.
     */
    private fun isAnnotationContainer(annotation: Annotation): Boolean {
        return annotation.annotationClass.java.declaredAnnotations
            .map { it.annotationClass.qualifiedName }
            .contains("kotlin.jvm.internal.RepeatableContainer")
    }


    /**
     * Unwraps the given container annotation containing multiple "repeatable" annotations of the same type.
     * @return each contained annotation as an own item in the list
     */
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


    /**
     * Parses the given annotation
     * @return the result as [AnnotationData]
     */
    private fun parseAnnotation(annotation: Annotation): AnnotationData {
        return AnnotationData(
            name = annotation.annotationClass.getSafeQualifiedName(),
            values = annotation.annotationClass.members
                .filterIsInstance<KProperty<*>>()
                .filter { it.javaField?.let { jf -> !Modifier.isStatic(jf.modifiers) } ?: true }
                .associate { it.name to it.getter.call(annotation) }
                .toMutableMap()
        )
    }

    // ====== TYPE CATEGORY DECIDER =======================================

    /**
     * Determine the general category the given type falls into (e.g. primitive, enum, collection, ...)
     */
    private fun determineTypeCategory(type: KType): TypeCategory {
        return when {
            primitiveTypes.contains(type.classifier as KClass<*>) -> TypeCategory.PRIMITIVE
            isEnum(type) -> TypeCategory.ENUM
            isCollection(type) -> TypeCategory.COLLECTION
            isMap(type) -> TypeCategory.MAP
            else -> TypeCategory.OBJECT
        }
    }


    /**
     * @return whether the given type is an enum
     */
    private fun isEnum(type: KType): Boolean {
        return (type.classifier as KClass<*>).java.enumConstants != null
    }


    /**
     * @return whether the given type is a collection (i.e. list, array, set, ...)
     */
    private fun isCollection(type: KType): Boolean {
        return if (type.isSubtypeOf(typeOf<Collection<*>>()) || type.isSubtypeOf(typeOf<Array<*>>())) {
            true
        } else {
            (type.classifier as KClass<*>).supertypes.any { isCollection(it) }
        }
    }


    /**
     * @return whether the given type is a map
     */
    private fun isMap(type: KType): Boolean {
        val clazz = type.classifier as KClass<*>
        return if (clazz.qualifiedName == Map::class.qualifiedName) {
            true
        } else {
            clazz.supertypes.any { isMap(it) }
        }
    }

    // ====== UTILS ====================================================

    /**
     * Resolves a wildcard type. Either creating a new one and adding it to the known types of returning an already existing wildcard type.
     */
    private fun resolveWildcard(knownTypeData: MutableList<TypeData>): TypeData {
        val wildcard = TypeData.createWildcard()
        return knownTypeData.find(wildcard.id) ?: wildcard.also { knownTypeData.add(it) }
    }

    /**
     * @return the specific [MemberKind] of the given function, i.e. whether it is a "normal" function, a getter or a "weak" getter
     */
    private fun determineFunctionMemberKind(function: KFunction<*>): MemberKind {
        if (function.returnType == Unit::class || function.parameters.any { it.name != null }) {
            return MemberKind.FUNCTION
        }
        if (function.name.startsWith("get") || function.name.startsWith("is")) {
            return MemberKind.GETTER
        }
        return MemberKind.WEAK_GETTER
    }


    /**
     * @return the [Visibility] of the given member
     */
    private fun determinePropertyVisibility(member: KCallable<*>): Visibility {
        return if (member.visibility == KVisibility.PUBLIC) Visibility.PUBLIC else Visibility.HIDDEN
    }


    /**
     * @return a [TypeParameterData] representing an unknown type parameter (for fallback purposes)
     */
    private fun unknownTypeParameter(name: String) = TypeParameterData(
        name = name,
        type = TypeId.createWildcard(),
        nullable = false,
    )


    /**
     * @return a [TypeName] for this class
     */
    private fun KClass<*>.toTypeName() = TypeName(
        full = this.getSafeQualifiedName(),
        short = this.simpleName ?: this.java.name
    )


    /**
     * Qualified name might be null, e.g. for local classes
     */
    private fun KClass<*>.getSafeQualifiedName(): String = this.qualifiedName ?: this.java.name


    /**
     * Get members of the class (or an empty list on unexpected error)
     */
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

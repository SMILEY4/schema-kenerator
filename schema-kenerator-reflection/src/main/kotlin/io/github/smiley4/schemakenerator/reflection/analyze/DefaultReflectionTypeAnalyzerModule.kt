package io.github.smiley4.schemakenerator.reflection.analyze

import io.github.smiley4.schemakenerator.core.typedata.AnnotationData
import io.github.smiley4.schemakenerator.core.typedata.CollectionData
import io.github.smiley4.schemakenerator.core.typedata.EnumData
import io.github.smiley4.schemakenerator.core.typedata.MapData
import io.github.smiley4.schemakenerator.core.typedata.MemberKind
import io.github.smiley4.schemakenerator.core.typedata.TypeData
import io.github.smiley4.schemakenerator.core.typedata.TypeId
import io.github.smiley4.schemakenerator.core.typedata.TypeName
import io.github.smiley4.schemakenerator.core.typedata.TypeParameterData
import io.github.smiley4.schemakenerator.core.typedata.WrappedTypeData
import io.github.smiley4.schemakenerator.reflection.analyze.provided.AnnotationAnalyzer
import io.github.smiley4.schemakenerator.reflection.analyze.provided.CollectionAnalyzer
import io.github.smiley4.schemakenerator.reflection.analyze.provided.EnumAnalyzer
import io.github.smiley4.schemakenerator.reflection.analyze.provided.MemberAnalyzer
import io.github.smiley4.schemakenerator.reflection.analyze.provided.SubtypeAnalyzer
import io.github.smiley4.schemakenerator.reflection.analyze.provided.SupertypeAnalyzer
import io.github.smiley4.schemakenerator.reflection.analyze.provided.TypeCategoryAnalyzer
import io.github.smiley4.schemakenerator.reflection.analyze.provided.TypeCategoryAnalyzer.TypeCategory
import io.github.smiley4.schemakenerator.reflection.analyze.provided.TypeParameterAnalyzer
import io.github.smiley4.schemakenerator.reflection.data.EnumConstType
import io.github.smiley4.schemakenerator.reflection.data.MinimalTypeData
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import kotlin.reflect.KType


class DefaultReflectionTypeAnalyzerModule(
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
     * The format / [EnumConstType] for enum options
     */
    private val enumConstType: EnumConstType = EnumConstType.NAME,
    /**
     * The list of types that are considered "primitive types"
     */
    private val primitiveTypes: Collection<KClass<*>>
) : ReflectionTypeAnalyzerModule {

    private val typeParameterAnalyzer = TypeParameterAnalyzer()
    private val typeCategoryAnalyzer = TypeCategoryAnalyzer()
    private val supertypeAnalyzer = SupertypeAnalyzer()
    private val subtypeAnalyzer = SubtypeAnalyzer()
    private val enumAnalyzer = EnumAnalyzer()
    private val annotationAnalyzer = AnnotationAnalyzer()
    private val memberAnalyzer = MemberAnalyzer()
    private val collectionAnalyzer = CollectionAnalyzer()


    override fun applies(type: KType, clazz: KClass<*>) = true


    override fun preAnalyze(context: ReflectionTypeAnalyzerModule.Context): MinimalTypeData {
        return MinimalTypeData(
            identifyingName = context.clazz.toTypeName(),
            descriptiveName = context.clazz.toTypeName(),
            typeParameters = typeParameterAnalyzer.analyzeTypeParameters(context)
        )
    }


    override fun analyze(context: ReflectionTypeAnalyzerModule.Context, minimalTypeData: MinimalTypeData): WrappedTypeData {

        // determine type category, i.e. whether type is primitive, class, enum, collection, map, ...
        val typeCategory = typeCategoryAnalyzer.determineTypeCategory(context.type, primitiveTypes)

        // collect supertypes
        val supertypes = if (typeCategory == TypeCategory.OBJECT) {
            supertypeAnalyzer.analyzeSupertypes(context).map { it.id }
        } else {
            emptyList()
        }

        // collect subtypes
        val subtypes = if (typeCategory == TypeCategory.OBJECT) {
            subtypeAnalyzer.analyzeSubtypes(context)
        } else {
            emptyList()
        }

        // collect enum constants
        val enumConstants = if (typeCategory == TypeCategory.ENUM) {
            enumAnalyzer.analyzeEnumConstants(context.clazz, enumConstType)
        } else {
            emptyList()
        }

        // check if collection is a set of unique items
        val uniqueCollection = collectionAnalyzer.areCollectionItemsUnique(context.type)

        // collect member information
        val members = if (typeCategory == TypeCategory.OBJECT) {
            memberAnalyzer.analyzeMembers(
                context,
                includeGetters,
                includeWeakGetters,
                includeFunctions,
                includeHidden,
                includeStatic,
                this::analyzeMemberAnnotations
            )
        } else {
            emptyList()
        }

        // collect annotation data
        val annotations = annotationAnalyzer.analyzeAnnotations(context.clazz)

        return when (typeCategory) {
            TypeCategory.PRIMITIVE -> TypeData(
                id = context.id,
                identifyingName = minimalTypeData.identifyingName,
                descriptiveName = minimalTypeData.descriptiveName,
                typeParameters = minimalTypeData.typeParameters.toMutableList(),
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
                id = context.id,
                identifyingName = minimalTypeData.identifyingName,
                descriptiveName = minimalTypeData.descriptiveName,
                typeParameters = minimalTypeData.typeParameters.toMutableList(),
                annotations = annotations.toMutableList(),
                subtypes = subtypes.toMutableList(),
                supertypes = supertypes.toMutableList(),
                members = members.toMutableList(),
                isInlineValue = context.clazz.isValue,
                enumData = null,
                collectionData = null,
                mapData = null
            )
            TypeCategory.ENUM -> TypeData(
                id = context.id,
                identifyingName = minimalTypeData.identifyingName,
                descriptiveName = minimalTypeData.descriptiveName,
                typeParameters = minimalTypeData.typeParameters.toMutableList(),
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
                id = context.id,
                identifyingName = minimalTypeData.identifyingName,
                descriptiveName = minimalTypeData.descriptiveName,
                typeParameters = minimalTypeData.typeParameters.toMutableList(),
                annotations = annotations.toMutableList(),
                subtypes = mutableListOf(),
                supertypes = mutableListOf(),
                members = mutableListOf(),
                isInlineValue = false,
                enumData = null,
                collectionData = CollectionData(
                    itemType = collectionAnalyzer.getCollectionItemType(minimalTypeData.typeParameters, this::unknownTypeParameter),
                    unique = uniqueCollection,
                ),
                mapData = null
            )
            TypeCategory.MAP -> TypeData(
                id = context.id,
                identifyingName = minimalTypeData.identifyingName,
                descriptiveName = minimalTypeData.descriptiveName,
                typeParameters = minimalTypeData.typeParameters.toMutableList(),
                annotations = annotations.toMutableList(),
                subtypes = mutableListOf(),
                supertypes = mutableListOf(),
                members = mutableListOf(),
                isInlineValue = false,
                enumData = null,
                collectionData = null,
                mapData = MapData(
                    keyType = collectionAnalyzer.getMapKeyType(minimalTypeData.typeParameters, this::unknownTypeParameter),
                    valueType = collectionAnalyzer.getMapValueType(minimalTypeData.typeParameters, this::unknownTypeParameter),
                )
            )
        }.let {
            WrappedTypeData(
                typeData = it,
                nullable = context.type.isMarkedNullable
            )
        }
    }

    private fun analyzeMemberAnnotations(member: KCallable<*>, ctx: ReflectionTypeAnalyzerModule.Context): List<AnnotationData> {
        return when (member) {
            is KProperty<*> -> annotationAnalyzer.analyzeAnnotations(member, ctx.clazz)
            is KFunction<*> -> annotationAnalyzer.analyzeAnnotations(member)
            else -> emptyList()
        }
    }


    /**
     * @return a [TypeName] for this class
     */
    private fun KClass<*>.toTypeName() = TypeName(
        full = this.qualifiedName ?: this.java.name,
        short = this.simpleName ?: this.java.name
    )


    /**
     * @return a [TypeParameterData] representing an unknown type parameter (for fallback purposes)
     */
    private fun unknownTypeParameter(name: String) = TypeParameterData(
        name = name,
        type = TypeId.createWildcard(),
        nullable = false,
    )

}

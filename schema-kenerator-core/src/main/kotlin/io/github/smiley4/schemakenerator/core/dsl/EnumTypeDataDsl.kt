package io.github.smiley4.schemakenerator.core.dsl

import io.github.smiley4.schemakenerator.core.data.EnumTypeData
import io.github.smiley4.schemakenerator.core.data.ObjectTypeData
import io.github.smiley4.schemakenerator.core.data.PropertyType
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.core.data.TypeParameterData
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf


fun enumType(id: TypeId, block: EnumTypeDataDsl.() -> Unit): EnumTypeData {
    return EnumTypeDataDsl(id).apply(block).build()
}

class EnumTypeDataDsl(val id: TypeId) {

    private val data = EnumTypeData(
        id = id,
        simpleName = "",
        qualifiedName = "",
        enumConstants = mutableListOf()
    )


    /**
     * Set both the full/qualified and simple name of the type
     */
    fun name(name: String) {
        data.simpleName = name
        data.qualifiedName = name
    }


    /**
     * Set both the full/qualified and simple name of the type to the name of the given type
     */
    fun name(type: KType) {
        if (type.classifier is KClass<*>) {
            (type.classifier as KClass<*>).also { klass ->
                data.simpleName = klass.simpleName ?: data.simpleName
                data.qualifiedName = klass.qualifiedName ?: data.qualifiedName
            }
        }
    }


    /**
     * Set both the full/qualified and simple name of the type to the name of the given type
     */
    inline fun <reified T> name() = name(typeOf<T>())


    /**
     * Set the simple name of the type
     */
    fun simpleName(name: String) {
        data.simpleName = name
    }


    /**
     * Set the full/qualified name of the type
     */
    fun setQualifiedName(name: String) {
        data.qualifiedName = name
    }


    /**
     * Add a new type parameter with the given name and type
     */
    fun typeParameter(name: String, type: TypeId, required: Boolean = true) {
        data.typeParameters[name] = TypeParameterData(
            name = name,
            type = type,
            nullable = !required,
        )
    }


    /**
     * Add a new annotation with the given name
     */
    fun annotation(name: String = "", block: AnnotationDataDsl.() -> Unit) {
        data.annotations.add(AnnotationDataDsl()
            .also { it.name(name) }
            .apply(block).build())
    }


    /**
     * Add the given type as a subtype
     */
    fun subtype(type: TypeId) {
        data.subtypes.add(type)
    }


    /**
     * Add the given type as a supertype
     */
    fun supertype(type: TypeId) {
        data.supertypes.add(type)
    }


    /**
     * Adds a new member (e.g. property, getter, function, ...) to the object
     */
    fun member(name: String = "", type: TypeId = TypeId.wildcard(), block: MemberDataDsl.() -> Unit) {
        data.members.add(MemberDataDsl()
            .also { it.name(name); it.type(type) }
            .apply(block).build())
    }


    /**
     * Adds a new member of type [PropertyType.PROPERTY] to the object
     */
    fun property(name: String = "", type: TypeId = TypeId.wildcard(), block: MemberDataDsl.() -> Unit) = member(name, type) {
        kind(PropertyType.PROPERTY)
        block(this)
    }


    /**
     * Adds a new member of type [PropertyType.GETTER] to the object
     */
    fun getter(name: String = "", type: TypeId = TypeId.wildcard(), block: MemberDataDsl.() -> Unit) = member(name, type) {
        kind(PropertyType.GETTER)
        block(this)
    }


    /**
     * Adds a new member of type [PropertyType.PROPERTY] to the object
     */
    fun weakGetter(name: String = "", type: TypeId = TypeId.wildcard(), block: MemberDataDsl.() -> Unit) = member(name, type) {
        kind(PropertyType.WEAK_GETTER)
        block(this)
    }


    /**
     * Adds a new member of type [PropertyType.PROPERTY] to the object
     */
    fun function(name: String = "", type: TypeId = TypeId.wildcard(), block: MemberDataDsl.() -> Unit) = member(name, type) {
        kind(PropertyType.FUNCTION)
        block(this)
    }


    /**
     * Adds the values as possible enum constants
     */
    fun enumConstant(vararg constants: String) {
        data.enumConstants.addAll(constants)
    }

    fun build() = data

}
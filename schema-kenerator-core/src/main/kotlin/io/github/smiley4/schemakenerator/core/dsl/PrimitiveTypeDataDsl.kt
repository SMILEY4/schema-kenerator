package io.github.smiley4.schemakenerator.core.dsl

import io.github.smiley4.schemakenerator.core.data.PrimitiveTypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.core.data.TypeParameterData
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

fun primitiveType(id: TypeId, block: PrimitiveTypeDataDsl.() -> Unit): PrimitiveTypeData {
    return PrimitiveTypeDataDsl(id).apply(block).build()
}

class PrimitiveTypeDataDsl(val id: TypeId) {

    private val data = PrimitiveTypeData(
        id = id,
        simpleName = "",
        qualifiedName = "",
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
        data.annotations.add(AnnotationDataDsl().also { it.name(name) }.apply(block).build())
    }


    fun build() = data

}
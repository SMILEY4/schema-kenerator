package io.github.smiley4.schemakenerator.core.dsl

import io.github.smiley4.schemakenerator.core.data.PropertyData
import io.github.smiley4.schemakenerator.core.data.PropertyType
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.core.data.Visibility

class MemberDataDsl {

    private val data = PropertyData(
        name = "",
        type = TypeId.wildcard(),
        nullable = false,
        visibility = Visibility.PUBLIC,
        kind = PropertyType.PROPERTY,
        annotations = mutableListOf()
    )


    /**
     * Sets the name of the member
     */
    fun name(name: String) {
        data.name = name
    }

    /**
     * Sets the (return) type of the member
     */
    fun type(type: TypeId) {
        data.type = type
    }

    /**
     * Whether the member can be/return null
     */
    fun nullable(nullable: Boolean) {
        data.nullable = nullable
    }

    /**
     * Sets the visibility of the member
     */
    fun visibility(visibility: Visibility) {
        data.visibility = visibility
    }

    /**
     * Sets the type of member
     */
    fun kind(kind: PropertyType) {
        data.kind = kind
    }

    /**
     * Add a new annotation with the given name to this member
     */
    fun annotation(name: String = "", block: AnnotationDataDsl.() -> Unit) {
        data.annotations.add(AnnotationDataDsl().also { it.name(name) }.apply(block).build())
    }

    fun build() = data

}
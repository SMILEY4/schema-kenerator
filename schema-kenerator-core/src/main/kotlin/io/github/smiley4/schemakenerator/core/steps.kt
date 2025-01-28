package io.github.smiley4.schemakenerator.core

import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.TypeData

/**
 * See [AddMissingSubtypeSupertypeRelations]
 */
fun Bundle<TypeData>.addMissingSupertypeSubtypeRelations(): Bundle<TypeData> {
    return AddMissingSubtypeSupertypeRelations().process(this)
}


/**
 * See [HandleNameAnnotationStep]
 */
fun Bundle<TypeData>.handleNameAnnotation(): Bundle<TypeData> {
    return HandleNameAnnotationStep().process(this)
}


/**
 * See [RenameMembersStep]
 */
fun Bundle<TypeData>.renameMembers(rename: (name: String) -> String): Bundle<TypeData> {
    return RenameMembersStep(rename).process(this)
}


/**
 * See [GettersToPropertiesStep]
 */
fun Bundle<TypeData>.gettersToProperties(): Bundle<TypeData> {
    return GettersToPropertiesStep().process(this)
}


/**
 * See [AddDiscriminatorStep]
 */
fun Bundle<TypeData>.addDiscriminatorProperty(discriminatorPropertyName: String = "type"): Bundle<TypeData> {
    return AddDiscriminatorStep(discriminatorPropertyName).process(this)
}

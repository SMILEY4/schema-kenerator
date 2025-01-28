package io.github.smiley4.schemakenerator.core

import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.steps.AddDiscriminatorStep
import io.github.smiley4.schemakenerator.core.steps.AddMissingSubtypeSupertypeRelations
import io.github.smiley4.schemakenerator.core.steps.GettersToPropertiesStep
import io.github.smiley4.schemakenerator.core.steps.RenameMembersStep
import io.github.smiley4.schemakenerator.core.steps.RenameTypesStep
import io.github.smiley4.schemakenerator.core.typedata.TypeData

/**
 * See [AddMissingSubtypeSupertypeRelations]
 */
fun Bundle<TypeData>.addMissingSupertypeSubtypeRelations(): Bundle<TypeData> {
    return AddMissingSubtypeSupertypeRelations().process(this)
}


/**
 * See [RenameTypesStep]
 */
fun Bundle<TypeData>.handleNameAnnotation(): Bundle<TypeData> {
    return RenameTypesStep().process(this)
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

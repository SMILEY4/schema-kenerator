package io.github.smiley4.schemakenerator.swagger

import io.swagger.v3.oas.models.media.Schema


data class SwaggerSchema(
    val schema: Schema<*>,
    val definitions: Map<String, Schema<*>> = mutableMapOf()
)

fun SwaggerSchema.getByRef(ref: String): Schema<*>? {
    val definitionName = ref.replace("#/definitions/", "")
    return this.definitions[definitionName]
}

fun SwaggerSchema.getByRefOrThrow(ref: String) = this.getByRef(ref) ?: throw Exception("Could not find definition for ref $ref")
package io.github.smiley4.schemakenerator.jsonschema.steps

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.jsonschema.data.RefType
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonArray
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonNode
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonObject
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonTextValue
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonValue
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.array
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.obj

object JsonSchemaCompileUtils {

    private const val MAX_RESOLVE_REFS_DEPTH = 64;

    fun resolveReferences(node: JsonNode, depth: Int = 0, resolver: (refObj: JsonObject) -> JsonNode): JsonNode {
        if (depth > MAX_RESOLVE_REFS_DEPTH) {
            return obj { }
        }
        if (node is JsonObject && node.properties.containsKey("\$ref")) {
            val resolved = resolver(node)
            return if (resolved == node) {
                resolved
            } else {
                resolveReferences(resolved, depth + 1, resolver)
            }
        } else {
            return when (node) {
                is JsonArray -> array {
                    node.items
                        .map { resolveReferences(it, depth + 1, resolver) }
                        .forEach { item(it) }
                }
                is JsonObject -> {
                    obj {
                        node.properties.forEach { (key, value) ->
                            key to resolveReferences(value, depth + 1, resolver)
                        }
                    }
                }
                is JsonValue<*> -> node
            }
        }
    }

    fun shouldReference(json: JsonNode): Boolean {
        val complexProperties = setOf(
            "required",
            "properties"
        )
        return if (json is JsonObject) {
            val isComplexObject = getTextProperty(json, "type") == "object"
                    && json.properties.keys.any { complexProperties.contains(it) }
                    && !existsProperty(json, "additionalProperties")
            isComplexObject || existsProperty(json, "enum") || existsProperty(json, "anyOf")
        } else {
            false
        }

    }

    fun getTextProperty(node: JsonNode, key: String): String? {
        if (node is JsonObject) {
            val type = node.properties[key]
            if (type is JsonTextValue) {
                return type.value
            }
        }
        return null
    }

    fun existsProperty(node: JsonNode, key: String): Boolean {
        if (node is JsonObject) {
            return node.properties[key] != null
        }
        return false
    }


    fun getRefPath(pathType: RefType, typeData: BaseTypeData, typeDataMap: Map<TypeId, BaseTypeData>): String {
        return when (pathType) {
            RefType.FULL -> typeData.qualifiedName
            RefType.SIMPLE -> typeData.simpleName
        }.let {
            if (typeData.typeParameters.isNotEmpty()) {
                val paramString = typeData.typeParameters
                    .map { (_, param) -> getRefPath(pathType, typeDataMap[param.type]!!, typeDataMap) }
                    .joinToString(",")
                "$it<$paramString>"
            } else {
                it
            }
        }.let {
            it + (typeData.id.additionalId?.let { a -> "#$a" } ?: "")
        }
    }

}

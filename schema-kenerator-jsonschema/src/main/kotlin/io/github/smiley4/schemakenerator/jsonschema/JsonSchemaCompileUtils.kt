package io.github.smiley4.schemakenerator.jsonschema

import io.github.smiley4.schemakenerator.core.data.TypeData
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

    fun shouldReference(schema: JsonNode, typeData: TypeData): Boolean {
        return if (schema is JsonObject) {

            val isObject = (getTypes(schema).contains("object") || schema.properties.contains("properties"))
                    && !typeData.isMap
                    && typeData.identifyingName.full != Any::class.qualifiedName!!
                    && typeData.identifyingName.full != "*"

            val isEnum = schema.properties.contains("enum")

            val isAnyOfObject = schema.properties.contains("anyOf") && schema.getArray("anyOf").items.all {
                it is JsonObject && it.properties.containsKey("${'$'}ref")
            }

            val isOneOfObject = schema.properties.contains("oneOf") && schema.getArray("oneOf").items.all {
                it is JsonObject && it.properties.containsKey("${'$'}ref")
            }

            return isObject || isEnum || isAnyOfObject || isOneOfObject
        } else {
            false
        }
    }

    private fun getTypes(schema: JsonObject): Set<String> {
        return schema.properties["type"]
            ?.let {
                when (it) {
                    is JsonArray -> it.items.filterIsInstance<JsonTextValue>().mapNotNull { it.value }.toSet()
                    is JsonTextValue -> setOf(it.value)
                    else -> emptySet()
                }
            }
            ?: emptySet()
    }

}

package io.github.smiley4.schemakenerator.jsonschema

import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonArray
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonNode
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonObject
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

    fun shouldReference(schema: JsonNode): Boolean {
        return if (schema is JsonObject) {
            schema.properties.contains("properties")
                    || schema.properties.contains("enum")
                    || schema.properties.contains("anyOf")
                    || schema.properties.contains("oneOf")
        } else {
            false
        }
    }


}

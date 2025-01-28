@file:Suppress("ClassName")

package io.github.smiley4.schemakenerator.examples

import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.typedata.TypeData
import io.github.smiley4.schemakenerator.core.typedata.find
import io.github.smiley4.schemakenerator.core.typedata.findTypeParameter
import io.github.smiley4.schemakenerator.jsonschema.compileInlining
import io.github.smiley4.schemakenerator.jsonschema.data.TitleType
import io.github.smiley4.schemakenerator.jsonschema.generateJsonSchema
import io.github.smiley4.schemakenerator.jsonschema.withTitle
import io.github.smiley4.schemakenerator.reflection.analyseTypeUsingReflection
import io.kotest.core.spec.style.FreeSpec
import kotlin.reflect.typeOf

class E01_General_Concept : FreeSpec({

    "sequence steps" {

        // The generation of schemas from types/classes is done in a sequence of independent steps.
        // Each step is usually either extracting data, adding additional data or transforming the existing information.
        // Steps can be added, removed or combined in different ways to produce a desired result/schema.

        val jsonSchema = typeOf<List<String>>()
            .analyseTypeUsingReflection()   // step 1: extract information from the given type
            .generateJsonSchema()           // step 2: generate independent json-schemas for each involved type extracted in the previous step
            .withTitle(TitleType.SIMPLE)    // step 3: add the "title" property to the generated schemas with the "simple" name of the types
            .compileInlining()              // step 4: combine the independent json schemas into a single schema by inlining all referenced schemas

        println(jsonSchema.json.prettyPrint())
        // {
        //     "title": "List<String>",
        //     "type": "array",
        //     "items": {
        //         "title": "String",
        //         "type": "string"
        //     }
        // }

    }

    "bundles with stored data" {

        val result: Bundle<TypeData> = typeOf<Map<String, Int>>().analyseTypeUsingReflection()

        // "Bundle<...>" is generally the class storing current information about the input and output of a step.
        // It contains two properties:
        //   - "data":          information/schema of the root type, i.e. the main type to extract data from and generate schemas for.
        //   - "supporting":    list of additional types/schemas that are referenced by the root type or any other type

        // Example: output "bundle" after "analyseTypeUsingReflection" with "Map<String, Int>" as input.
        println(result.data.descriptiveName.short)                  // -> "Map"
        println(result.supporting.map { it.descriptiveName.short }) // -> "[String, Int]"

    }

    "references via type-ids" {

        // when extracting data from classes, nested properties are flattened (stored in Bundle#supporting) and referenced via "TypeIds".
        val result = typeOf<List<String>>().analyseTypeUsingReflection()

        val itemTypeParameter = result.data.findTypeParameter("E")!!
        println(itemTypeParameter.type) // -> id for type "kotlin.String", actual data for type "String" is stored as element in "result.supporting"

        val stringTypeData = result.supporting.find(itemTypeParameter.type)!! // find the actual data referenced by the type parameter
        println(stringTypeData.descriptiveName.full) // "kotlin.String"

    }

})

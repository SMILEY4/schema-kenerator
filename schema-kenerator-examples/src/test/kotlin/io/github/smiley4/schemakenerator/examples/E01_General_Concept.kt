@file:Suppress("ClassName")

package io.github.smiley4.schemakenerator.examples

import io.github.smiley4.schemakenerator.core.CoreSteps.initial
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaSteps.compileInlining
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaSteps.generateJsonSchema
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaSteps.withTitle
import io.github.smiley4.schemakenerator.jsonschema.data.TitleType
import io.github.smiley4.schemakenerator.reflection.ReflectionSteps.analyzeTypeUsingReflection
import io.kotest.core.spec.style.FreeSpec

class E01_General_Concept : FreeSpec({

    "sequence steps" {

        // The generation of schemas from types/classes is done in a sequence of independent steps.
        // Each step is usually either extracting data, adding additional data or transforming the existing information.
        // Steps can be added, removed or combined in different ways to produce a desired result/schema.

        val jsonSchema = initial<List<String>>()
            .analyzeTypeUsingReflection()   // step 1: extract information from the given type
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

    "references via type-ids" {

        fun TypeData.findTypeParameter(name: String) = typeParameters.find { it.name == name }

        // when extracting data from classes, nested properties are flattened (stored in Bundle#supporting) and referenced via "TypeIds".
        val result = initial<List<String>>().analyzeTypeUsingReflection()

        val itemTypeParameter = result.root.findTypeParameter("E")!!
        println(itemTypeParameter.type) // -> id for type "kotlin.String", actual data for type "String" is stored as element in "result.supporting"

        val stringTypeData = result[itemTypeParameter.type]!! // find the actual data referenced by the type parameter
        println(stringTypeData.descriptiveName.full) // "kotlin.String"
    }

})

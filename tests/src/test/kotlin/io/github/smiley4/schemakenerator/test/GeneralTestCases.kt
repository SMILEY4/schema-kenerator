package io.github.smiley4.schemakenerator.test

import io.github.smiley4.schemakenerator.core.CoreSteps.addMissingSupertypeSubtypeRelations
import io.github.smiley4.schemakenerator.core.CoreSteps.initial
import io.github.smiley4.schemakenerator.core.data.InitialTypeData
import io.github.smiley4.schemakenerator.core.data.TypeDataGroup
import io.github.smiley4.schemakenerator.jackson.JacksonSteps.collectJacksonSubTypes
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaSteps.compileInlining
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaSteps.compileReferencing
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaSteps.generateJsonSchema
import io.github.smiley4.schemakenerator.jsonschema.data.CompiledJsonSchemaData
import io.github.smiley4.schemakenerator.jsonschema.data.IntermediateJsonSchemaData
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonObject
import io.github.smiley4.schemakenerator.reflection.ReflectionSteps.analyzeTypeUsingReflection
import io.github.smiley4.schemakenerator.reflection.ReflectionSteps.collectSubTypes
import io.github.smiley4.schemakenerator.serialization.SerializationSteps.analyzeTypeUsingKotlinxSerialization
import io.github.smiley4.schemakenerator.swagger.SwaggerSteps.compileInlining
import io.github.smiley4.schemakenerator.swagger.SwaggerSteps.compileReferencing
import io.github.smiley4.schemakenerator.swagger.SwaggerSteps.generateSwaggerSchema
import io.github.smiley4.schemakenerator.swagger.data.CompiledSwaggerSchemaData
import io.github.smiley4.schemakenerator.swagger.data.IntermediateSwaggerSchemaData
import io.github.smiley4.schemakenerator.test.cases.BasicTestCases
import io.github.smiley4.schemakenerator.test.cases.CoreAnnotationTestCases
import io.github.smiley4.schemakenerator.test.cases.CustomTypeProcessingTestCases
import io.github.smiley4.schemakenerator.test.cases.InheritanceTestCases
import io.github.smiley4.schemakenerator.test.cases.JacksonAnnotationTestCases
import io.github.smiley4.schemakenerator.test.cases.JavaxJackartaAnnotationTestCases
import io.github.smiley4.schemakenerator.test.cases.MiscTestCases
import io.github.smiley4.schemakenerator.test.cases.RedirectTypeTestCases
import io.github.smiley4.schemakenerator.test.cases.SwaggerAnnotationTestCases
import io.github.smiley4.schemakenerator.test.cases.TestCase
import io.github.smiley4.schemakenerator.test.cases.TitleTestCases
import io.kotest.assertions.json.ArrayOrder
import io.kotest.assertions.json.FieldComparison
import io.kotest.assertions.json.NumberFormat
import io.kotest.assertions.json.PropertyOrder
import io.kotest.assertions.json.TypeCoercion
import io.kotest.assertions.json.shouldEqualJson
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.scopes.FunSpecContainerScope
import io.kotest.core.spec.style.scopes.FunSpecRootScope
import io.swagger.v3.core.util.Json31
import io.swagger.v3.oas.models.Components
import kotlin.reflect.KType

class GeneralTestCases : FunSpec({

    runSuite(
        listOf(
            BasicTestCases.any,
            BasicTestCases.uByte,
            BasicTestCases.int,
            BasicTestCases.float,
            BasicTestCases.boolean,
            BasicTestCases.string,
            BasicTestCases.enum,
            BasicTestCases.listOfStrings,
            BasicTestCases.mapStringToInt,
            BasicTestCases.simpleFields,
            BasicTestCases.optionalParametersAsRequired,
            BasicTestCases.optionalParametersAsNonRequired,
            BasicTestCases.collections,
            BasicTestCases.valueClass,
            BasicTestCases.nullabilityOfParametersOfSameTypeFirst,
            BasicTestCases.nullabilityOfParametersOfSameTypeSecond,
            BasicTestCases.genericField,
            BasicTestCases.genericNullableField,
            BasicTestCases.genericComplexField,
            BasicTestCases.genericWildcardField,
            BasicTestCases.genericDeepField,
            BasicTestCases.differentGenericsForSameWrapper,
            BasicTestCases.nestedClass,
            BasicTestCases.nullableSelfReference,

            TitleTestCases.simple,
            TitleTestCases.full,
            TitleTestCases.openApiSimple,
            TitleTestCases.openApiFull,

            InheritanceTestCases.subtypeWithSupertype,
            InheritanceTestCases.supertypeWithSubtypes,
            InheritanceTestCases.basicDiscriminator,
            InheritanceTestCases.jacksonUseClassDiscriminator,
            InheritanceTestCases.jacksonUseNameDiscriminator,
            InheritanceTestCases.jacksonUseSimpleNameDiscriminator,
            InheritanceTestCases.kotlinxSerializationDiscriminator,
            InheritanceTestCases.collectSubtypesCore,
            InheritanceTestCases.collectSubtypesJackson,

            CoreAnnotationTestCases.basics,
            CoreAnnotationTestCases.annotatedValueClass,

            JacksonAnnotationTestCases.jsonIgnore,
            JacksonAnnotationTestCases.jsonIgnoreType,
            JacksonAnnotationTestCases.jsonIgnoreProperties,
            JacksonAnnotationTestCases.jsonProperty,
            JacksonAnnotationTestCases.jsonPropertyDescription,

            JavaxJackartaAnnotationTestCases.validationsJavax,
            JavaxJackartaAnnotationTestCases.validationsJackarta,
            JavaxJackartaAnnotationTestCases.notNullJavax,
            JavaxJackartaAnnotationTestCases.notNullJackarta,
            JavaxJackartaAnnotationTestCases.notEmptyJavax,
            JavaxJackartaAnnotationTestCases.notEmptyJackarta,
            JavaxJackartaAnnotationTestCases.notBlankJavax,
            JavaxJackartaAnnotationTestCases.notBlankJackarta,
            JavaxJackartaAnnotationTestCases.allRequiredAnnotationsJavax,
            JavaxJackartaAnnotationTestCases.allRequiredAnnotationsJackarta,

            SwaggerAnnotationTestCases.basics,
            SwaggerAnnotationTestCases.partiallySpecified,
            SwaggerAnnotationTestCases.hiddenFields,

            CustomTypeProcessingTestCases.localDateTimeWithoutConfig,
            CustomTypeProcessingTestCases.localDateTimeWithCustomProcessor,

            RedirectTypeTestCases.matchFromKeepTo,
            RedirectTypeTestCases.matchFromReplaceTo,
            RedirectTypeTestCases.ignoreFromKeepTo,
            RedirectTypeTestCases.ignoreFromReplaceTo,
            RedirectTypeTestCases.chain,
            RedirectTypeTestCases.optional,

            MiscTestCases.requiredAnnotationAllPropsNullableOrOptional,
            MiscTestCases.includeAnnotationsFromConstructorParameters,
            MiscTestCases.renamePropertiesAddPrefix,
            MiscTestCases.renamePropertiesSnakeCase,
            MiscTestCases.renamePropertiesKebabCase,
            MiscTestCases.customizePropertyWithSharedType,
            MiscTestCases.nullablePropertyOfSealedClass,
            MiscTestCases.mergePropertyAttributesIntoType,
            MiscTestCases.kotlinxContextualFromConfig,
            MiscTestCases.kotlinxContextualFromAnnotationWith,
            MiscTestCases.kotlinxMultipleContextualAnnotationsSameType,
            MiscTestCases.overwritingTypeWithMoreSpecificType,
            MiscTestCases.collectCorrectSubtypesWithTypeParametersInvolved,
            MiscTestCases.specialFloatingPointValuesDontAllow,
            MiscTestCases.specialFloatingPointValuesAllow,
            MiscTestCases.mapsWithComplexKeysAsArraysDisabled,
            MiscTestCases.mapsWithComplexKeysAsArraysEnabled,
            MiscTestCases.descriptionOnPropertyAndType
        )
    )

}) {

    companion object {

        private fun FunSpecRootScope.runSuite(cases: List<TestCase>) {
            cases.map { it.group }.distinct().forEach { groupName ->
                runGroup(cases.filter { it.group == groupName })
            }
        }

        private fun FunSpecRootScope.runGroup(cases: List<TestCase>) {
            context(cases.first().group) {
                cases.forEach { case ->
                    runCase(case)
                }
            }
        }

        private suspend fun FunSpecContainerScope.runCase(case: TestCase) {
            context(case.name) {

                context("swagger, inline") {

                    runTestPart("reflection", case.expectedSwaggerInline != null && case.withReflection) {
                        swaggerSchemaInlined(case.type, case) {
                            this
                                .collectSubTypes()
                                .collectJacksonSubTypes(typeProcessing = { t -> t.analyzeTypeUsingReflection() })
                                .analyzeTypeUsingReflection(case.reflectionConfig)
                                .addMissingSupertypeSubtypeRelations()
                        }
                            .also {
                                println("== ACTUAL =======================")
                                println(it)
                                println()
                                println("== EXPECTED =====================")
                                println(case.expectedSwaggerInline)
                            }
                            .shouldEqualJsonLenient(case.expectedSwaggerInline!!)
                    }
                    runTestPart(
                        "kotlinx-serialization",
                        (case.expectedSwaggerInline != null || case.expectedSwaggerInlineKotlinxSerialization != null) && case.withKotlinxSerialization
                    ) {
                        swaggerSchemaInlined(case.type, case) {
                            this
                                .analyzeTypeUsingKotlinxSerialization(case.kotlinxSerializationConfig)
                                .addMissingSupertypeSubtypeRelations()
                        }
                            .also {
                                println("== ACTUAL =======================")
                                println(it)
                                println()
                                println("== EXPECTED =====================")
                                println(case.expectedSwaggerInlineKotlinxSerialization ?: case.expectedSwaggerInline)
                            }
                            .shouldEqualJsonLenient(case.expectedSwaggerInlineKotlinxSerialization ?: case.expectedSwaggerInline!!)
                    }

                }

                context("swagger, referenced") {

                    runTestPart("reflection", case.expectedSwaggerReference != null && case.withReflection) {
                        swaggerSchemaReferenced(case.type, case) {
                            this
                                .collectSubTypes()
                                .collectJacksonSubTypes(typeProcessing = { t -> t.analyzeTypeUsingReflection() })
                                .analyzeTypeUsingReflection(case.reflectionConfig)
                                .addMissingSupertypeSubtypeRelations()
                        }
                            .also {
                                println("== ACTUAL =======================")
                                println(it)
                                println()
                                println("== EXPECTED =====================")
                                println(case.expectedSwaggerReference!!)
                            }
                            .shouldEqualJsonLenient(case.expectedSwaggerReference!!)
                    }
                    runTestPart(
                        "kotlinx-serialization",
                        (case.expectedSwaggerReference != null || case.expectedSwaggerReferenceKotlinxSerialization != null) && case.withKotlinxSerialization
                    ) {
                        swaggerSchemaReferenced(case.type, case) {
                            this
                                .analyzeTypeUsingKotlinxSerialization(case.kotlinxSerializationConfig)
                                .addMissingSupertypeSubtypeRelations()
                        }
                            .also {
                                println("== ACTUAL =======================")
                                println(it)
                                println()
                                println("== EXPECTED =====================")
                                println(case.expectedSwaggerReferenceKotlinxSerialization ?: case.expectedSwaggerReference!!)
                            }
                            .shouldEqualJsonLenient(case.expectedSwaggerReferenceKotlinxSerialization ?: case.expectedSwaggerReference!!)
                    }

                }

                context("json schema, inline") {

                    runTestPart("reflection", case.expectedJsonInline != null && case.withReflection) {
                        jsonSchemaInlined(case.type, case) {
                            this
                                .collectSubTypes()
                                .collectJacksonSubTypes(typeProcessing = { t -> t.analyzeTypeUsingReflection() })
                                .analyzeTypeUsingReflection(case.reflectionConfig)
                                .addMissingSupertypeSubtypeRelations()
                        }
                            .also {
                                println("== ACTUAL =======================")
                                println(it)
                                println()
                                println("== EXPECTED =====================")
                                println(case.expectedJsonInline!!)
                            }
                            .shouldEqualJsonLenient(case.expectedJsonInline!!)
                    }
                    runTestPart(
                        "kotlinx-serialization",
                        (case.expectedJsonInline != null || case.expectedJsonInlineKotlinxSerialization != null) && case.withKotlinxSerialization
                    ) {
                        jsonSchemaInlined(case.type, case) {
                            this
                                .analyzeTypeUsingKotlinxSerialization(case.kotlinxSerializationConfig)
                                .addMissingSupertypeSubtypeRelations()
                        }
                            .also {
                                println("== ACTUAL =======================")
                                println(it)
                                println()
                                println("== EXPECTED =====================")
                                println(case.expectedJsonInlineKotlinxSerialization ?: case.expectedJsonInline!!)
                            }
                            .shouldEqualJsonLenient(case.expectedJsonInlineKotlinxSerialization ?: case.expectedJsonInline!!)
                    }
                }

                context("json schema, referenced") {

                    runTestPart("reflection", case.expectedJsonReference != null && case.withReflection) {
                        jsonSchemaReferenced(case.type, case) {
                            this
                                .collectSubTypes()
                                .collectJacksonSubTypes(typeProcessing = { t -> t.analyzeTypeUsingReflection() })
                                .analyzeTypeUsingReflection(case.reflectionConfig)
                                .addMissingSupertypeSubtypeRelations()
                        }
                            .also {
                                println("== ACTUAL =======================")
                                println(it)
                                println()
                                println("== EXPECTED =====================")
                                println(case.expectedJsonReference!!)
                            }
                            .shouldEqualJsonLenient(case.expectedJsonReference!!)
                    }
                    runTestPart(
                        "kotlinx-serialization",
                        (case.expectedJsonReference != null || case.expectedJsonReferenceKotlinxSerialization != null) && case.withKotlinxSerialization
                    ) {
                        jsonSchemaReferenced(case.type, case) {
                            this
                                .analyzeTypeUsingKotlinxSerialization(case.kotlinxSerializationConfig)
                                .addMissingSupertypeSubtypeRelations()
                        }
                            .also {
                                println("== ACTUAL =======================")
                                println(it)
                                println()
                                println("== EXPECTED =====================")
                                println(case.expectedJsonReferenceKotlinxSerialization ?: case.expectedJsonReference!!)
                            }
                            .shouldEqualJsonLenient(case.expectedJsonReferenceKotlinxSerialization ?: case.expectedJsonReference!!)
                    }

                }

            }
        }

        private suspend fun FunSpecContainerScope.runTestPart(name: String, enabled: Boolean, action: () -> Unit) {
            if (enabled) {
                test(name) {
                    action()
                }
            } else {
                xtest(name) {}
            }
        }


        private fun swaggerSchemaInlined(type: KType, data: TestCase, analyzeStep: InitialTypeData.() -> TypeDataGroup) =
            generateSwaggerSchema(type, data, analyzeStep) { compileInlining() }

        private fun swaggerSchemaReferenced(type: KType, data: TestCase, analyzeStep: InitialTypeData.() -> TypeDataGroup) =
            generateSwaggerSchema(type, data, analyzeStep) { compileReferencing(pathType = data.swaggerRefType) }

        private fun generateSwaggerSchema(
            type: KType,
            data: TestCase,
            analyzeStep: InitialTypeData.() -> TypeDataGroup,
            compileStep: IntermediateSwaggerSchemaData.() -> CompiledSwaggerSchemaData
        ): String {

            val result = initial(type)
                .let(analyzeStep)
                .let(data.postAnalyze)
                .generateSwaggerSchema(data.swaggerGeneratorConfig)
                .let(data.postGenerateSwaggerSchema)
                .compileStep()

            val swagger = Components().also { components ->
                components.schemas = buildMap {
                    this["_root"] = result.swagger
                    result.componentSchemas.forEach { (name, schema) ->
                        this[name] = schema
                    }
                }
            }

            return Json31.pretty(swagger)
        }

        private fun jsonSchemaInlined(type: KType, data: TestCase, analyzeStep: InitialTypeData.() -> TypeDataGroup) =
            generateJsonSchema(type, data, analyzeStep) { compileInlining() }

        private fun jsonSchemaReferenced(type: KType, data: TestCase, analyzeStep: InitialTypeData.() -> TypeDataGroup) =
            generateJsonSchema(type, data, analyzeStep) { compileReferencing(pathType = data.jsonRefType) }

        private fun generateJsonSchema(
            type: KType,
            data: TestCase,
            analyzeStep: InitialTypeData.() -> TypeDataGroup,
            compileStep: IntermediateJsonSchemaData.() -> CompiledJsonSchemaData
        ): String {

            val result = initial(type)
                .let(analyzeStep)
                .let(data.postAnalyze)
                .generateJsonSchema(data.jsonGeneratorConfig)
                .let(data.postGenerateJsonSchema)
                .compileStep()

            val mergedSchema = (result.json.copyNode() as JsonObject).also { schema ->
                if (result.definitions.isNotEmpty()) {
                    schema.properties["definitions"] = JsonObject(
                        buildMap {
                            result.definitions.forEach { (defName, defSchema) ->
                                this[defName] = defSchema
                            }
                        }.toMutableMap()
                    )
                }
            }

            return mergedSchema.prettyPrint()
        }

        private fun String.shouldEqualJsonLenient(expected: String): String {
            return this.shouldEqualJson {
                propertyOrder = PropertyOrder.Lenient
                arrayOrder = ArrayOrder.Lenient
                fieldComparison = FieldComparison.Strict
                numberFormat = NumberFormat.Lenient
                typeCoercion = TypeCoercion.Disabled
                expected
            }
        }

    }

}
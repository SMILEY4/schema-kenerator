@file:OptIn(ExperimentalSerializationApi::class)

package io.github.smiley4.schemakenerator.serialization.analyzer

import io.github.smiley4.schemakenerator.core.data.InitialKTypeData
import io.github.smiley4.schemakenerator.core.data.InitialTypeData
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeDataGroup
import io.github.smiley4.schemakenerator.core.data.TypeDataUtils.matches
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.core.data.WrappedTypeData
import io.github.smiley4.schemakenerator.serialization.data.InitialSerialDescriptorTypeData
import io.github.smiley4.schemakenerator.serialization.data.TypeRedirect
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.capturedKClass
import kotlinx.serialization.modules.SerializersModule
import kotlin.reflect.KType

internal class SerializationTypeAnalyzerImpl(
    /**
     * kotlinx serializers module from `Json { }.serializersModule` for support of contextual serializers
     */
    private val serializersModule: SerializersModule?,
    /**
     * redirect types to other types, i.e. when a type is found as a key, the corresponding type will be processed instead
     */
    private val typeRedirects: List<TypeRedirect>,
    /**
     * List of modules for type analysis. First matching module is used to analyze a given type.
     */
    private val modules: List<SerializationTypeAnalyzerModule>,
) : SerializationTypeAnalyzer {

    /**
     * Analyzes the given input type bundle
     */
    fun analyze(input: InitialTypeData): TypeDataGroup {

        val knownTypeData = mutableListOf<TypeData>()

        val root = when (input) {
            is InitialKTypeData -> {
                input.associatedTypes.forEach { analyze(it, knownTypeData) }
                analyze(input.type, knownTypeData)
            }
            is InitialSerialDescriptorTypeData -> {
                analyze(input.type, knownTypeData)
            }
            else -> throw IllegalArgumentException("Unsupported input type: ${input::class.qualifiedName}")
        }

        return TypeDataGroup(
            rootId = root.typeData.id,
            data = knownTypeData.associateBy { it.id },
        )
    }

    private fun analyze(input: KType, knownTypeData: MutableList<TypeData>): WrappedTypeData {
        return getSerializerFor(input)
            ?.let { serializer -> analyze(serializer.descriptor, knownTypeData, TypeDataCache()) }
            ?: WrappedTypeData(
                typeData = knownTypeData.find(TypeId.createWildcard()) ?: TypeData.createWildcard().also { knownTypeData.add(it) },
                nullable = false
            )
    }

    private fun analyze(input: SerialDescriptor, knownTypeData: MutableList<TypeData>): WrappedTypeData {
        return analyze(input, knownTypeData, TypeDataCache())
    }

    override fun analyze(
        descriptor: SerialDescriptor,
        knownTypeData: MutableList<TypeData>,
        cache: TypeDataCache
    ): WrappedTypeData {

        // check type redirects
        val matchingRedirect = typeRedirects.findLast { it.matches(descriptor) }
        if (matchingRedirect != null) {
            val (targetDescriptor, targetType) = matchingRedirect.buildTargetType(descriptor)
            return if (targetDescriptor != null) {
                analyze(targetDescriptor, knownTypeData)
            } else {
                analyze(targetType!!, knownTypeData)
            }
        }

        // check contextual descriptors
        val contextualByKClass = descriptor.capturedKClass?.let { serializersModule?.getContextual(it)?.descriptor }
        if (contextualByKClass != null) {
            return analyze(contextualByKClass, knownTypeData, cache)
        }

        // input serial descriptor has already been parsed before (or is currently being parsed) -> break out of infinite loops
        cache[descriptor]?.also {
            return@analyze WrappedTypeData(
                typeData = it,
                nullable = descriptor.isNullable
            )
        }

        // reserve this descriptor / mark this descriptor as processed with a pending result
        // reserve type-id so that other types can already reference this type (e.g. members resulting in a reference loop)
        val reservedTypeId = TypeId.create()
        cache[descriptor] = TypeData.createPlaceholder(reservedTypeId)

        // find matching analyzer module for type
        val module = modules.firstOrNull { it.applies(descriptor) }
            ?: throw IllegalArgumentException("No analysis module matches the given serial descriptor '$descriptor'.")

        // analyze type
        val wrappedTypeData = module.analyze(
            SerializationTypeAnalyzerModule.Context(
                analyzer = this,
                id = reservedTypeId,
                descriptor = descriptor,
                knownTypeData = knownTypeData,
                cache = cache
            )
        )

        // handle analysis result
        return wrappedTypeData.also { result ->
            knownTypeData.removeIf {
                it.matches(
                    other = result.typeData,
                    compareId = false,
                    compareIdentifyingName = true,
                    compareDescriptiveName = true,
                    compareTypeParameters = true,
                    compareMembers = true
                )
            }
            knownTypeData.add(result.typeData)
            cache[descriptor] = result.typeData
        }
    }


    /**
     * @return the type data with the given id or null
     */
    private fun Collection<TypeData>.find(id: TypeId): TypeData? = this.find { it.id == id }

}

package io.github.smiley4.schemakenerator.core.data

import kotlin.reflect.KType

/**
 * A [InputType] containing [KType] (usually acquired via reflection)
 */
class KTypeInput(val kType: KType) : InputType

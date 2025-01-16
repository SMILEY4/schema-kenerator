package io.github.smiley4.schemakenerator.core.data

import kotlin.reflect.KType

interface InputType

class KTypeInput(val kType: KType) : InputType
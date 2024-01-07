package io.github.smiley4.schemakenerator.reflection

import io.github.smiley4.schemakenerator.core.parser.CustomTypeParser
import kotlin.reflect.KClass

interface CustomReflectionTypeParser : CustomTypeParser<KClass<*>>
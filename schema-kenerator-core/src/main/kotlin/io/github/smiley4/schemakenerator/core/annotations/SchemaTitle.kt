package io.github.smiley4.schemakenerator.core.annotations

 @Target(
     AnnotationTarget.CLASS,
 )
 @Retention(AnnotationRetention.RUNTIME)
annotation class SchemaTitle(val title: String)

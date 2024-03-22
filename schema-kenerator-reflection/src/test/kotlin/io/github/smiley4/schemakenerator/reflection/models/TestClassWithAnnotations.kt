package io.github.smiley4.schemakenerator.reflection.models

import io.github.smiley4.schemakenerator.core.parser.AnnotationData
import io.github.smiley4.schemakenerator.core.parser.ContextTypeRef
import io.github.smiley4.schemakenerator.core.parser.InlineTypeRef
import io.github.smiley4.schemakenerator.core.parser.ObjectTypeData
import io.github.smiley4.schemakenerator.core.parser.PropertyData
import io.github.smiley4.schemakenerator.core.parser.PropertyType
import io.github.smiley4.schemakenerator.core.parser.TypeId
import io.github.smiley4.schemakenerator.core.parser.Visibility

@TestAnnotation("Hello Class 1")
@TestAnnotation("Hello Class 2")
class TestClassWithAnnotations {
    @TestAnnotation("Hello Field")
    val value: String = ""
}


object TestClassWithAnnotationsMeta {

    val INLINE = ObjectTypeData(
        id = TypeId("io.github.smiley4.schemakenerator.reflection.models.TestClassWithAnnotations"),
        qualifiedName = "io.github.smiley4.schemakenerator.reflection.models.TestClassWithAnnotations",
        simpleName = "TestClassWithAnnotations",
        annotations = mutableListOf(
            AnnotationData(
                name = "io.github.smiley4.schemakenerator.reflection.models.TestAnnotation",
                annotation = NoOpAnnotation()
            ),
            AnnotationData(
                name = "io.github.smiley4.schemakenerator.reflection.models.TestAnnotation",
                annotation = NoOpAnnotation()
            )
        ),
        members = mutableListOf(
            PropertyData(
                name = "value",
                nullable = false,
                visibility = Visibility.PUBLIC,
                kind = PropertyType.PROPERTY,
                type = InlineTypeRef(MiscMeta.STRING),
                annotations = listOf(
                    AnnotationData(
                        name = "io.github.smiley4.schemakenerator.reflection.models.TestAnnotation",
                        annotation = NoOpAnnotation()
                    ),
                ),
            ),
        )
    )

    val CONTEXT = ObjectTypeData(
        id = TypeId("io.github.smiley4.schemakenerator.reflection.models.TestClassWithAnnotations"),
        qualifiedName = "io.github.smiley4.schemakenerator.reflection.models.TestClassWithAnnotations",
        simpleName = "TestClassWithAnnotations",
        annotations = mutableListOf(
            AnnotationData(
                name = "io.github.smiley4.schemakenerator.reflection.models.TestAnnotation",
                annotation = NoOpAnnotation()
            ),
            AnnotationData(
                name = "io.github.smiley4.schemakenerator.reflection.models.TestAnnotation",
                annotation = NoOpAnnotation()
            )
        ),
        members = mutableListOf(
            PropertyData(
                name = "value",
                nullable = false,
                visibility = Visibility.PUBLIC,
                kind = PropertyType.PROPERTY,
                type = ContextTypeRef(MiscMeta.STRING.id),
                annotations = listOf(
                    AnnotationData(
                        name = "io.github.smiley4.schemakenerator.reflection.models.TestAnnotation",
                        annotation = NoOpAnnotation()
                    ),
                ),
            ),
        )
    )

}
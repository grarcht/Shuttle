package com.grarcht.shuttle.framework.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration

private const val SHUTTLE_CARGO_ANNOTATION = "com.grarcht.shuttle.framework.ShuttleCargo"
private const val SERIALIZABLE_FQN = "java.io.Serializable"
private const val ERROR_NOT_SERIALIZABLE =
    "is annotated with @ShuttleCargo but does not implement java.io.Serializable. " +
        "Add ': Serializable' to the class declaration."

/**
 * Validates that every class annotated with [com.grarcht.shuttle.framework.ShuttleCargo]
 * implements [java.io.Serializable]. Emits a build error with a descriptive message if the
 * requirement is not met, guiding the consumer to add the missing interface.
 */
class ShuttleCargoProcessor(private val logger: KSPLogger) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        resolver
            .getSymbolsWithAnnotation(SHUTTLE_CARGO_ANNOTATION)
            .filterIsInstance<KSClassDeclaration>()
            .forEach { classDeclaration ->
                if (!classDeclaration.implementsSerializable()) {
                    logger.error(
                        "Class '${classDeclaration.simpleName.asString()}' $ERROR_NOT_SERIALIZABLE",
                        classDeclaration
                    )
                }
            }
        return emptyList()
    }

    private fun KSClassDeclaration.implementsSerializable(): Boolean =
        superTypes
            .map { it.resolve() }
            .any { type ->
                type.declaration.qualifiedName?.asString() == SERIALIZABLE_FQN ||
                    (type.declaration as? KSClassDeclaration)?.implementsSerializable() == true
            }
}

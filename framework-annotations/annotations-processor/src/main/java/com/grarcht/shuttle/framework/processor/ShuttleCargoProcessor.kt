package com.grarcht.shuttle.framework.processor

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated

/**
 * A KSP symbol processor for classes annotated with
 * [com.grarcht.shuttle.framework.ShuttleCargo]. Serializable enforcement is handled at the IR
 * level by the companion compiler plugin, so this processor is reserved for any additional
 * source-level validations added in the future.
 */
class ShuttleCargoProcessor : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> = emptyList()
}

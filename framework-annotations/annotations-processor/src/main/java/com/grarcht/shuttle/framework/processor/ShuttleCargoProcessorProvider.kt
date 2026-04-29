package com.grarcht.shuttle.framework.processor

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

/**
 * Provides instances of [ShuttleCargoProcessor] to the KSP framework. Registered via the
 * service locator file in META-INF/services so KSP can discover it automatically at build time.
 */
class ShuttleCargoProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        ShuttleCargoProcessor(environment.logger)
}

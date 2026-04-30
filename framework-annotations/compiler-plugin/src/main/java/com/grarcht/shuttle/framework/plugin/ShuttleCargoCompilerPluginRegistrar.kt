package com.grarcht.shuttle.framework.plugin

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration

/**
 * Registers the Shuttle compiler plugin extensions with the Kotlin compiler. Enables the IR
 * generation extension that automatically implements [java.io.Serializable] on any class
 * annotated with [com.grarcht.shuttle.framework.ShuttleCargo].
 */
@OptIn(ExperimentalCompilerApi::class)
class ShuttleCargoCompilerPluginRegistrar : CompilerPluginRegistrar() {

    override val supportsK2: Boolean = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        IrGenerationExtension.registerExtension(ShuttleCargoIrGenerationExtension())
    }
}

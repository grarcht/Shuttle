package com.grarcht.shuttle.framework.plugin

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

/**
 * An IR generation extension that transforms the module after it has been converted to IR.
 * Delegates to [ShuttleCargoIrTransformer] to add [java.io.Serializable] to annotated classes.
 */
class ShuttleCargoIrGenerationExtension : IrGenerationExtension {

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        moduleFragment.transform(ShuttleCargoIrTransformer(pluginContext), null)
    }
}

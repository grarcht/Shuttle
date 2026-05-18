package com.grarcht.shuttle.framework.plugin

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

private val SHUTTLE_CARGO_FQ_NAME = FqName("com.grarcht.shuttle.framework.ShuttleCargo")
private val SHUTTLE_CARGO_DATA_FQ_NAME = FqName("com.grarcht.shuttle.framework.ShuttleCargoData")
private val SHUTTLE_CARGO_DATA_CLASS_ID = ClassId(
    FqName("com.grarcht.shuttle.framework"),
    Name.identifier("ShuttleCargoData")
)

/**
 * An IR element transformer that visits every class in the module and, for any class annotated
 * with [com.grarcht.shuttle.framework.ShuttleCargo], injects
 * [com.grarcht.shuttle.framework.ShuttleCargoData] as a supertype if it is not already present.
 * Since [ShuttleCargoData] extends [java.io.Serializable], both contracts are satisfied
 * transitively with a single injection.
 *
 * @param pluginContext the IR plugin context used to resolve class symbols.
 */
class ShuttleCargoIrTransformer(
    private val pluginContext: IrPluginContext
) : IrElementTransformerVoid() {

    override fun visitClass(declaration: IrClass): IrStatement {
        if (declaration.hasAnnotation(SHUTTLE_CARGO_FQ_NAME) &&
            !declaration.implementsCargoDataTransitively()
        ) {
            val cargoDataSymbol = pluginContext.referenceClass(SHUTTLE_CARGO_DATA_CLASS_ID)
            if (cargoDataSymbol != null) {
                declaration.superTypes = declaration.superTypes + cargoDataSymbol.owner.defaultType
            }
        }
        return super.visitClass(declaration)
    }

    @OptIn(UnsafeDuringIrConstructionAPI::class)
    private fun IrClass.implementsCargoDataTransitively(): Boolean =
        superTypes.any { superType ->
            superType.classFqName == SHUTTLE_CARGO_DATA_FQ_NAME ||
                (superType.classOrNull?.owner?.implementsCargoDataTransitively() == true)
        }
}

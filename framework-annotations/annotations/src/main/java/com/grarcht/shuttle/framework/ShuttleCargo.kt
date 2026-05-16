package com.grarcht.shuttle.framework

/**
 * Marks a class as eligible for transport by Shuttle. The Shuttle compiler plugin automatically
 * injects [ShuttleCargoData] (and transitively [java.io.Serializable]) into the annotated class
 * at compile time. No explicit supertype declaration is required from consumers.
 *
 * Usage:
 * ```kotlin
 * @ShuttleCargo
 * class MyModel(val id: String, val data: ByteArray)
 * ```
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS)
annotation class ShuttleCargo

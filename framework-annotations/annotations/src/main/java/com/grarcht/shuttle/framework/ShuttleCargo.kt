package com.grarcht.shuttle.framework

/**
 * Marks a class as eligible for transport by Shuttle. Classes annotated with
 * [ShuttleCargo] are validated at build time to ensure they implement
 * [java.io.Serializable], which is required for Shuttle's persistence mechanism.
 *
 * This annotation is the primary contract for Shuttle consumers. The underlying
 * serialization mechanism is an implementation detail managed by Shuttle.
 *
 * Usage:
 * ```kotlin
 * @ShuttleCargo
 * class MyModel(val id: String, val data: ByteArray) : Serializable
 * ```
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS)
annotation class ShuttleCargo

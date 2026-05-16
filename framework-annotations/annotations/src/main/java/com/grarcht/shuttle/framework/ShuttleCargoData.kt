package com.grarcht.shuttle.framework

import java.io.Serializable

/**
 * A marker interface identifying a class as Shuttle cargo. Any class annotated with
 * [ShuttleCargo] automatically implements this interface via the Shuttle compiler plugin,
 * which also injects [java.io.Serializable] transitively. Shuttle's public APIs accept
 * [ShuttleCargoData] so that the underlying serialization mechanism stays hidden from consumers.
 */
interface ShuttleCargoData : Serializable

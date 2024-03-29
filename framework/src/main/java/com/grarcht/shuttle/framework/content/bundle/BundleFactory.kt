package com.grarcht.shuttle.framework.content.bundle

import android.os.Bundle
import android.os.PersistableBundle

/**
 *  This contractual interface is used to provide a factory that [Bundle] objects.  Having this factory enables for
 *  unit testing of [Bundle] objects in the Shuttle Framework.
 *
 *  For more information on the factory design pattern, refer to:
 *  <a href="https://www.tutorialspoint.com/design_pattern/factory_pattern.htm">Factory Design Pattern</a>
 */
interface BundleFactory {
    /**
     * @return the newly created bundle reference
     */
    fun create(): Bundle

    /**
     * @param loader An explicit ClassLoader to use when instantiating objects inside of the Bundle.
     * @return the newly created bundle reference
     */
    fun create(loader: ClassLoader?): Bundle

    /**
     * @param capacity The initial capacity of the Bundle
     * @return the newly created bundle reference
     */
    fun create(capacity: Int): Bundle

    /**
     * @param bundle A bundle to be shallow copied.
     * @return the newly created bundle reference
     */
    fun create(bundle: Bundle): Bundle

    /**
     * @param bundle APersistableBundle to be shallow copied.
     * @return the newly created bundle reference
     */
    fun create(bundle: PersistableBundle): Bundle
}

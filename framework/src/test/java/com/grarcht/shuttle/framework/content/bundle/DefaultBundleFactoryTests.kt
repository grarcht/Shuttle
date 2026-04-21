package com.grarcht.shuttle.framework.content.bundle

import android.os.Bundle
import android.os.PersistableBundle
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock

class DefaultBundleFactoryTests {

    @Test
    fun verifyCreateWithNoArgsReturnsBundle() {
        val factory = DefaultBundleFactory()
        val bundle = factory.create()
        assertNotNull(bundle)
    }

    @Test
    fun verifyCreateWithClassLoaderReturnsBundle() {
        val factory = DefaultBundleFactory()
        val bundle = factory.create(loader = null)
        assertNotNull(bundle)
    }

    @Test
    fun verifyCreateWithCapacityReturnsBundle() {
        val factory = DefaultBundleFactory()
        val bundle = factory.create(capacity = 4)
        assertNotNull(bundle)
    }

    @Test
    fun verifyCreateWithBundleReturnsBundle() {
        val factory = DefaultBundleFactory()
        val source = mock(Bundle::class.java)
        val bundle = factory.create(bundle = source)
        assertNotNull(bundle)
    }

    @Test
    fun verifyCreateWithPersistableBundleReturnsBundle() {
        val factory = DefaultBundleFactory()
        val source = mock(PersistableBundle::class.java)
        val bundle = factory.create(bundle = source)
        assertNotNull(bundle)
    }
}

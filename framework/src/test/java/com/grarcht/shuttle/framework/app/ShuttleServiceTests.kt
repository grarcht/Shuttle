package com.grarcht.shuttle.framework.app

import android.content.Intent
import android.os.Looper
import android.os.Message
import com.grarcht.shuttle.framework.Shuttle
import com.grarcht.shuttle.framework.content.ShuttleIntent
import com.grarcht.shuttle.framework.os.messenger.ShuttleMessengerDecorator
import com.grarcht.shuttle.framework.os.messenger.factory.ShuttleMessengerFactory
import com.grarcht.shuttle.framework.validator.ShuttleServiceMessageValidator
import com.grarcht.shuttle.framework.visibility.observation.ShuttleVisibilityObservable
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

private const val SERVICE_NAME = "TestService"
private const val TEST_SERVICE_CLASS_NAME = "TestShuttleService"
private const val CARGO_ID = "cargoId"
private const val CARGO_ID_1 = "cargoId1"
private const val CARGO_DATA = "cargo"

/**
 * Verifies the functionality of [ShuttleService]. ShuttleService is the Android Service base
 * class that underpins both local-bound and IPC messenger-bound service configurations in the
 * Shuttle framework. Without correct binding, unbinding, and resource-release behaviour, client
 * components would fail to communicate with the service or leak resources.
 */
class ShuttleServiceTests {

    private fun createService(bindingType: ShuttleServiceType): TestShuttleService {
        val service = TestShuttleService()
        val shuttle = mock<Shuttle>()
        val errorObservable = mock<ShuttleVisibilityObservable>()
        val messengerFactory = mock<ShuttleMessengerFactory>()
        service.config = ShuttleServiceConfig(
            serviceName = SERVICE_NAME,
            shuttle = shuttle,
            rebindOnUnbind = false,
            errorObservable = errorObservable,
            bindingType = bindingType,
            messengerFactory = messengerFactory,
            messageValidator = ShuttleServiceMessageValidator()
        )
        return service
    }

    @Test
    fun verifyOnStartCommandReturnsRedeliverIntent() {
        val service = createService(ShuttleServiceType.BOUND_LOCAL)
        val result = service.onStartCommand(intent = null, flags = 0, startId = 1)
        assertEquals(android.app.Service.START_REDELIVER_INTENT, result)
    }

    @Test
    fun verifyOnBindWithLocalTypeCreatesBinder() {
        val service = createService(ShuttleServiceType.BOUND_LOCAL)
        val binder = service.onBind(intent = null)
        assertNotNull(binder)
    }

    @Test
    fun verifyOnBindWithMessengerTypeCreatesIpcBinder() {
        val service = Mockito.spy(createService(ShuttleServiceType.BOUND_MESSENGER))
        Mockito.doReturn(null).`when`(service).createMessengerDecoratorForIPC()

        service.onBind(intent = null)

        Mockito.verify(service).createMessengerDecoratorForIPC()
    }

    @Test
    fun verifyOnBindWithNonBoundTypeReturnsNull() {
        val service = createService(ShuttleServiceType.NON_BOUND_STARTED)
        val binder = service.onBind(intent = null)
        assertNull(binder)
    }

    @Test
    fun verifyOnRebindWithLocalTypeDoesNotThrow() {
        val service = createService(ShuttleServiceType.BOUND_LOCAL)
        service.onRebind(intent = null)
    }

    @Test
    fun verifyOnUnbindReturnsRebindOnUnbindConfig() {
        val service = TestShuttleService()
        val shuttle = mock<Shuttle>()
        val errorObservable = mock<ShuttleVisibilityObservable>()
        val messengerFactory = mock<ShuttleMessengerFactory>()
        service.config = ShuttleServiceConfig(
            serviceName = SERVICE_NAME,
            shuttle = shuttle,
            rebindOnUnbind = true,
            errorObservable = errorObservable,
            bindingType = ShuttleServiceType.BOUND_LOCAL,
            messengerFactory = messengerFactory
        )

        val result = service.onUnbind(intent = null)

        assertEquals(true, result)
    }

    @Test
    fun verifyOnDestroyForLocalServiceClearsBinderAndDoesNotThrow() {
        val service = createService(ShuttleServiceType.BOUND_LOCAL)
        service.onBind(null)

        service.onDestroy()

        assertNull(service.binder)
    }

    @Test
    fun verifyReleaseResourcesForIPCServicesWithMessengerType() {
        val service = createService(ShuttleServiceType.BOUND_MESSENGER)
        val mockDecorator = mock<ShuttleMessengerDecorator>()
        whenever(mockDecorator.cargoIds).thenReturn(mutableListOf(CARGO_ID_1))
        service.ipcServiceMessengerDecorator = mockDecorator
        val shuttle = service.config.shuttle
        whenever(shuttle.cleanShuttleFromDeliveryFor(any(), any())).thenReturn(shuttle)

        service.releaseResourcesForIPCServices()

        assertAll(
            { assertNull(service.ipcServiceMessengerDecorator) },
            { assertNull(service.binder) }
        )
    }

    @Test
    fun verifyReleaseResourcesForLocalServicesWithLocalType() {
        val service = createService(ShuttleServiceType.BOUND_LOCAL)
        service.onBind(null)
        assertNotNull(service.binder)

        service.releaseResourcesForLocalServices()

        assertNull(service.binder)
    }

    @Test
    fun verifyGetServiceNameReturnsSimpleName() {
        val service = createService(ShuttleServiceType.BOUND_LOCAL)
        assertEquals(TEST_SERVICE_CLASS_NAME, service.getServiceName())
    }

    @Test
    fun verifyOnReceiveMessageDoesNotThrow() {
        val service = createService(ShuttleServiceType.BOUND_LOCAL)
        service.onReceiveMessage(0, mock<Message>())
    }

    @Test
    fun verifyCreateBinderForLocalBoundServiceWithNonLocalTypeReturnsNull() {
        val service = createService(ShuttleServiceType.BOUND_AIDL)

        val binder = service.createBinderForLocalBoundService()

        assertNull(binder)
    }

    @Test
    fun verifyCreateMessengerDecoratorForIPCWithNonMessengerTypeReturnsNull() {
        val service = createService(ShuttleServiceType.BOUND_LOCAL)

        val binder = service.createMessengerDecoratorForIPC()

        assertNull(binder)
    }

    @Test
    fun verifyCreateMessengerDecoratorForIPCReusesExistingDecorator() {
        val service = createService(ShuttleServiceType.BOUND_MESSENGER)
        val mockDecorator = mock<ShuttleMessengerDecorator>()
        whenever(mockDecorator.getBinder()).thenReturn(null)
        service.ipcServiceMessengerDecorator = mockDecorator

        val result = service.createMessengerDecoratorForIPC()

        assertNull(result) // decorator.getBinder() returns null (mock default)
    }

    @Test
    fun verifyTransportCargoWithShuttleCallsGetCargoIntentForTransport() {
        val service = Mockito.spy(createService(ShuttleServiceType.BOUND_LOCAL))
        val mockIntent = mock<Intent>()
        Mockito.doReturn(mockIntent).`when`(service).getCargoIntentForTransport(any<String>(), anyOrNull<String>())

        service.transportCargoWithShuttle(CARGO_ID, CARGO_DATA)

        Mockito.verify(service).getCargoIntentForTransport(any(), anyOrNull())
    }

    @Test
    fun verifyGetCargoIntentForTransportReturnsCreatedIntent() {
        val service = createService(ShuttleServiceType.BOUND_LOCAL)
        val mockShuttleIntent = Mockito.mock(ShuttleIntent::class.java)
        val mockFinalIntent = mock<Intent>()

        whenever(service.config.shuttle.intentCargoWith(any<Intent>())).thenReturn(mockShuttleIntent)
        whenever(mockShuttleIntent.logTag(anyOrNull())).thenReturn(mockShuttleIntent)
        whenever(mockShuttleIntent.transport(any<String>(), anyOrNull())).thenReturn(mockShuttleIntent)
        whenever(mockShuttleIntent.create()).thenReturn(mockFinalIntent)

        val result = service.getCargoIntentForTransport(CARGO_ID, CARGO_DATA)

        assertEquals(mockFinalIntent, result)
    }

    @Test
    fun verifyCreateMessengerDecoratorForIPCWithNullDecoratorCreatesNewDecorator() {
        val mockLooper = mock<Looper>()
        val service = TestShuttleServiceWithLooper(mockLooper)
        val shuttle = mock<Shuttle>()
        val errorObservable = mock<ShuttleVisibilityObservable>()
        val messengerFactory = mock<ShuttleMessengerFactory>()
        service.config = ShuttleServiceConfig(
            serviceName = SERVICE_NAME,
            shuttle = shuttle,
            rebindOnUnbind = false,
            errorObservable = errorObservable,
            bindingType = ShuttleServiceType.BOUND_MESSENGER,
            messengerFactory = messengerFactory,
            messageValidator = ShuttleServiceMessageValidator()
        )
        val mockDecorator = mock<ShuttleMessengerDecorator>()
        whenever(mockDecorator.getBinder()).thenReturn(null)
        whenever(
            messengerFactory.createMessenger(any(), any(), any(), any(), any())
        ).thenReturn(mockDecorator)

        service.createMessengerDecoratorForIPC()

        assertEquals(mockDecorator, service.ipcServiceMessengerDecorator)
    }

    @Test
    fun verifyCreateMessengerDecoratorForIPCWithFactoryReturningNullReturnsNull() {
        val mockLooper = mock<Looper>()
        val service = TestShuttleServiceWithLooper(mockLooper)
        val shuttle = mock<Shuttle>()
        val errorObservable = mock<ShuttleVisibilityObservable>()
        val messengerFactory = mock<ShuttleMessengerFactory>()
        service.config = ShuttleServiceConfig(
            serviceName = SERVICE_NAME,
            shuttle = shuttle,
            rebindOnUnbind = false,
            errorObservable = errorObservable,
            bindingType = ShuttleServiceType.BOUND_MESSENGER,
            messengerFactory = messengerFactory,
            messageValidator = ShuttleServiceMessageValidator()
        )
        whenever(
            messengerFactory.createMessenger(any(), any(), any(), any(), any())
        ).thenReturn(null)

        val result = service.createMessengerDecoratorForIPC()

        assertAll(
            { assertNull(result) },
            { assertNull(service.ipcServiceMessengerDecorator) }
        )
    }

    @Test
    fun verifyReleaseResourcesForIPCServicesWithNullDecoratorDoesNotThrow() {
        val service = createService(ShuttleServiceType.BOUND_MESSENGER)
        // ipcServiceMessengerDecorator is null by default

        service.releaseResourcesForIPCServices()

        assertNull(service.ipcServiceMessengerDecorator)
    }

    private class TestShuttleService : ShuttleService()

    private class TestShuttleServiceWithLooper(private val looper: Looper) : ShuttleService() {
        override fun getMainLooper(): Looper = looper
    }
}

package com.aggregateservice.core.network

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthEventBusTest {

    @Test
    fun `events has no replay cache`() = runTest {
        val bus = AuthEventBus()

        bus.emit(AuthEvent.Logout)

        bus.events.test {
            expectNoEvents()
            cancel()
        }
    }

    @Test
    fun `events delivers to active subscribers`() = runTest {
        val bus = AuthEventBus()

        bus.events.test {
            bus.emit(AuthEvent.Logout)
            val event = awaitItem()
            assertEquals(AuthEvent.Logout, event)
            cancel()
        }
    }

    @Test
    fun `Logout is the only AuthEvent type`() = runTest {
        val bus = AuthEventBus()

        bus.events.test {
            bus.emit(AuthEvent.Logout)
            val event = awaitItem()
            assertEquals(AuthEvent.Logout, event)
            assertEquals("Logout", event.toString())
            cancel()
        }
    }

    @Test
    fun `extraBufferCapacity buffers one event`() = runTest {
        val bus = AuthEventBus()

        bus.emit(AuthEvent.Logout)

        bus.events.test {
            expectNoEvents()
            bus.emit(AuthEvent.Logout)
            val event = awaitItem()
            assertEquals(AuthEvent.Logout, event)
            cancel()
        }
    }
}

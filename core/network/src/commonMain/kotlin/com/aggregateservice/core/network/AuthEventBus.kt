package com.aggregateservice.core.network

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Sealed class representing authentication events.
 */
sealed class AuthEvent {
    /**
     * Logout event - emitted when user logs out or token refresh fails.
     * One-shot event, no replay.
     */
    data object Logout : AuthEvent()
}

/**
 * Event bus for logout propagation.
 *
 * Uses MutableSharedFlow WITHOUT replay cache (replay=0 by default).
 * New subscribers receive ONLY future events, not past events.
 * This prevents new subscribers from receiving stale logout events.
 *
 * extraBufferCapacity=1 allows buffering one event if no subscribers
 * are listening at the moment of emit.
 */
class AuthEventBus {

    private val _events = MutableSharedFlow<AuthEvent>(
        extraBufferCapacity = 1,  // Buffer one event if no subscribers
        // Note: replay defaults to 0 = no replay cache
    )

    /**
     * SharedFlow exposing auth events to subscribers.
     * Subscribers only receive events emitted after they subscribe.
     */
    val events: SharedFlow<AuthEvent> = _events.asSharedFlow()

    /**
     * Emit an auth event.
     * Propagates to all active subscribers immediately.
     * If no subscribers, event is buffered (extraBufferCapacity=1).
     *
     * @param event AuthEvent to emit
     */
    fun emit(event: AuthEvent) {
        _events.tryEmit(event)
    }
}

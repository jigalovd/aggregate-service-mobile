package com.aggregateservice.feature.provider.bookings.presentation.screenmodel

import co.touchlab.kermit.Logger
import com.aggregateservice.core.network.AppError
import com.aggregateservice.feature.provider.bookings.domain.model.BookingFilter
import com.aggregateservice.feature.provider.bookings.domain.model.BookingStatus
import com.aggregateservice.feature.provider.bookings.domain.model.ProviderBooking
import com.aggregateservice.feature.provider.bookings.domain.repository.ProviderBookingRepository
import com.aggregateservice.feature.provider.bookings.presentation.model.ProviderBookingsUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Instant
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for ProviderBookingsScreenModel.
 * Uses functional mocks with behavior injection for predictable test scenarios.
 * Follows ProviderDashboardScreenModelTest pattern (D013).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProviderBookingsScreenModelTest {
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var repository: MockProviderBookingRepository
    private lateinit var logger: Logger

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = MockProviderBookingRepository()
        logger = Logger.withTag("ProviderBookingsTest")
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createScreenModel(): ProviderBookingsScreenModel {
        return ProviderBookingsScreenModel(
            repository = repository,
            logger = logger,
        )
    }

    // ============ Initial Load Tests ============

    @Test
    fun `initial load should emit Content on success`() = runTest {
        val expectedBookings = listOf(
            createTestBooking("booking-1", BookingStatus.PENDING),
            createTestBooking("booking-2", BookingStatus.CONFIRMED),
        )
        repository.getProviderBookingsResult = Result.success(expectedBookings)

        val screenModel = createScreenModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = screenModel.uiState.value
        assertIs<ProviderBookingsUiState.Content>(state)
        assertEquals(2, state.bookings.size)
        assertFalse(state.isRefreshing)
        assertFalse(state.isLoadingAction)
        assertNull(state.actionError)
    }

    @Test
    fun `initial load should emit Error on API failure`() = runTest {
        repository.getProviderBookingsResult = Result.failure(
            AppError.UnknownError(message = "Network Error")
        )

        val screenModel = createScreenModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = screenModel.uiState.value
        assertIs<ProviderBookingsUiState.Error>(state)
        assertTrue(state.error is AppError.UnknownError)
        assertTrue(state.bookings.isEmpty())
    }

    @Test
    fun `initial load should handle UnauthorizedError`() = runTest {
        repository.getProviderBookingsResult = Result.failure(AppError.Unauthorized)

        val screenModel = createScreenModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = screenModel.uiState.value
        assertIs<ProviderBookingsUiState.Error>(state)
        assertTrue(state.error is AppError.Unauthorized)
    }

    @Test
    fun `empty booking list should show Content with empty list not Error`() = runTest {
        repository.getProviderBookingsResult = Result.success(emptyList())

        val screenModel = createScreenModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = screenModel.uiState.value
        assertIs<ProviderBookingsUiState.Content>(state)
        assertTrue(state.bookings.isEmpty())
        assertTrue(state.isEmpty)
    }

    // ============ Filter Tests ============

    @Test
    fun `filterBookings should update selectedFilter and reload`() = runTest {
        repository.getProviderBookingsResult = Result.success(listOf(createTestBooking("pending-1", BookingStatus.PENDING)))

        val screenModel = createScreenModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Change filter to PENDING
        repository.getProviderBookingsResult = Result.success(
            listOf(createTestBooking("pending-2", BookingStatus.PENDING))
        )

        screenModel.filterBookings(BookingFilter.PENDING)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = screenModel.uiState.value
        assertIs<ProviderBookingsUiState.Content>(state)
        assertEquals(BookingFilter.PENDING, state.selectedFilter)
    }

    @Test
    fun `filterBookings with ALL should pass null status to repository`() = runTest {
        repository.getProviderBookingsResult = Result.success(emptyList())

        val screenModel = createScreenModel()
        testDispatcher.scheduler.advanceUntilIdle()

        screenModel.filterBookings(BookingFilter.ALL)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, repository.getProviderBookingsCallCount)
        assertNull(repository.lastStatusParam) // null = ALL
    }

    @Test
    fun `filterBookings with PENDING should pass PENDING status to repository`() = runTest {
        repository.getProviderBookingsResult = Result.success(emptyList())

        val screenModel = createScreenModel()
        testDispatcher.scheduler.advanceUntilIdle()

        screenModel.filterBookings(BookingFilter.PENDING)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(BookingStatus.PENDING.name, repository.lastStatusParam)
    }

    // ============ Action Tests ============

    @Test
    fun `acceptBooking should call repository and refresh on success`() = runTest {
        repository.getProviderBookingsResult = Result.success(
            listOf(createTestBooking("pending-booking", BookingStatus.PENDING))
        )
        repository.acceptBookingResult = Result.success(Unit)

        val screenModel = createScreenModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Clear previous call count
        repository.getProviderBookingsCallCount = 0

        screenModel.acceptBooking("pending-booking")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, repository.acceptBookingCallCount)
        assertEquals("pending-booking", repository.lastAcceptedBookingId)

        // Should have refreshed
        assertTrue(repository.getProviderBookingsCallCount >= 1)
    }

    @Test
    fun `acceptBooking should show error on failure`() = runTest {
        repository.getProviderBookingsResult = Result.success(
            listOf(createTestBooking("pending-booking", BookingStatus.PENDING))
        )
        repository.acceptBookingResult = Result.failure(
            AppError.UnknownError(message = "Accept failed")
        )

        val screenModel = createScreenModel()
        testDispatcher.scheduler.advanceUntilIdle()

        screenModel.acceptBooking("pending-booking")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = screenModel.uiState.value
        assertIs<ProviderBookingsUiState.Content>(state)
        assertFalse(state.isLoadingAction)
        assertNotNull(state.actionError)
        assertTrue(state.actionError!!.contains("accept", ignoreCase = true))
    }

    @Test
    fun `rejectBooking should call repository with reason and refresh on success`() = runTest {
        repository.getProviderBookingsResult = Result.success(
            listOf(createTestBooking("pending-booking", BookingStatus.PENDING))
        )
        repository.rejectBookingResult = Result.success(Unit)

        val screenModel = createScreenModel()
        testDispatcher.scheduler.advanceUntilIdle()

        repository.getProviderBookingsCallCount = 0

        screenModel.rejectBooking("pending-booking", "Client requested")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, repository.rejectBookingCallCount)
        assertEquals("pending-booking", repository.lastRejectedBookingId)
        assertEquals("Client requested", repository.lastRejectReason)

        assertTrue(repository.getProviderBookingsCallCount >= 1)
    }

    @Test
    fun `rejectBooking should show error on failure`() = runTest {
        repository.getProviderBookingsResult = Result.success(
            listOf(createTestBooking("pending-booking", BookingStatus.PENDING))
        )
        repository.rejectBookingResult = Result.failure(
            AppError.UnknownError(message = "Reject failed")
        )

        val screenModel = createScreenModel()
        testDispatcher.scheduler.advanceUntilIdle()

        screenModel.rejectBooking("pending-booking", "Test reason")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = screenModel.uiState.value
        assertIs<ProviderBookingsUiState.Content>(state)
        assertFalse(state.isLoadingAction)
        assertNotNull(state.actionError)
        assertTrue(state.actionError!!.contains("reject", ignoreCase = true))
    }

    @Test
    fun `cancelBooking should call repository with reason and refresh on success`() = runTest {
        repository.getProviderBookingsResult = Result.success(
            listOf(createTestBooking("confirmed-booking", BookingStatus.CONFIRMED))
        )
        repository.cancelBookingResult = Result.success(Unit)

        val screenModel = createScreenModel()
        testDispatcher.scheduler.advanceUntilIdle()

        repository.getProviderBookingsCallCount = 0

        screenModel.cancelBooking("confirmed-booking", "Schedule conflict")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, repository.cancelBookingCallCount)
        assertEquals("confirmed-booking", repository.lastCancelledBookingId)
        assertEquals("Schedule conflict", repository.lastCancelReason)

        assertTrue(repository.getProviderBookingsCallCount >= 1)
    }

    @Test
    fun `cancelBooking with null reason should work`() = runTest {
        repository.getProviderBookingsResult = Result.success(
            listOf(createTestBooking("booking", BookingStatus.CONFIRMED))
        )
        repository.cancelBookingResult = Result.success(Unit)

        val screenModel = createScreenModel()
        testDispatcher.scheduler.advanceUntilIdle()

        screenModel.cancelBooking("confirmed-booking", null)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, repository.cancelBookingCallCount)
        assertNull(repository.lastCancelReason)
    }

    @Test
    fun `cancelBooking should show error on failure`() = runTest {
        repository.getProviderBookingsResult = Result.success(
            listOf(createTestBooking("booking", BookingStatus.CONFIRMED))
        )
        repository.cancelBookingResult = Result.failure(
            AppError.UnknownError(message = "Cancel failed")
        )

        val screenModel = createScreenModel()
        testDispatcher.scheduler.advanceUntilIdle()

        screenModel.cancelBooking("booking", "Test")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = screenModel.uiState.value
        assertIs<ProviderBookingsUiState.Content>(state)
        assertFalse(state.isLoadingAction)
        assertNotNull(state.actionError)
    }

    // ============ Refresh Tests ============

    @Test
    fun `refresh should set isRefreshing and reload`() = runTest {
        repository.getProviderBookingsResult = Result.success(
            listOf(createTestBooking("booking-1"))
        )

        val screenModel = createScreenModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Prepare fresh data
        repository.getProviderBookingsResult = Result.success(
            listOf(createTestBooking("booking-1"), createTestBooking("booking-2"))
        )
        repository.getProviderBookingsCallCount = 0

        screenModel.refresh()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = screenModel.uiState.value
        assertIs<ProviderBookingsUiState.Content>(state)
        assertFalse(state.isRefreshing)
        assertTrue(repository.getProviderBookingsCallCount >= 1)
    }

    @Test
    fun `refresh from error state should reload`() = runTest {
        repository.getProviderBookingsResult = Result.failure(AppError.UnknownError())
        repository.getProviderBookingsCallCount = 0

        val screenModel = createScreenModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(screenModel.uiState.value is ProviderBookingsUiState.Error)

        repository.getProviderBookingsResult = Result.success(listOf(createTestBooking("recovered")))
        repository.getProviderBookingsCallCount = 0

        screenModel.refresh()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = screenModel.uiState.value
        assertIs<ProviderBookingsUiState.Content>(state)
        assertEquals(1, state.bookings.size)
    }

    // ============ Retry Tests ============

    @Test
    fun `retry should reload data`() = runTest {
        repository.getProviderBookingsResult = Result.failure(AppError.UnknownError())

        val screenModel = createScreenModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(screenModel.uiState.value is ProviderBookingsUiState.Error)

        repository.getProviderBookingsResult = Result.success(listOf(createTestBooking("retry-booking")))
        repository.getProviderBookingsCallCount = 0

        screenModel.retry()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = screenModel.uiState.value
        assertIs<ProviderBookingsUiState.Content>(state)
        assertEquals(1, state.bookings.size)
    }

    // ============ Clear Error Tests ============

    @Test
    fun `clearActionError should clear actionError in Content state`() = runTest {
        repository.getProviderBookingsResult = Result.success(
            listOf(createTestBooking("booking", BookingStatus.PENDING))
        )
        repository.acceptBookingResult = Result.failure(AppError.UnknownError(message = "Action failed"))

        val screenModel = createScreenModel()
        testDispatcher.scheduler.advanceUntilIdle()

        screenModel.acceptBooking("booking")
        testDispatcher.scheduler.advanceUntilIdle()

        val stateWithError = screenModel.uiState.value
        assertIs<ProviderBookingsUiState.Content>(stateWithError)
        assertNotNull(stateWithError.actionError)

        screenModel.clearActionError()
        testDispatcher.scheduler.advanceUntilIdle()

        val stateAfterClear = screenModel.uiState.value
        assertIs<ProviderBookingsUiState.Content>(stateAfterClear)
        assertNull(stateAfterClear.actionError)
    }

    // ============ Helper Methods ============

    private fun createTestBooking(
        id: String,
        status: BookingStatus = BookingStatus.PENDING,
    ): ProviderBooking {
        return ProviderBooking(
            id = id,
            clientName = "Test Client $id",
            startTime = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
            endTime = Instant.fromEpochMilliseconds(System.currentTimeMillis() + 3600000),
            serviceName = "Test Service",
            status = status,
            totalPrice = 100.0,
            clientPhone = "+1234567890",
            notes = "Test notes",
        )
    }
}

/**
 * Mock implementation of ProviderBookingRepository for testing.
 */
private class MockProviderBookingRepository : ProviderBookingRepository {
    var getProviderBookingsResult: Result<List<ProviderBooking>> = Result.success(emptyList())
    var acceptBookingResult: Result<Unit> = Result.success(Unit)
    var rejectBookingResult: Result<Unit> = Result.success(Unit)
    var cancelBookingResult: Result<Unit> = Result.success(Unit)

    var getProviderBookingsCallCount = 0
    var acceptBookingCallCount = 0
    var rejectBookingCallCount = 0
    var cancelBookingCallCount = 0

    var lastStatusParam: String? = null
    var lastAcceptedBookingId: String? = null
    var lastRejectedBookingId: String? = null
    var lastRejectReason: String? = null
    var lastCancelledBookingId: String? = null
    var lastCancelReason: String? = null

    override suspend fun getProviderBookings(
        status: String?,
        page: Int,
        pageSize: Int,
    ): Result<List<ProviderBooking>> {
        getProviderBookingsCallCount++
        lastStatusParam = status
        return getProviderBookingsResult
    }

    override suspend fun acceptBooking(bookingId: String): Result<Unit> {
        acceptBookingCallCount++
        lastAcceptedBookingId = bookingId
        return acceptBookingResult
    }

    override suspend fun rejectBooking(bookingId: String, reason: String): Result<Unit> {
        rejectBookingCallCount++
        lastRejectedBookingId = bookingId
        lastRejectReason = reason
        return rejectBookingResult
    }

    override suspend fun cancelBooking(bookingId: String, reason: String?): Result<Unit> {
        cancelBookingCallCount++
        lastCancelledBookingId = bookingId
        lastCancelReason = reason
        return cancelBookingResult
    }
}

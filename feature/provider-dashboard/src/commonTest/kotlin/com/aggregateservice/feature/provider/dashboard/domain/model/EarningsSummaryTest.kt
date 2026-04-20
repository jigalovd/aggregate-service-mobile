package com.aggregateservice.feature.provider.dashboard.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for EarningsSummary domain model.
 */
class EarningsSummaryTest {

    @Test
    fun `formattedToday returns correct ILS format`() {
        val summary = EarningsSummary(
            todayAmount = 150.0,
            weekAmount = 1000.0,
            monthAmount = 5000.0,
            currency = "ILS",
        )
        assertEquals("150 ₪", summary.formattedToday)
    }

    @Test
    fun `formattedWeek returns correct ILS format`() {
        val summary = EarningsSummary(
            todayAmount = 150.0,
            weekAmount = 1000.0,
            monthAmount = 5000.0,
            currency = "ILS",
        )
        assertEquals("1000 ₪", summary.formattedWeek)
    }

    @Test
    fun `formattedMonth returns correct ILS format`() {
        val summary = EarningsSummary(
            todayAmount = 150.0,
            weekAmount = 1000.0,
            monthAmount = 5000.0,
            currency = "ILS",
        )
        assertEquals("5000 ₪", summary.formattedMonth)
    }

    @Test
    fun `formattedToday handles USD currency`() {
        val summary = EarningsSummary(
            todayAmount = 50.0,
            weekAmount = 300.0,
            monthAmount = 1500.0,
            currency = "USD",
        )
        assertEquals("50 $", summary.formattedToday)
    }

    @Test
    fun `formattedToday handles EUR currency`() {
        val summary = EarningsSummary(
            todayAmount = 75.0,
            weekAmount = 500.0,
            monthAmount = 2000.0,
            currency = "EUR",
        )
        assertEquals("75 €", summary.formattedToday)
    }

    @Test
    fun `formattedToday handles unknown currency`() {
        val summary = EarningsSummary(
            todayAmount = 100.0,
            weekAmount = 700.0,
            monthAmount = 3000.0,
            currency = "GBP",
        )
        assertEquals("100 GBP", summary.formattedToday)
    }

    @Test
    fun `empty returns zero values`() {
        val empty = EarningsSummary.empty()
        assertEquals(0.0, empty.todayAmount)
        assertEquals(0.0, empty.weekAmount)
        assertEquals(0.0, empty.monthAmount)
        assertEquals("ILS", empty.currency)
    }

    @Test
    fun `formattedToday handles zero amount`() {
        val summary = EarningsSummary(
            todayAmount = 0.0,
            weekAmount = 0.0,
            monthAmount = 0.0,
        )
        assertEquals("0 ₪", summary.formattedToday)
    }
}

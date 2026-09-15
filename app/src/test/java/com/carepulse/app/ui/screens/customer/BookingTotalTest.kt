package com.carepulse.app.ui.screens.customer

import org.junit.Assert.assertEquals
import org.junit.Test

class BookingTotalTest {

    @Test
    fun `total is hourly rate times the four-hour slot`() {
        assertEquals("LKR 22,200", formatBookingTotal(hourlyRate = 5_550))
    }

    @Test
    fun `slot length is four hours`() {
        assertEquals(4, BookingSlotHours)
    }

    @Test
    fun `custom hours are respected`() {
        assertEquals("LKR 1,500", formatBookingTotal(hourlyRate = 500, hours = 3))
    }

    @Test
    fun `large totals use thousands separators`() {
        assertEquals("LKR 4,000,000", formatBookingTotal(hourlyRate = 1_000_000))
    }

    @Test
    fun `zero rate formats as zero`() {
        assertEquals("LKR 0", formatBookingTotal(hourlyRate = 0))
    }
}

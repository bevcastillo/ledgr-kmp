package com.bev.ledgr.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ParseAmountTest {

    @Test
    fun wholeNumber_convertToCents() {
        assertEquals(15000L, parseAmountToCents("150"))
    }

    @Test
    fun twoDecimals_convertToCents() {
        assertEquals(15075L, parseAmountToCents("150.75"))
    }

    @Test
    fun oneDecimal_isPaddedToCents() {
        assertEquals(15050L, parseAmountToCents("150.5"))
    }

    @Test
    fun surroundingSpaces_areIgnored() {
        assertEquals(500L, parseAmountToCents("  5  "))
    }

    @Test
    fun invalidInput_returnsNull() {
        assertNull(parseAmountToCents("abc"))
        assertNull(parseAmountToCents("150.999"))
        assertNull(parseAmountToCents("-5"))
        assertNull(parseAmountToCents(""))
    }
}

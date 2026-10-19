package com.bev.ledgr.data

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus

data class MonthRange(val startMillis: Long, val endMillis: Long)

fun monthRange(
    year: Int,
    month: Int,
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): MonthRange {
    val firstDay = LocalDate(year, month, 1)
    val firstDayNextMonth = firstDay.plus(1, DateTimeUnit.MONTH)
    return MonthRange(
        startMillis = firstDay.atStartOfDayIn(timeZone).toEpochMilliseconds(),
        endMillis = firstDayNextMonth.atStartOfDayIn(timeZone).toEpochMilliseconds()
    )
}
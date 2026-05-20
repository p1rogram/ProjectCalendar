package com.example.projectcalendar.data.utils

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

internal fun Long.toLocalDateTime(): LocalDateTime =
    LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())

internal fun Long.toLocalDate(): LocalDate =
    toLocalDateTime().toLocalDate()

internal fun LocalDateTime.toEpochMillis(): Long =
    atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

internal fun LocalDate.toStartOfDayMillis(): Long =
    atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

internal fun LocalDate.toNextDayStartMillis(): Long =
    plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

internal fun getMonthDays(date : LocalDate): Int {
    return date.lengthOfMonth()
}
internal fun getMonthName(date : LocalDate): String {
    return YearMonth.of(date.year, date.monthValue).month.getDisplayName(TextStyle.FULL,
        Locale.forLanguageTag("ru")
    )
}
internal fun weekOffset(date : LocalDate) : Int {
    return LocalDate.of(date.year, date.monthValue, 1)
        .dayOfWeek.value - 1
}
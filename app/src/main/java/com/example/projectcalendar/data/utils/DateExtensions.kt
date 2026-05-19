package com.example.projectcalendar.data.utils

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Конвертирует миллисекунды (из БД) в LocalDateTime
 */
internal fun Long.toLocalDateTime(): LocalDateTime =
    LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())

/**
 * Конвертирует миллисекунды (из БД) в LocalDate (отбрасывает время)
 */
internal fun Long.toLocalDate(): LocalDate =
    toLocalDateTime().toLocalDate()

/**
 * Конвертирует LocalDateTime в миллисекунды (для записи в БД)
 */
internal fun LocalDateTime.toEpochMillis(): Long =
    atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

/**
 * Конвертирует LocalDate в миллисекунды начала дня (00:00)
 * Используется для точного поиска задач/событий на конкретную дату
 */
internal fun LocalDate.toStartOfDayMillis(): Long =
    atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

/**
 * Конвертирует LocalDate в миллисекунды начала СЛЕДУЮЩЕГО дня
 * Критично для запросов диапазона:
 * SELECT * WHERE date BETWEEN start AND end
 * Чтобы захватить события до 23:59 последнего дня, end должен быть 00:00 следующего дня
 */
internal fun LocalDate.toNextDayStartMillis(): Long =
    plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
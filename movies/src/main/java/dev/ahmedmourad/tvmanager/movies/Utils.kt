package dev.ahmedmourad.tvmanager.movies

import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId

fun LocalDateTime.toMillis() = this.toInstant(OffsetDateTime.now().offset)
    .toEpochMilli()

fun localDateTime(millis: Long): LocalDateTime = LocalDateTime.ofInstant(
    Instant.ofEpochMilli(millis), ZoneId.systemDefault()
)

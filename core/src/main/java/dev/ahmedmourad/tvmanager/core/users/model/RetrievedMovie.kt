package dev.ahmedmourad.tvmanager.core.users.model

import java.time.LocalDate
import java.time.LocalDateTime

data class RetrievedMovie(
    val id: MovieId,
    val title: String,
    val createdAt: LocalDateTime,
    val releaseDate: LocalDate?,
    val seasonsCount: Double?
)

package dev.ahmedmourad.tvmanager.core.users.model

import java.time.LocalDate

data class Movie(
    val title: String,
    val releaseDate: LocalDate?,
    val seasonsCount: Double?
)

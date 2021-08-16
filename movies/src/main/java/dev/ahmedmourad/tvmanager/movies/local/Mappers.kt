package dev.ahmedmourad.tvmanager.movies.local

import dev.ahmedmourad.tvmanager.core.users.model.MovieId
import dev.ahmedmourad.tvmanager.core.users.model.RetrievedMovie
import dev.ahmedmourad.tvmanager.movies.local.entities.MovieEntity
import dev.ahmedmourad.tvmanager.movies.localDateTime
import dev.ahmedmourad.tvmanager.movies.toMillis

internal fun RetrievedMovie.toMovieEntity(): MovieEntity = MovieEntity(
    id = id.value,
    title = title,
    createdAt = createdAt.toMillis(),
    releaseDate = releaseDate?.atTime(0, 0)?.toMillis(),
    seasonsCount = seasonsCount
)

internal fun MovieEntity.toMovie(): RetrievedMovie = RetrievedMovie(
    id = MovieId(id),
    title = title,
    createdAt = localDateTime(createdAt),
    releaseDate = releaseDate?.let(::localDateTime)?.toLocalDate(),
    seasonsCount = seasonsCount
)

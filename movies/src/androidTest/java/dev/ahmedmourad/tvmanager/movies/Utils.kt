@file:JvmName("TestUtils")
package dev.ahmedmourad.tvmanager.movies

import dev.ahmedmourad.tvmanager.core.users.model.Movie
import dev.ahmedmourad.tvmanager.core.users.model.MovieId
import dev.ahmedmourad.tvmanager.core.users.model.RetrievedMovie
import java.time.LocalDateTime
import java.util.*
import kotlin.random.Random

fun randomMovies(min: Int = 5, max: Int = 25, allowNulls: Boolean = true): List<RetrievedMovie> {
    val list = mutableListOf<RetrievedMovie>()
    repeat(max - min) { index ->
        if (allowNulls) {
            list.add(randomNullsMovie(min + index.toLong()))
        } else {
            list.add(randomMovie(min + index.toLong()))
        }
    }
    return list.distinct()
}

fun randomNullsMovie(createdAt: Long) = RetrievedMovie(
    id = MovieId(UUID.randomUUID().toString()),
    createdAt = localDateTime(System.currentTimeMillis() - 100000 + createdAt),
    title = UUID.randomUUID().toString(),
    releaseDate = nullable { localDateTime(System.currentTimeMillis() - (100000..500000).random()).toLocalDate() },
    seasonsCount = nullable { (0..50).random().toDouble() }
)

fun randomMovie(createdAt: Long) = RetrievedMovie(
    id = MovieId(UUID.randomUUID().toString()),
    createdAt = localDateTime(System.currentTimeMillis() - 100000 + createdAt),
    title = UUID.randomUUID().toString(),
    releaseDate = localDateTime(System.currentTimeMillis() - (100000..500000).random()).toLocalDate(),
    seasonsCount = (0..50).random().toDouble()
)

private fun <T> nullable(generate: () -> T): T? {
    return if (Random.nextBoolean()) null else generate()
}

fun randomSimpleMovies(min: Int = 5, max: Int = 25): List<Movie> {
    return randomMovies(min, max).map(RetrievedMovie::simplify)
}

fun RetrievedMovie.simplify() = Movie(
    title = title,
    releaseDate = releaseDate,
    seasonsCount = seasonsCount
)

fun Movie.toRetrievedMovie() = RetrievedMovie(
    id = MovieId(UUID.randomUUID().toString()),
    title = title,
    createdAt = LocalDateTime.now(),
    releaseDate = releaseDate,
    seasonsCount = seasonsCount
)

package dev.ahmedmourad.tvmanager.movies.repo

import dev.ahmedmourad.tvmanager.core.users.model.RetrievedMovie

internal interface LocalDataSource {
    suspend fun findMovies(page: Int): LocalResult<List<RetrievedMovie>>
    suspend fun addMovies(movies: List<RetrievedMovie>): LocalResult<List<RetrievedMovie>>
    suspend fun addMovie(movie: RetrievedMovie): LocalResult<RetrievedMovie>
    suspend fun deleteAll(): LocalResult<Unit>
}

sealed interface LocalResult<out T> {
    data class Success<out T>(val v: T) : LocalResult<T>
    data class Error(val e: Throwable) : LocalResult<Nothing>
}

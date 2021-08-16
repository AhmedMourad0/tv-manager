package dev.ahmedmourad.tvmanager.movies.repo

import dev.ahmedmourad.tvmanager.core.users.model.Movie
import dev.ahmedmourad.tvmanager.core.users.model.RetrievedMovie
import kotlinx.coroutines.flow.Flow

internal interface RemoteDataSource {
    suspend fun addMovie(movie: Movie): RemoteResult<RetrievedMovie>
    fun findMovies(page: Int): Flow<RemoteResult<List<RetrievedMovie>>>
}

sealed interface RemoteResult<out T : Any> {
    data class Success<out T : Any>(val v: T) : RemoteResult<T>
    object NoConnection : RemoteResult<Nothing>
    data class Error(val e: Throwable) : RemoteResult<Nothing>
}

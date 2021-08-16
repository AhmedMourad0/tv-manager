package dev.ahmedmourad.tvmanager.movies.remote

import dagger.Reusable
import dev.ahmedmourad.tvmanager.core.users.model.Movie
import dev.ahmedmourad.tvmanager.core.users.model.RetrievedMovie
import dev.ahmedmourad.tvmanager.movies.repo.RemoteDataSource
import dev.ahmedmourad.tvmanager.movies.repo.RemoteResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

@Reusable
internal class RemoteDataSourceImpl @Inject constructor(
    private val service: MoviesService
) : RemoteDataSource {

    override suspend fun addMovie(movie: Movie) = withContext(Dispatchers.IO) {
        exec { service.addMovie(movie) }
    }

    override fun findMovies(page: Int): Flow<RemoteResult<List<RetrievedMovie>>> {
        return withExponentialBackoff { exec { service.findMovies(page) } }
    }
}

private suspend fun <T : Any> exec(block: suspend () -> RemoteResult<T>): RemoteResult<T> {
    return try {
        block()
    } catch (e: Exception) {
        when (e) {
            is CancellationException -> throw e
            is SocketTimeoutException,
            is SocketException,
            is IOException,
            is UnknownHostException -> RemoteResult.NoConnection
            else -> RemoteResult.Error(e)
        }
    }
}

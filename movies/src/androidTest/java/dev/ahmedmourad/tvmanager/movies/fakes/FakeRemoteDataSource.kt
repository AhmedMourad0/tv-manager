package dev.ahmedmourad.tvmanager.movies.fakes

import dev.ahmedmourad.tvmanager.core.PAGE_SIZE
import dev.ahmedmourad.tvmanager.core.users.model.Movie
import dev.ahmedmourad.tvmanager.core.users.model.RetrievedMovie
import dev.ahmedmourad.tvmanager.movies.repo.RemoteDataSource
import dev.ahmedmourad.tvmanager.movies.repo.RemoteResult
import dev.ahmedmourad.tvmanager.movies.toMillis
import dev.ahmedmourad.tvmanager.movies.toRetrievedMovie
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeRemoteDataSource : RemoteDataSource {

    var error: RemoteResult<Nothing>? = null
    var data = emptyList<RetrievedMovie>()

    override suspend fun addMovie(movie: Movie): RemoteResult<RetrievedMovie> {
        if (error != null) {
            return error!!
        }
        val m = movie.toRetrievedMovie()
        data = data + m
        return RemoteResult.Success(m)
    }

    override fun findMovies(page: Int): Flow<RemoteResult<List<RetrievedMovie>>> {
        if (error != null) {
            return flowOf(error!!)
        }
        return flowOf(RemoteResult.Success(
            data.sortedByDescending { it.createdAt.toMillis() }
                .drop((page - 1) * PAGE_SIZE)
                .take(PAGE_SIZE)
        ))
    }
}

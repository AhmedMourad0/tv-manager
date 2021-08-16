package dev.ahmedmourad.tvmanager.movies.fakes

import dev.ahmedmourad.tvmanager.core.PAGE_SIZE
import dev.ahmedmourad.tvmanager.core.users.model.Movie
import dev.ahmedmourad.tvmanager.core.users.model.RetrievedMovie
import dev.ahmedmourad.tvmanager.movies.remote.MoviesService
import dev.ahmedmourad.tvmanager.movies.repo.RemoteResult
import dev.ahmedmourad.tvmanager.movies.toMillis
import dev.ahmedmourad.tvmanager.movies.toRetrievedMovie

internal class FakeMoviesService : MoviesService {

    var data = emptyList<RetrievedMovie>()

    override suspend fun addMovie(movie: Movie): RemoteResult<RetrievedMovie> {
        val m = movie.toRetrievedMovie()
        data = data + m
        return RemoteResult.Success(m)
    }

    override suspend fun findMovies(page: Int): RemoteResult<List<RetrievedMovie>> {
        return RemoteResult.Success(
            data.sortedByDescending { it.createdAt.toMillis() }
                .drop((page - 1) * PAGE_SIZE)
                .take(PAGE_SIZE)
        )
    }
}

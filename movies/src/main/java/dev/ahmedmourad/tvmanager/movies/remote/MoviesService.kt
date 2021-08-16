package dev.ahmedmourad.tvmanager.movies.remote

import dev.ahmedmourad.tvmanager.core.users.model.Movie
import dev.ahmedmourad.tvmanager.core.users.model.RetrievedMovie
import dev.ahmedmourad.tvmanager.movies.repo.RemoteResult

internal interface MoviesService {
    suspend fun addMovie(movie: Movie): RemoteResult<RetrievedMovie>
    suspend fun findMovies(page: Int): RemoteResult<List<RetrievedMovie>>
}

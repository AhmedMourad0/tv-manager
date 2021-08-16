package dev.ahmedmourad.tvmanager.core.users

import androidx.paging.PagingSource
import dev.ahmedmourad.tvmanager.core.EagerPagingSource
import dev.ahmedmourad.tvmanager.core.users.model.Movie
import dev.ahmedmourad.tvmanager.core.users.model.RetrievedMovie
import dev.ahmedmourad.tvmanager.core.users.usecases.AddMovieResult

interface MoviesRepository {
    suspend fun addMovie(movie: Movie): AddMovieResult
    fun findMovies(): PagingSource<Int, RetrievedMovie>
    fun findMoviesEager(): EagerPagingSource<Int, RetrievedMovie>
}

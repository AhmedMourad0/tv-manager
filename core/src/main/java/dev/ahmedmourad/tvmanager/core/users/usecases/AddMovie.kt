package dev.ahmedmourad.tvmanager.core.users.usecases

import dev.ahmedmourad.tvmanager.core.users.MoviesRepository
import dagger.Reusable
import dev.ahmedmourad.tvmanager.core.users.model.Movie
import dev.ahmedmourad.tvmanager.core.users.model.RetrievedMovie
import javax.inject.Inject

interface AddMovie {
    suspend fun execute(movie: Movie): AddMovieResult
}

@Reusable
internal class AddMovieImpl @Inject constructor(
    private val repository: MoviesRepository
) : AddMovie {
    override suspend fun execute(movie: Movie): AddMovieResult {
        return repository.addMovie(movie)
    }
}

sealed interface AddMovieResult {
    data class Success(val v: RetrievedMovie) : AddMovieResult
    object NoConnection : AddMovieResult
    data class Error(val e: Throwable) : AddMovieResult
}

package dev.ahmedmourad.tvmanager.movies.repo

import androidx.paging.PagingSource
import dagger.Reusable
import dev.ahmedmourad.tvmanager.core.EagerPagingSource
import dev.ahmedmourad.tvmanager.core.users.MoviesRepository
import dev.ahmedmourad.tvmanager.core.users.model.*
import dev.ahmedmourad.tvmanager.core.users.usecases.*
import dev.ahmedmourad.tvmanager.movies.repo.paging.RemoteOnlyPagingSource
import dev.ahmedmourad.tvmanager.movies.repo.paging.TiledPagingSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@Reusable
internal class MoviesRepositoryImpl @Inject constructor(
    private val local: LocalDataSource,
    private val remote: RemoteDataSource
) : MoviesRepository {

    override suspend fun addMovie(movie: Movie) = withContext(Dispatchers.IO) {
        when (val result = remote.addMovie(movie)) {
            is RemoteResult.Success -> cache(result.v)
            RemoteResult.NoConnection -> AddMovieResult.NoConnection
            is RemoteResult.Error -> {
                Timber.e(result.e)
                AddMovieResult.Error(result.e)
            }
        }
    }

    private suspend fun cache(movie: RetrievedMovie) = withContext(Dispatchers.IO) {
        when (val result = local.addMovie(movie)) {
            is LocalResult.Success -> AddMovieResult.Success(movie)
            is LocalResult.Error -> {
                Timber.e(result.e)
                AddMovieResult.Error(result.e)
            }
        }
    }

    override fun findMovies(): PagingSource<Int, RetrievedMovie> {
        return TiledPagingSource(
            local,
            remote
        )
    }

    override fun findMoviesEager(): EagerPagingSource<Int, RetrievedMovie> {
        return RemoteOnlyPagingSource(
            local,
            remote
        )
    }
}

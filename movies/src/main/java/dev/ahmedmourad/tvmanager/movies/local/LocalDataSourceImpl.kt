package dev.ahmedmourad.tvmanager.movies.local

import dev.ahmedmourad.tvmanager.movies.local.daos.MoviesDao
import dagger.Reusable
import dev.ahmedmourad.tvmanager.core.PAGE_SIZE
import dev.ahmedmourad.tvmanager.core.users.model.RetrievedMovie
import dev.ahmedmourad.tvmanager.movies.local.entities.MovieEntity
import dev.ahmedmourad.tvmanager.movies.repo.LocalDataSource
import dev.ahmedmourad.tvmanager.movies.repo.LocalResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@Reusable
internal class LocalDataSourceImpl @Inject constructor(
    private val dao: MoviesDao
) : LocalDataSource {

    override suspend fun findMovies(page: Int) = withContext(Dispatchers.IO) {
        exec {
            dao.findMovies(
                (page - 1) * PAGE_SIZE,
                PAGE_SIZE
            ).map(MovieEntity::toMovie)
        }
    }

    override suspend fun addMovies(movies: List<RetrievedMovie>) = withContext(Dispatchers.IO) {
        exec {
            dao.insert(movies.map(RetrievedMovie::toMovieEntity))
            movies
        }
    }

    override suspend fun addMovie(movie: RetrievedMovie) = withContext(Dispatchers.IO) {
        exec {
            dao.insert(movie.toMovieEntity())
            movie
        }
    }

    override suspend fun deleteAll() = withContext(Dispatchers.IO) {
        exec {
            dao.deleteAll()
            Unit
        }
    }
}

private suspend fun <T> exec(block: suspend () -> T): LocalResult<T> {
    return try {
        LocalResult.Success(block())
    } catch (e: Exception) {
        when (e) {
            is CancellationException -> throw e
            else -> LocalResult.Error(e)
        }
    }
}

package dev.ahmedmourad.tvmanager.movies.fakes

import dev.ahmedmourad.tvmanager.core.PAGE_SIZE
import dev.ahmedmourad.tvmanager.core.users.model.RetrievedMovie
import dev.ahmedmourad.tvmanager.movies.repo.LocalDataSource
import dev.ahmedmourad.tvmanager.movies.repo.LocalResult
import dev.ahmedmourad.tvmanager.movies.toMillis
import kotlinx.coroutines.CancellationException
import java.lang.Exception

class FakeLocalDataSource : LocalDataSource {

    var error: LocalResult<Nothing>? = null
    var data = mutableListOf<RetrievedMovie>()

    override suspend fun findMovies(page: Int): LocalResult<List<RetrievedMovie>> {
        if (error != null) {
            return error!!
        }
        return exec {
            data.sortedByDescending { it.createdAt.toMillis() }
                .drop((page - 1) * PAGE_SIZE)
                .take(PAGE_SIZE)
        }
    }

    override suspend fun addMovies(movies: List<RetrievedMovie>): LocalResult<List<RetrievedMovie>> {
        if (error != null) {
            return error!!
        }
        data.addAll(movies)
        return LocalResult.Success(movies)
    }

    override suspend fun addMovie(movie: RetrievedMovie): LocalResult<RetrievedMovie> {
        if (error != null) {
            return error!!
        }
        data.add(movie)
        return LocalResult.Success(movie)
    }

    override suspend fun deleteAll(): LocalResult<Unit> {
        if (error != null) {
            return error!!
        }
        data.clear()
        return LocalResult.Success(Unit)
    }
}

private suspend fun <T> exec(block: suspend () -> T): LocalResult<T> {
    return try {
        LocalResult.Success(block())
    } catch (e: Exception) {
        if (e is CancellationException) {
            throw e
        } else {
            LocalResult.Error(e)
        }
    }
}

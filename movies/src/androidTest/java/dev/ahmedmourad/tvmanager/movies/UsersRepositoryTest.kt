package dev.ahmedmourad.tvmanager.movies

import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.ahmedmourad.tvmanager.movies.fakes.FakeLocalDataSource
import dev.ahmedmourad.tvmanager.movies.fakes.FakeRemoteDataSource
import dev.ahmedmourad.tvmanager.core.users.MoviesRepository
import dev.ahmedmourad.tvmanager.core.users.model.Movie
import dev.ahmedmourad.tvmanager.core.users.usecases.AddMovieResult
import dev.ahmedmourad.tvmanager.movies.repo.MoviesRepositoryImpl
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class UsersRepositoryTest {

    private lateinit var local: FakeLocalDataSource
    private lateinit var remote: FakeRemoteDataSource
    private lateinit var repo: MoviesRepository

    @Before
    fun setup() {
        local = FakeLocalDataSource()
        remote = FakeRemoteDataSource()
        repo = MoviesRepositoryImpl(
            local,
            remote
        )
    }

    @Test
    fun addMovie_addsMovieToRemoteDataSource() = runBlocking {

        val data = randomMovies(5, 50, false)
        local.data = data.take(20).toMutableList()
        remote.data = data

        suspend fun go(movie: Movie) {
            val added = repo.addMovie(movie)
            assert(added is AddMovieResult.Success)
            added as AddMovieResult.Success
            assertEquals(movie, added.v.simplify())
            assertEquals(movie, remote.data.last().simplify())
        }

        go(randomMovie((1000L..100000L).random()).simplify())
        go(randomMovie((1000L..100000L).random()).simplify())
        go(randomMovie((1000L..100000L).random()).simplify())
    }
}

package dev.ahmedmourad.tvmanager.movies

import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.ahmedmourad.tvmanager.core.PAGE_SIZE
import dev.ahmedmourad.tvmanager.core.users.model.Movie
import dev.ahmedmourad.tvmanager.movies.fakes.FakeMoviesService
import dev.ahmedmourad.tvmanager.movies.remote.RemoteDataSourceImpl
import dev.ahmedmourad.tvmanager.movies.repo.RemoteDataSource
import dev.ahmedmourad.tvmanager.movies.repo.RemoteResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class RemoteDataSourceTests {

    private lateinit var service: FakeMoviesService
    private lateinit var source: RemoteDataSource

    @Before
    fun setup() {
        service = FakeMoviesService()
        source = RemoteDataSourceImpl(service)
    }

    @Test
    fun findMovies_returnsTheMoviesOfTheSelectedPage() = runBlocking {
        val data = randomMovies(5, 50, false)
        service.data = data
        suspend fun go(page: Int) {
            val fetched = source.findMovies(page).first()
            assertTrue(fetched is RemoteResult.Success)
            fetched as RemoteResult.Success
            assertEquals(
                data.sortedByDescending { it.createdAt.toMillis() }
                    .drop((page - 1) * PAGE_SIZE)
                    .take(PAGE_SIZE),
                fetched.v
            )
        }

        go(1)
        go(2)
        go(3)
    }

    @Test
    fun addMovie_addsMovieToTheService() = runBlocking {
        val data = randomMovies(5, 25, false)
        service.data = data
        suspend fun go(movie: Movie) {
            val added = source.addMovie(movie)
            assertTrue(added is RemoteResult.Success)
            added as RemoteResult.Success
            assertEquals(movie, added.v.simplify())
            assertEquals(movie, service.data.last().simplify())
        }

        go(randomMovie((1000L..100000L).random()).simplify())
        go(randomMovie((1000L..100000L).random()).simplify())
        go(randomMovie((1000L..100000L).random()).simplify())
    }
}


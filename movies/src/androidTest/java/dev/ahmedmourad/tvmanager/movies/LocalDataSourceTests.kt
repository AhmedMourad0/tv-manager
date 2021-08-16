package dev.ahmedmourad.tvmanager.movies

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.ahmedmourad.tvmanager.core.PAGE_SIZE
import dev.ahmedmourad.tvmanager.core.users.model.RetrievedMovie
import dev.ahmedmourad.tvmanager.movies.local.LocalDataSourceImpl
import dev.ahmedmourad.tvmanager.movies.local.MoviesDatabase
import dev.ahmedmourad.tvmanager.movies.local.entities.MovieEntity
import dev.ahmedmourad.tvmanager.movies.local.toMovie
import dev.ahmedmourad.tvmanager.movies.repo.LocalDataSource
import dev.ahmedmourad.tvmanager.movies.repo.LocalResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class LocalDataSourceTests {

    private lateinit var db: MoviesDatabase
    private lateinit var source: LocalDataSource

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, MoviesDatabase::class.java).build()
        source = LocalDataSourceImpl(db.usersDao())
    }

    @Test
    @Throws(Exception::class)
    fun findMovies_returnsTheSelectedPageOfMovies() = runBlocking {

        assertEquals(0, db.usersDao().findAllMoviesForTesting().first().size)
        val movies = randomMovies(5, 50)
        assertEquals(LocalResult.Success(movies), source.addMovies(movies))

        suspend fun go(page: Int) {
            val retrieved = source.findMovies(page)
            assert(retrieved is LocalResult.Success)
            retrieved as LocalResult.Success
            assertEquals(
                movies.sortedByDescending { it.createdAt.toMillis() }
                    .drop((page - 1) * PAGE_SIZE)
                    .take(PAGE_SIZE),
                retrieved.v.sortedByDescending { it.createdAt.toMillis() }
            )
        }

        go(3)
        go(7)
        go(4)
    }

    @Test
    @Throws(Exception::class)
    fun addMovies_insertsTheMoviesToTheDatabase() = runBlocking {

        assertEquals(0, db.usersDao().findAllMoviesForTesting().first().size)
        val movies = randomMovies(5, 25)
        assertEquals(LocalResult.Success(movies), source.addMovies(movies))

        suspend fun go(movies: List<RetrievedMovie>) {
            assert(source.addMovies(movies) is LocalResult.Success)
            val retrieved = db.usersDao().findAllMoviesForTesting().first().map(MovieEntity::toMovie)
            assertTrue(retrieved.containsAll(movies))
        }

        go(randomMovies(PAGE_SIZE, PAGE_SIZE))
        go(randomMovies(5, 25))
    }

    @Test
    @Throws(Exception::class)
    fun addMovie_insertsTheMovieToTheDatabase() = runBlocking {

        assertEquals(0, db.usersDao().findAllMoviesForTesting().first().size)
        val movies = randomMovies(5, 25)
        assertEquals(LocalResult.Success(movies), source.addMovies(movies))

        suspend fun go(movie: RetrievedMovie) {
            assert(source.addMovie(movie) is LocalResult.Success)
            val retrieved = db.usersDao().findAllMoviesForTesting().first().map(MovieEntity::toMovie)
            assertTrue(retrieved.contains(movie))
        }

        go(randomMovie((1000L..10000L).random()))
        go(randomMovie((1000L..10000L).random()))
        go(randomMovie((1000L..10000L).random()))
    }

    @Test
    @Throws(Exception::class)
    fun deleteAll_deletesAllMovies() = runBlocking {

        assertEquals(0, db.usersDao().findAllMoviesForTesting().first().size)
        val movies = randomMovies(5, 25)
        assertEquals(LocalResult.Success(movies), source.addMovies(movies))

        assert(source.deleteAll() is LocalResult.Success)
        val retrieved = db.usersDao()
            .findAllMoviesForTesting()
            .first()
            .map(MovieEntity::toMovie)
        assertEquals(0, retrieved.size)
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }
}

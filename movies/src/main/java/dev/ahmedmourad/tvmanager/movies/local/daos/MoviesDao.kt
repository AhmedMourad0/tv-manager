package dev.ahmedmourad.tvmanager.movies.local.daos

import androidx.annotation.VisibleForTesting
import androidx.room.*
import dev.ahmedmourad.tvmanager.movies.local.LocalContract.Movie
import dev.ahmedmourad.tvmanager.movies.local.entities.MovieEntity
import kotlinx.coroutines.flow.Flow

@Dao
internal abstract class MoviesDao {

    @Query(
        """
        SELECT
            ${Movie.COL_ID},
            ${Movie.COL_TITLE},
            ${Movie.COL_CREATED_AT},
            ${Movie.COL_RELEASE_DATE},
            ${Movie.COL_SEASONS_COUNT}
        FROM
            ${Movie.TABLE_NAME}
        ORDER BY
            ${Movie.COL_CREATED_AT} DESC
        LIMIT
            :limit
        OFFSET
            :skip;
    """
    )
    abstract suspend fun findMovies(skip: Int, limit: Int): List<MovieEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(movie: MovieEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(movies: List<MovieEntity>)

    @Query("DELETE FROM ${Movie.TABLE_NAME};")
    abstract suspend fun deleteAll(): Int

    @Query("""
        SELECT
            ${Movie.COL_ID},
            ${Movie.COL_TITLE},
            ${Movie.COL_CREATED_AT},
            ${Movie.COL_RELEASE_DATE},
            ${Movie.COL_SEASONS_COUNT}
        FROM
            ${Movie.TABLE_NAME}
        ORDER BY ${Movie.COL_CREATED_AT} DESC
    """)
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    internal abstract fun findAllMoviesForTesting(): Flow<List<MovieEntity>>
}

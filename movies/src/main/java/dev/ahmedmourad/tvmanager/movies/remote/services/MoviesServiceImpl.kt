package dev.ahmedmourad.tvmanager.movies.remote.services

import CreateMovieMutation
import MoviesQuery
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.toInput
import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.exception.ApolloException
import dagger.Reusable
import dev.ahmedmourad.tvmanager.core.PAGE_SIZE
import dev.ahmedmourad.tvmanager.core.users.model.Movie
import dev.ahmedmourad.tvmanager.core.users.model.MovieId
import dev.ahmedmourad.tvmanager.core.users.model.RetrievedMovie
import dev.ahmedmourad.tvmanager.movies.remote.MoviesService
import dev.ahmedmourad.tvmanager.movies.repo.RemoteResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import type.*
import javax.inject.Inject

@Reusable
internal class MoviesServiceImpl @Inject constructor(
    private val client: ApolloClient
) : MoviesService {

    override suspend fun addMovie(movie: Movie) = withContext(Dispatchers.IO) {
        try {
            client.mutate(CreateMovieMutation(movie.toCreateMovieInput()))
                .await()
                .data
                ?.createMovie
                ?.movie
                ?.toMovie()
                ?.let { RemoteResult.Success(it) }
                ?: RemoteResult.Error(IllegalStateException("null data"))
        } catch (e : ApolloException) {
            Timber.e(e)
            RemoteResult.NoConnection
        }
    }

    override suspend fun findMovies(page: Int) = withContext(Dispatchers.IO) {
        try {
            val query = MoviesQuery(
                order = listOf(MovieOrder.CREATEDAT_DESC).toInput(),
                skip = ((page - 1) * PAGE_SIZE).toInput(),
                first = PAGE_SIZE.toInput()
            )
            client.query(query)
                .await()
                .data
                ?.movies
                ?.edges
                ?.mapNotNull(MoviesQuery.Edge?::toMovie)
				?.sortedByDescending(RetrievedMovie::createdAt)
                ?.let { RemoteResult.Success(it) }
                ?: RemoteResult.Error(IllegalStateException("null data"))
        } catch (e : ApolloException) {
            Timber.e(e)
            RemoteResult.NoConnection
        }
    }
}

private fun MoviesQuery.Edge?.toMovie(): RetrievedMovie? {
    val node = this?.node ?: return null
    return RetrievedMovie(
        id = MovieId(node.id),
        title = node.title,
        createdAt = node.createdAt,
        releaseDate = node.releaseDate?.toLocalDate(),
        seasonsCount = node.seasons
    )
}

private fun Movie.toCreateMovieInput(): CreateMovieInput {
    return CreateMovieInput(
        fields = CreateMovieFieldsInput(
            title = this.title,
            releaseDate = this.releaseDate?.atTime(0, 0).toInput(),
            seasons = this.seasonsCount.toInput()
        ).toInput()
    )
}

private fun CreateMovieMutation.Movie.toMovie(): RetrievedMovie {
    return RetrievedMovie(
        id = MovieId(this.id),
        title = this.title,
        createdAt = this.createdAt,
        releaseDate = this.releaseDate?.toLocalDate(),
        seasonsCount = this.seasons
    )
}

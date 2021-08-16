package dev.ahmedmourad.tvmanager.core.users.usecases

import androidx.paging.PagingSource
import dev.ahmedmourad.tvmanager.core.users.MoviesRepository
import dagger.Reusable
import dev.ahmedmourad.tvmanager.core.EagerPagingSource
import dev.ahmedmourad.tvmanager.core.users.model.RetrievedMovie
import javax.inject.Inject

interface FindMovies {
    fun tiled(): PagingSource<Int, RetrievedMovie>
    fun remote(): EagerPagingSource<Int, RetrievedMovie>
}

@Reusable
internal class FindMoviesImpl @Inject constructor(
    private val repository: MoviesRepository
) : FindMovies {
    override fun tiled(): PagingSource<Int, RetrievedMovie> {
        return repository.findMovies()
    }
    override fun remote(): EagerPagingSource<Int, RetrievedMovie> {
        return repository.findMoviesEager()
    }
}

package dev.ahmedmourad.tvmanager.core.users.di

import dagger.Binds
import dagger.Module
import dagger.Reusable
import dev.ahmedmourad.tvmanager.core.users.usecases.*

@Module
internal interface MoviesBindingsModule {

    @Binds
    @Reusable
    fun bindAddMovie(
        impl: AddMovieImpl
    ): AddMovie

    @Binds
    @Reusable
    fun bindFindMovies(
        impl: FindMoviesImpl
    ): FindMovies
}

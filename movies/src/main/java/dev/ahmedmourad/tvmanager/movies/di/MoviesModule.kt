package dev.ahmedmourad.tvmanager.movies.di

import dagger.Binds
import dagger.Module
import dagger.Reusable
import dev.ahmedmourad.tvmanager.core.users.MoviesRepository
import dev.ahmedmourad.tvmanager.movies.local.di.LocalBindingsModule
import dev.ahmedmourad.tvmanager.movies.local.di.LocalProvidersModule
import dev.ahmedmourad.tvmanager.movies.remote.di.RemoteBindingsModule
import dev.ahmedmourad.tvmanager.movies.remote.di.RemoteProvidersModule
import dev.ahmedmourad.tvmanager.movies.repo.MoviesRepositoryImpl

@Module(includes = [
    LocalBindingsModule::class,
    LocalProvidersModule::class,
    RemoteBindingsModule::class,
    RemoteProvidersModule::class,
    MoviesBindingsModule::class
])
interface MoviesModule

@Module
internal interface MoviesBindingsModule {
    @Binds
    @Reusable
    fun bindMoviesRepository(
        impl: MoviesRepositoryImpl
    ): MoviesRepository
}

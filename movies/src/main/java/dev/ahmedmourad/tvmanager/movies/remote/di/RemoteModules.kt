package dev.ahmedmourad.tvmanager.movies.remote.di

import android.content.Context
import com.apollographql.apollo.ApolloClient
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dev.ahmedmourad.tvmanager.movies.remote.MoviesService
import dev.ahmedmourad.tvmanager.movies.remote.RemoteDataSourceImpl
import dev.ahmedmourad.tvmanager.movies.remote.services.MoviesServiceImpl
import dev.ahmedmourad.tvmanager.movies.remote.services.createApolloClient
import dev.ahmedmourad.tvmanager.movies.repo.RemoteDataSource
import javax.inject.Singleton

@Module
internal interface RemoteBindingsModule {
    @Binds
    @Reusable
    fun bindRemoteDataSource(
        impl: RemoteDataSourceImpl
    ): RemoteDataSource

    @Binds
    @Reusable
    fun bindMoviesService(
        impl: MoviesServiceImpl
    ): MoviesService
}

@Module
internal object RemoteProvidersModule {
    @Provides
    @Singleton
    @JvmStatic
    fun provideApolloClient(context: Context): ApolloClient {
        return createApolloClient(context)
    }
}

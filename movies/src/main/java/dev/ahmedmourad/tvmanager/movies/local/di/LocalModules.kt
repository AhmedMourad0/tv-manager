package dev.ahmedmourad.tvmanager.movies.local.di

import android.content.Context
import dev.ahmedmourad.tvmanager.movies.local.LocalDataSourceImpl
import dev.ahmedmourad.tvmanager.movies.local.MoviesDatabase
import dev.ahmedmourad.tvmanager.movies.local.daos.MoviesDao
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dev.ahmedmourad.tvmanager.movies.repo.LocalDataSource
import javax.inject.Singleton

@Module
internal interface LocalBindingsModule {
    @Binds
    @Reusable
    fun bindLocalDataSource(
        impl: LocalDataSourceImpl
    ): LocalDataSource
}

@Module
internal object LocalProvidersModule {

    @Provides
    @Singleton
    @JvmStatic
    fun provideMoviesDatabase(appCtx: Context): MoviesDatabase {
        return MoviesDatabase.getInstance(appCtx)
    }

    @Provides
    @Reusable
    @JvmStatic
    fun provideMoviesDao(db: MoviesDatabase): MoviesDao {
        return db.usersDao()
    }
}

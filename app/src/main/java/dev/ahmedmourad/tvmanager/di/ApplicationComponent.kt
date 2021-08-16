package dev.ahmedmourad.tvmanager.di

import dagger.Component
import dev.ahmedmourad.tvmanager.common.MainActivity
import dev.ahmedmourad.tvmanager.core.di.CoreModule
import dev.ahmedmourad.tvmanager.movies.add.AddMovieFragment
import dev.ahmedmourad.tvmanager.movies.di.MoviesModule
import dev.ahmedmourad.tvmanager.movies.find.FindMoviesFragment
import javax.inject.Singleton

@Component(modules = [
    CoreModule::class,
    MoviesModule::class,
    AppModule::class
])
@Singleton
internal interface ApplicationComponent {
    fun inject(target: MainActivity)
    fun inject(target: FindMoviesFragment)
    fun inject(target: AddMovieFragment)
}

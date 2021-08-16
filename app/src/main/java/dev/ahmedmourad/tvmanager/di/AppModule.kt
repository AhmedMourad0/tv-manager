package dev.ahmedmourad.tvmanager.di

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dev.ahmedmourad.tvmanager.common.AssistedViewModelFactory
import dev.ahmedmourad.tvmanager.common.MainViewModel
import dev.ahmedmourad.tvmanager.movies.add.AddMoviesViewModel
import dev.ahmedmourad.tvmanager.movies.find.FindMoviesViewModel

@Module(includes = [ContextModule::class, AppBindingsModule::class])
interface AppModule

@Module
interface AppBindingsModule {

    @Binds
    fun bindMainViewModel(
        impl: MainViewModel.Factory
    ): AssistedViewModelFactory<MainViewModel>

    @Binds
    fun bindFindMoviesViewModel(
        impl: FindMoviesViewModel.Factory
    ): AssistedViewModelFactory<FindMoviesViewModel>

    @Binds
    fun bindAddMoviesViewModel(
        impl: AddMoviesViewModel.Factory
    ): AssistedViewModelFactory<AddMoviesViewModel>
}

@Module
class ContextModule(private val appCtx: Context) {
    @Provides
    fun provideAppContext(): Context {
        return appCtx
    }
}

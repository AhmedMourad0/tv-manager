package dev.ahmedmourad.tvmanager.core.di

import dev.ahmedmourad.tvmanager.core.users.di.MoviesBindingsModule
import dagger.Module

@Module(includes = [
    MoviesBindingsModule::class
])
interface CoreModule

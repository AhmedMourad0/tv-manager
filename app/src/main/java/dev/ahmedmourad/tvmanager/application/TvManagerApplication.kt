package dev.ahmedmourad.tvmanager.application

import androidx.multidex.MultiDexApplication
import dev.ahmedmourad.tvmanager.di.ApplicationComponent
import dev.ahmedmourad.tvmanager.di.ContextModule
import dev.ahmedmourad.tvmanager.di.DaggerApplicationComponent
import dev.ahmedmourad.tvmanager.di.DaggerComponentProvider
import timber.log.Timber

@Suppress("unused")
internal class TvManagerApplication : MultiDexApplication(), DaggerComponentProvider {
    override val component: ApplicationComponent = DaggerApplicationComponent.builder()
        .contextModule(ContextModule(this))
        .build()

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}

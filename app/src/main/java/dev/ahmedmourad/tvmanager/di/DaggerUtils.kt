package dev.ahmedmourad.tvmanager.di

import android.content.Context

internal interface DaggerComponentProvider {
    val component: ApplicationComponent
}

internal val Context.injector get() = (this.applicationContext as DaggerComponentProvider).component

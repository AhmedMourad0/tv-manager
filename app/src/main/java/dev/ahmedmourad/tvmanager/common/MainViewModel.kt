package dev.ahmedmourad.tvmanager.common

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.Reusable
import javax.inject.Inject

class MainViewModel(
        private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    @Reusable
    class Factory @Inject constructor() : AssistedViewModelFactory<MainViewModel> {
        override fun invoke(handle: SavedStateHandle): MainViewModel {
            return MainViewModel(handle)
        }
    }

    companion object {
        fun defaultArgs(): Bundle? = null
    }
}

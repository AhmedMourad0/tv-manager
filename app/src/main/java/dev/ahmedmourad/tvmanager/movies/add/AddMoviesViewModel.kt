package dev.ahmedmourad.tvmanager.movies.add

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.Reusable
import dev.ahmedmourad.tvmanager.common.AssistedViewModelFactory
import dev.ahmedmourad.tvmanager.core.users.model.Movie
import dev.ahmedmourad.tvmanager.core.users.model.RetrievedMovie
import dev.ahmedmourad.tvmanager.core.users.usecases.AddMovie
import dev.ahmedmourad.tvmanager.core.users.usecases.AddMovieResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class AddMoviesViewModel(
    private val addMovie: AddMovie
) : ViewModel() {

    private val _title = MutableStateFlow<String?>(null)
    private val _releaseDate = MutableStateFlow<LocalDate?>(null)
    private val _seasonsCount = MutableStateFlow<Double?>(null)

    val title: StateFlow<String?> = _title
    val releaseDate: StateFlow<LocalDate?> = _releaseDate
    val seasonsCount: StateFlow<Double?> = _seasonsCount

    val canAdd = title.map { it != null }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = false
    )

    val addMovieState = MutableStateFlow<AddMovieState?>(null)

    fun onTitleChanged(new: String?) {
        _title.value = new.takeUnless(String?::isNullOrBlank)
    }

    fun onReleaseDateChanged(new: LocalDate?) {
        _releaseDate.value = new
    }

    fun onSeasonsCountChanged(new: Double?) {
        _seasonsCount.value = new
    }

    fun onAddMovie() {
        viewModelScope.launch {
            val movie = Movie(title.value!!, releaseDate.value, seasonsCount.value)
            addMovieState.value = when (val result = addMovie.execute(movie)) {
                is AddMovieResult.Success -> AddMovieState.Success(result.v)
                is AddMovieResult.Error -> AddMovieState.Error(result.e)
                AddMovieResult.NoConnection -> AddMovieState.NoConnection
            }
        }
    }

    sealed interface AddMovieState {
        data class Success(val item: RetrievedMovie) : AddMovieState
        object NoConnection : AddMovieState
        data class Error(val e: Throwable) : AddMovieState
    }

    @Reusable
    class Factory @Inject constructor(
        private val addMovie: AddMovie
    ) : AssistedViewModelFactory<AddMoviesViewModel> {
        override fun invoke(handle: SavedStateHandle): AddMoviesViewModel {
            return AddMoviesViewModel(addMovie)
        }
    }

    companion object {
        fun defaultArgs(): Bundle? = null
    }
}

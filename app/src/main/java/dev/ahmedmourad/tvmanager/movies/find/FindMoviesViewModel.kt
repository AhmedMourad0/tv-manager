package dev.ahmedmourad.tvmanager.movies.find

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import dagger.Reusable
import dev.ahmedmourad.tvmanager.common.AssistedViewModelFactory
import dev.ahmedmourad.tvmanager.core.EagerPagingSource
import dev.ahmedmourad.tvmanager.core.PAGE_SIZE
import dev.ahmedmourad.tvmanager.core.users.model.RetrievedMovie
import dev.ahmedmourad.tvmanager.core.users.usecases.FindMovies
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

//By combining these values into a single immutable class, we assert
// that changes to them are done atomically, triggering only a single
// event instead of one per property
private data class PagingState(
    val remoteSource: EagerPagingSource<Int, RetrievedMovie>,
    //This becomes true after the user clicks our fab or swipes to refresh, and it stays true
    val enforceNewData: Boolean
)

@OptIn(ExperimentalCoroutinesApi::class)
//The strategy for loading data in this view model is as follows:
// We start by relying on a TiledPagingSource, which fetches data
// from cache, and switches to remote data when cache is consumes,
// it also stores remote data in cache when fetched.
//
// We also simultaneously start loading a page of remote data using
// an EagerDataSource
//
// All remote data fetching uses an exponential backoff retry strategy
//
// When the page of the eager source is ready, we show a fab to the user
// indicating newer data is available, when the user clicks the fab, we switch
// from the TiledPagingSource to the EagerPagingSource
//
// The EagerDataSource goes on and fetches other future pages remotes while also
// storing them in cache for offline usage
//
// This's not a perfect implementation of the strategy as the eagerly fetched data,
// is not compared with the current available data before showing the fab
// There's also the fact that the eagerly loaded data becomes outdated after
// a while if not used
//
// So a better strategy would compare the data before showing the fab to the user and
// also periodically load other data eagerly to either reshow the fab or refresh the
// outdated eagerly-loaded data
//
// But this should be sufficient for the purposes of this task
class FindMoviesViewModel(
    private val findMovies: FindMovies
) : ViewModel() {

    // If the returned result is the same as the one already stored in "state",
    // no items are emitted (since state flows are distinctUntilChanged), so we
    // use this flow to signal that the loading has finished, no matter the result
    val endRefresh = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    //This signals the list to clear its items
    val clearList = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private val pagingState = MutableStateFlow(
        PagingState(
            remoteSource = findMovies.remote(),
            enforceNewData = false
        )
    )

    //Used in hiding and showing the newer data fab
    //This becomes true when the remoteSource has finished loading,
    // then it becomes false and stays false
    // It could be modified to periodically load data and also
    // compare remotely loaded data with cached data, but this should be
    // sufficient for this task
    val isNewerDataAvailable = pagingState.flatMapLatest { state ->
        if (state.enforceNewData) {
            flowOf(false)
        } else {
            pagingState.value.remoteSource.isFirstPageReady()
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = false
    )

    private val tiled = pager(findMovies.tiled())
        .cachedIn(viewModelScope)
        .map<PagingData<RetrievedMovie>, State> {
            State.Data(it)
        }.catch { e ->
            emit(State.Error(e))
        }.onEach {
            endRefresh.tryEmit(Unit)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = State.Loading
        )

    private val remote = pagingState.distinctUntilChangedBy {
        //This only changes when the remote source changes, this is
        // to prevent recreating the source when enforceNewData becomes true
        it.remoteSource
    }.flatMapLatest { state ->
        pager(state.remoteSource)
    }.cachedIn(viewModelScope)
        .map<PagingData<RetrievedMovie>, State> {
            State.Data(it)
        }.catch { e ->
            emit(State.Error(e))
        }.onEach {
            endRefresh.tryEmit(Unit)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = State.Loading
        )

    val state = pagingState.distinctUntilChangedBy {
        //This only cares about enforceNewData, the refreshing of the data is done
        // by swapping the remote source
        //The sole purpose of this is to switch from tiled to remote when
        // enforceNewData becomes true
        it.enforceNewData
    }.flatMapLatest { state ->
        if (state.enforceNewData) remote else tiled
    }.onEach {
        endRefresh.tryEmit(Unit)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = State.Loading
    )

    init {
        //Initiate the loading of the first page in the remote source
        viewModelScope.launch {
            pagingState.value.remoteSource.initiate()
        }
    }

    fun enforceNewerData() {
        pagingState.value = pagingState.value.copy(enforceNewData = true)
    }

    fun onRefresh() {
        if (pagingState.value.enforceNewData) {
            //We only want to swap the remoteSource (refresh) if enforceNewData
            // is already true
            pagingState.value = PagingState(
                remoteSource = findMovies.remote(),
                enforceNewData = true
            )
        } else {
            enforceNewerData()
        }
    }

    private fun pager(
        source: PagingSource<Int, RetrievedMovie>
    ): Flow<PagingData<RetrievedMovie>> {
        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = false),
            pagingSourceFactory = { source }
        ).flow
    }

    sealed interface State {
        data class Data(val data: PagingData<RetrievedMovie>) : State
        object Loading : State
        object NoConnection : State
        data class Error(val e: Throwable) : State
    }

    @Reusable
    class Factory @Inject constructor(
        private val findMovies: FindMovies
    ) : AssistedViewModelFactory<FindMoviesViewModel> {
        override fun invoke(handle: SavedStateHandle): FindMoviesViewModel {
            return FindMoviesViewModel(findMovies)
        }
    }

    companion object {
        fun defaultArgs(): Bundle? = null
    }
}

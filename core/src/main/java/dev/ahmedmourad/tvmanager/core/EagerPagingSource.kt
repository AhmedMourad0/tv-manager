package dev.ahmedmourad.tvmanager.core

import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow

/**
 * A Paging Source that eagerly loads the first page of remote data
 */
abstract class EagerPagingSource<Key : Any, Value : Any> : PagingSource<Key, Value>() {
    abstract suspend fun initiate()
    abstract fun isFirstPageReady(): Flow<Boolean>
}

package dev.ahmedmourad.tvmanager.movies.repo.paging

import androidx.paging.PagingState
import dev.ahmedmourad.tvmanager.core.EagerPagingSource
import dev.ahmedmourad.tvmanager.core.NoInternetConnectionException
import dev.ahmedmourad.tvmanager.core.users.model.RetrievedMovie
import dev.ahmedmourad.tvmanager.movies.repo.LocalDataSource
import dev.ahmedmourad.tvmanager.movies.repo.RemoteDataSource
import dev.ahmedmourad.tvmanager.movies.repo.RemoteResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*

@Suppress("EqualsOrHashCode")
/**
 * This's an EagerPagingSource, it eagerly loads the first page of remote
 * data when initiate is called.
 * Although this paging source never loads data from cache,
 * it does store the data it fetches in cache and invalidate
 * the cache when appropriate
 */
internal class RemoteOnlyPagingSource(
    private val local: LocalDataSource,
    private val remote: RemoteDataSource
) : EagerPagingSource<Int, RetrievedMovie>() {

    private val firstPage = MutableStateFlow<LoadResult<Int, RetrievedMovie>?>(null)

    //Load the first page remotely
    override suspend fun initiate() = withContext(Dispatchers.IO) {
        firstPage.value = fromRemote(1)
    }

    //This's can never emit false, but it emits true if/when the items are loaded
    override fun isFirstPageReady() = firstPage.filter { it != null }.map { true }

    override suspend fun load(params: LoadParams<Int>) = withContext(Dispatchers.IO) {
        // Start refresh at page 1 if undefined.
        val nextPage = params.key ?: 1
        if (nextPage == 1) {
            cache(firstPage.value ?: fromRemote(1), invalidate = true)
        } else {
            firstPage.value = null
            cache(fromRemote(nextPage), invalidate = false)
        }
    }

    private suspend fun fromRemote(page: Int) = withContext(Dispatchers.IO) {
        remote.findMovies(page)
            //We have exponential backoff
            .filterNot { it is RemoteResult.NoConnection }
            .first()
            .toLoadResult(page)
    }

    private suspend fun cache(page: LoadResult<Int, RetrievedMovie>, invalidate: Boolean) = withContext(Dispatchers.IO) {
        when (page) {
            is LoadResult.Error -> {
                Timber.e(page.throwable)
                page
            }
            is LoadResult.Page -> {
                if (invalidate) local.deleteAll()
                local.addMovies(page.data)
                page
            }
        }
    }

    //TODO: paginate using the createdAt millis instead of page number and page size
    private suspend fun RemoteResult<List<RetrievedMovie>>.toLoadResult(
        page: Int
    ) = withContext(Dispatchers.IO) {
        when (val response = this@toLoadResult) {
            is RemoteResult.Success -> {
                LoadResult.Page(
                    data = response.v,
                    prevKey = null, // Only paging forward.
                    //If the list is empty, we ran out of remote data
                    nextKey = (page + 1).takeIf { response.v.isNotEmpty() }
                )
            }
            RemoteResult.NoConnection -> {
                LoadResult.Error(NoInternetConnectionException())
            }
            is RemoteResult.Error -> {
                Timber.e(response.e)
                LoadResult.Error(response.e)
            }
        }
    }

    override fun getRefreshKey(state: PagingState<Int, RetrievedMovie>): Int? {
        // Try to find the page key of the closest page to anchorPosition, from
        // either the prevKey or the nextKey, but you need to handle nullability
        // here:
        //  * prevKey == null -> anchorPage is the first page.
        //  * nextKey == null -> anchorPage is the last page.
        //  * both prevKey and nextKey null -> anchorPage is the initial page, so
        //    just return null.

        // Google wrote this, and it seems to work fine
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    private val key = UUID.randomUUID().toString()
    override fun equals(other: Any?): Boolean {
        return (other as? RemoteOnlyPagingSource)?.key == this.key
    }
}

package dev.ahmedmourad.tvmanager.movies.repo.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import dev.ahmedmourad.tvmanager.core.NoInternetConnectionException
import dev.ahmedmourad.tvmanager.core.users.model.RetrievedMovie
import dev.ahmedmourad.tvmanager.movies.repo.LocalDataSource
import dev.ahmedmourad.tvmanager.movies.repo.LocalResult
import dev.ahmedmourad.tvmanager.movies.repo.RemoteDataSource
import dev.ahmedmourad.tvmanager.movies.repo.RemoteResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * This's a TiledPagingSource, it starts by loading data from cache, then
 * it switches to remote data when the cache is consumed.
 * This paging source stores the data it fetches in cache and invalidate
 * the cache when appropriate
 */
internal class TiledPagingSource(
    private val local: LocalDataSource,
    private val remote: RemoteDataSource
) : PagingSource<Int, RetrievedMovie>() {

    override suspend fun load(params: LoadParams<Int>) = withContext(Dispatchers.IO) {
        // Start refresh at page 1 if undefined.
        val nextPage = params.key ?: 1
        fromCache(nextPage) ?: cache(fromRemote(nextPage))
    }

    private suspend fun fromCache(page: Int) = withContext(Dispatchers.IO) {
        when (val response = local.findMovies(page)) {
            is LocalResult.Success -> {
                //If cache is empty, we return null to start fetching remote data
                if (response.v.isEmpty()) return@withContext null
                LoadResult.Page(
                    data = response.v,
                    prevKey = null, // Only paging forward.
                    nextKey = page + 1
                )
            }
            is LocalResult.Error -> {
                Timber.e(response.e)
                //Errors in cache are logged and ignored
                //By returning null we start relying on remote data if cache fails
                null
            }
        }
    }

    private suspend fun fromRemote(page: Int) = withContext(Dispatchers.IO) {
        remote.findMovies(page)
            //We have exponential backoff
            .filterNot { it is RemoteResult.NoConnection }
            .first()
            .toLoadResult(page)
    }

    private suspend fun cache(page: LoadResult<Int, RetrievedMovie>) = withContext(Dispatchers.IO) {
        when (page) {
            is LoadResult.Error -> {
                Timber.e(page.throwable)
                page
            }
            is LoadResult.Page -> {
                local.addMovies(page.data)
                page
            }
        }
    }

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
}

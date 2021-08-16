package dev.ahmedmourad.tvmanager.movies.remote

import dev.ahmedmourad.tvmanager.core.NoInternetConnectionException
import dev.ahmedmourad.tvmanager.movies.repo.RemoteResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlin.math.pow

@OptIn(ExperimentalCoroutinesApi::class)
internal fun <T : Any> withExponentialBackoff(
    initialDelay: Long = 1000,
    factor: Float = 2f,
    maxDelay: Long = 60_000,
    maxAttempts: Int = 40,
    remoteCall: suspend () -> RemoteResult<T>,
): Flow<RemoteResult<T>> {
    return flow {
        //We fetch data remotely
        when (val result = remoteCall()) {
            //If data is found
            is RemoteResult.Success -> {
                emit(RemoteResult.Success(result.v))
            }
            //If we encounter a connection problem
            RemoteResult.NoConnection -> {
                emit(RemoteResult.NoConnection)
                //We throw an exception to trigger retryWhen
                throw NoInternetConnectionException()
            }
        }
    }.flowOn(Dispatchers.IO).retryWhen { cause, attempts ->
        //Exponential Backoff
        if (attempts < maxAttempts && cause is NoInternetConnectionException) {
            delay(calculateDelay(initialDelay, factor, maxDelay, attempts))
            true
        } else {
            false
        }
    }.distinctUntilChanged().catch { cause ->
        if (cause is NoInternetConnectionException) {
            emit(RemoteResult.NoConnection)
        } else {
            emit(RemoteResult.Error(cause))
        }
    }
}

private fun calculateDelay(
    initialDelay: Long,
    factor: Float,
    maxDelay: Long,
    attempts: Long
): Long {
    return (initialDelay * factor.pow(attempts.toFloat()).toLong()).coerceAtMost(maxDelay)
}

package com.podcast.core.network

import androidx.annotation.AnyThread
import com.podcast.core.network.api.ItunesApi
import javax.inject.Inject

class ApiServiceExecutor @Inject constructor(
    private val apiService: ItunesApi,
    private val apiExceptionMapper: ApiExceptionMapper,
) {

    @AnyThread
    suspend fun <T> execute(
        mapHttpException: ((HttpException) -> Exception?)? = null,
        request: suspend (ItunesApi) -> T,
    ): T =
        try {
            request(apiService)
        } catch (e: Exception) {
            throw apiExceptionMapper.map(e).let {
                if (it is HttpException) {
                    mapHttpException?.invoke(it) ?: it
                } else {
                    it
                }
            }
        }
}

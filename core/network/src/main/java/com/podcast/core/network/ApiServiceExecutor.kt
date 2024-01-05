package com.podcast.core.network

import androidx.annotation.AnyThread
import com.podcast.core.network.api.FYYDApi
import com.podcast.core.network.api.IndexApi
import com.podcast.core.network.api.ItunesApi
import timber.log.Timber
import javax.inject.Inject

class ApiServiceExecutor @Inject constructor(
    private val apiService: ItunesApi,
    private val fyydApi: FYYDApi,
    private val indexApi: IndexApi,
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
            Timber.e(e)
            throw apiExceptionMapper.map(e).let {
                if (it is HttpException) {
                    mapHttpException?.invoke(it) ?: it
                } else {
                    it
                }
            }
        }

    @AnyThread
    suspend fun <T> executeFyyD(
        mapHttpException: ((HttpException) -> Exception?)? = null,
        request: suspend (FYYDApi) -> T,
    ): T =
        try {
            request(fyydApi)
        } catch (e: Exception) {
            Timber.e(e)
            throw apiExceptionMapper.map(e).let {
                if (it is HttpException) {
                    mapHttpException?.invoke(it) ?: it
                } else {
                    it
                }
            }
        }
    @AnyThread
    suspend fun <T> executeIndex(
        mapHttpException: ((HttpException) -> Exception?)? = null,
        request: suspend (IndexApi) -> T,
    ): T =
        try {
            request(indexApi)
        } catch (e: Exception) {
            Timber.e(e)
            throw apiExceptionMapper.map(e).let {
                if (it is HttpException) {
                    mapHttpException?.invoke(it) ?: it
                } else {
                    it
                }
            }
        }
}

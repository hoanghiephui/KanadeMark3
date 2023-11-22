package com.podcast.core.network

import androidx.annotation.AnyThread
import caios.android.kanade.core.common.network.Dispatcher
import caios.android.kanade.core.common.network.KanadeDispatcher
import caios.android.kanade.core.common.network.extension.fromJson
import caios.android.kanade.core.model.exception.NoNetworkException
import caios.android.kanade.core.model.exception.UnexpectedException
import com.podcast.core.network.di.MoshiApiService
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.invoke
import timber.log.Timber
import java.io.IOException
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import retrofit2.HttpException as RetrofitHttpException

class ApiExceptionMapper @Inject constructor(
    @MoshiApiService private val moshi: Moshi,
    @Dispatcher(KanadeDispatcher.Default)
    private val defaultDispatcher: CoroutineDispatcher,
) {

    @AnyThread
    suspend fun map(error: Exception): Exception =
        when (error) {
            is IOException -> NoNetworkException(error)
            is RetrofitHttpException -> mapHttpException(error)
            else -> UnexpectedException(error)
        }

    @AnyThread
    private suspend fun mapHttpException(error: RetrofitHttpException): HttpException {
        val response = error.errorResponse()
        return HttpException(
            httpCode = error.code(),
            statusCode = response?.statusCode,
            statusMessage = response?.statusMessage,
        )
    }

    @AnyThread
    private suspend fun RetrofitHttpException.errorResponse(): ErrorResponse? = defaultDispatcher {
        response()?.errorBody()?.source()?.let {
            try {
                val json = String(it.readByteArray(), StandardCharsets.UTF_8)
                moshi.fromJson(json)
            } catch (e: JsonDataException) {
                Timber.e(e)
                null
            }
        }
    }
}

package com.podcast.core.network

import com.podcast.core.network.api.FYYDApi
import com.podcast.core.network.api.ItunesApi
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

internal object ApiServiceFactory {

    fun create(
        baseUrl: String,
        okHttpClient: OkHttpClient,
        moshi: Moshi,
    ): ItunesApi =
        setupService(
            baseUrl = baseUrl,
            okHttpClient = okHttpClient,
            moshi = moshi,
        )

    fun createFYYD(
        baseUrl: String,
        okHttpClient: OkHttpClient,
        moshi: Moshi,
    ): FYYDApi =
        setupFYYDService(
            baseUrl = baseUrl,
            okHttpClient = okHttpClient,
            moshi = moshi,
        )

    private fun setupService(
        baseUrl: String,
        okHttpClient: OkHttpClient,
        moshi: Moshi,
    ): ItunesApi {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .build()

        return retrofit.create(ItunesApi::class.java)
    }
    private fun setupFYYDService(
        baseUrl: String,
        okHttpClient: OkHttpClient,
        moshi: Moshi,
    ): FYYDApi {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .build()

        return retrofit.create(FYYDApi::class.java)
    }
}

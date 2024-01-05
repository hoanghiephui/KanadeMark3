package com.podcast.core.network

import com.podcast.core.network.api.FYYDApi
import com.podcast.core.network.api.IndexApi
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
        setupRetrofit(
            baseUrl = baseUrl,
            okHttpClient = okHttpClient,
            moshi = moshi,
        ).create(ItunesApi::class.java)

    fun createFYYD(
        baseUrl: String,
        okHttpClient: OkHttpClient,
        moshi: Moshi,
    ): FYYDApi =
        setupRetrofit(
            baseUrl = baseUrl,
            okHttpClient = okHttpClient,
            moshi = moshi,
        ).create(FYYDApi::class.java)

    fun createIndex(
        baseUrl: String,
        okHttpClient: OkHttpClient,
        moshi: Moshi,
    ): IndexApi =
        setupRetrofit(
            baseUrl = baseUrl,
            okHttpClient = okHttpClient,
            moshi = moshi,
        ).create(IndexApi::class.java)

    private fun setupRetrofit(
        baseUrl: String,
        okHttpClient: OkHttpClient,
        moshi: Moshi,
    ): Retrofit {

        return Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .build()
    }
}

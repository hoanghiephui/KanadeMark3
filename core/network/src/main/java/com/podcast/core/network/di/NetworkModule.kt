package com.podcast.core.network.di

import com.podcast.core.network.BuildConfig
import com.podcast.core.network.api.NAME_ITUNES
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun okHttpCallFactory(): Call.Factory = OkHttpClient.Builder()
        .addInterceptor(
            HttpLoggingInterceptor()
                .apply {
                    if (BuildConfig.DEBUG) {
                        setLevel(HttpLoggingInterceptor.Level.BODY)
                    }
                },
        )
        .build()

    @Provides
    @Singleton
    fun moshi() = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    @Provides
    @Singleton
    fun coreRetrofit(
        okHttpClient: Call.Factory,
        moshi: Moshi
    ) = Retrofit.Builder()
        .callFactory(okHttpClient)
        .addConverterFactory(
            MoshiConverterFactory.create(moshi)
        )

    @Provides
    @Singleton
    @Named(NAME_ITUNES)
    fun retrofitItunes(
        coreRetrofit: Retrofit.Builder
    ) = coreRetrofit
        .baseUrl("https://itunes.apple.com/")
        .build()
}

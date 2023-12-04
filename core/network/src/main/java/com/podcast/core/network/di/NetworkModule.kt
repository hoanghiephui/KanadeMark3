package com.podcast.core.network.di

import com.podcast.core.network.ApiServiceFactory
import com.podcast.core.network.BuildConfig
import com.podcast.core.network.OkHttpClientFactory
import com.podcast.core.network.adapter.StatusCodeAdapter
import com.podcast.core.network.api.ItunesApi
import com.prof18.rssparser.RssParserBuilder
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    @MoshiApiService
    fun provideMoshi(moshi: Moshi): Moshi =
        moshi
            .newBuilder()
            .add(StatusCodeAdapter)
            .build()

    @Provides
    @Singleton
    fun provideApiService(
        okHttpClient: OkHttpClient,
        @MoshiApiService moshi: Moshi,
    ): ItunesApi =
        ApiServiceFactory.create(
            baseUrl = "https://itunes.apple.com/",
            okHttpClient = okHttpClient,
            moshi = moshi,
        )

    @Provides
    @Singleton
    fun provideOkHttpClient(
        @ApplicationInterceptorOkHttpClient applicationInterceptors: Set<@JvmSuppressWildcards Interceptor>,
    ): OkHttpClient = OkHttpClientFactory.create(
        applicationInterceptors = applicationInterceptors,
    )

    @ApplicationInterceptorOkHttpClient
    @IntoSet
    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): Interceptor =
        HttpLoggingInterceptor().apply {
            if (BuildConfig.DEBUG) {
                setLevel(HttpLoggingInterceptor.Level.BODY)
            }
        }

    @Provides
    @Singleton
    fun provideRssParser(
        okHttpClient: OkHttpClient
    ): RssParserBuilder =
        RssParserBuilder(
            callFactory = okHttpClient,
            charset = Charsets.UTF_8,
        )
}

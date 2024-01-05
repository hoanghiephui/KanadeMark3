package com.podcast.core.network.di

import android.content.Context
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.util.DebugLogger
import com.podcast.core.network.ApiServiceFactory
import com.podcast.core.network.BuildConfig
import com.podcast.core.network.OkHttpClientFactory
import com.podcast.core.network.adapter.StatusCodeAdapter
import com.podcast.core.network.api.FYYDApi
import com.podcast.core.network.api.IndexApi
import com.podcast.core.network.api.ItunesApi
import com.podcast.core.network.util.PodcastDownloader
import com.prof18.rssparser.RssParser
import com.prof18.rssparser.RssParserBuilder
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.Date
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
            .add(Date::class.java, Rfc3339DateJsonAdapter())
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
    fun provideFyyDApiService(
        okHttpClient: OkHttpClient,
        @MoshiApiService moshi: Moshi,
    ): FYYDApi =
        ApiServiceFactory.createFYYD(
            baseUrl = "https://api.fyyd.de/0.2/",
            okHttpClient = okHttpClient,
            moshi = moshi,
        )

    @Provides
    @Singleton
    fun provideIndexApiService(
        okHttpClient: OkHttpClient,
        @MoshiApiService moshi: Moshi,
    ): IndexApi =
        ApiServiceFactory.createIndex(
            baseUrl = "https://api.podcastindex.org/api/1.0/",
            okHttpClient = okHttpClient,
            moshi = moshi,
        )

    @Provides
    @Singleton
    fun provideOkHttpClient(
        @ApplicationContext
        context: Context,
        @ApplicationInterceptorOkHttpClient
        applicationInterceptors: Set<@JvmSuppressWildcards Interceptor>,
    ): OkHttpClient = OkHttpClientFactory.create(
        applicationInterceptors = applicationInterceptors,
        context = context
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
        okHttpClient: OkHttpClient,
        ): RssParser =
        RssParserBuilder(
            callFactory = okHttpClient,
            charset = Charsets.UTF_8,
        ).build()

    @Provides
    @Singleton
    fun imageLoader(
        okHttpClient: OkHttpClient,
        @ApplicationContext
        application: Context,
    ): ImageLoader = ImageLoader.Builder(application)
        .okHttpClient(okHttpClient)
        .components {
            add(SvgDecoder.Factory())
        }
        // Assume most content images are versioned urls
        // but some problematic images are fetching each time
        .respectCacheHeaders(false)
        .apply {
            if (BuildConfig.DEBUG) {
                logger(DebugLogger())
            }
        }
        .build()

    @Singleton
    @Provides
    fun provideBookDownloader(@ApplicationContext context: Context) = PodcastDownloader(context)
}

package com.podcast.core.network

import caios.android.kanade.core.model.podcast.ItunesTopPodcastResponse
import com.podcast.core.network.api.ItunesApi
import com.podcast.core.network.api.NAME_ITUNES
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class RetrofitNetwork @Inject constructor(
    @Named(NAME_ITUNES)
    private val retrofit: Retrofit
): NetworkDataSource {
    private val networkApi = retrofit
        .create(ItunesApi::class.java)

    override suspend fun getTopPodcast(country: String, limit: Int): ItunesTopPodcastResponse =
        networkApi.getTopPodcast(country, limit)
}

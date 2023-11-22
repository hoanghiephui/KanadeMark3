
package com.podcast.core.network.di

import com.podcast.core.network.NetworkDataSource
import com.podcast.core.network.RetrofitNetwork
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface FlavoredNetworkModule {

    @Binds
    fun binds(impl: RetrofitNetwork): NetworkDataSource
}

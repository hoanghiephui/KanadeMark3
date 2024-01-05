
package com.podcast.core.network.di

import com.podcast.core.network.datasource.DefaultFyyDDataSource
import com.podcast.core.network.datasource.DefaultItunesDataSource
import com.podcast.core.network.datasource.FyyDDataSource
import com.podcast.core.network.datasource.ItunesDataSource
import com.podcast.core.network.util.ConnectivityManagerNetworkMonitor
import com.podcast.core.network.util.NetworkMonitor
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface DataSourceModule {

    @Binds
    fun bindsItunesDataSource(impl: DefaultItunesDataSource): ItunesDataSource

    @Binds
    fun bindsFyyDDataSource(impl: DefaultFyyDDataSource): FyyDDataSource


    @Binds
    fun bindsNetworkMonitor(
        networkMonitor: ConnectivityManagerNetworkMonitor,
    ): NetworkMonitor
}

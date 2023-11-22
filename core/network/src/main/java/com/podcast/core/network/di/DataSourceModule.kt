
package com.podcast.core.network.di

import com.podcast.core.network.datasource.DefaultItunesDataSource
import com.podcast.core.network.datasource.ItunesDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface DataSourceModule {

    @Binds
    fun binds(impl: DefaultItunesDataSource): ItunesDataSource
}

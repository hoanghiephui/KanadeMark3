package com.podcast.core.network.di

import javax.inject.Qualifier

@Qualifier
annotation class ApplicationInterceptorOkHttpClient

@Qualifier
annotation class NetworkInterceptorOkHttpClient

@Qualifier
annotation class MoshiApiService

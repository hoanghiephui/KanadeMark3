package caios.android.kanade.core.common.network.di

import android.content.Context
import caios.android.kanade.core.common.R
import caios.android.kanade.core.common.network.DateTimeProvider
import caios.android.kanade.core.common.network.DefaultDateTimeProvider
import caios.android.kanade.core.common.network.moshi.DurationAdapter
import caios.android.kanade.core.common.network.moshi.InstantAdapter
import caios.android.kanade.core.common.network.moshi.LocalDateAdapter
import caios.android.kanade.core.common.network.moshi.OffsetDateTimeAdapter
import com.applovin.sdk.AppLovinMediationProvider
import com.applovin.sdk.AppLovinSdk
import com.applovin.sdk.AppLovinSdkInitializationConfiguration
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal class CommonModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi =
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .add(LocalDateAdapter)
            .add(DurationAdapter)
            .add(OffsetDateTimeAdapter)
            .add(InstantAdapter)
            .build()

    @Provides
    @Singleton
    fun provideStateFlow(): MutableSharedFlow<Int> = MutableSharedFlow()

    @Provides
    @Singleton
    fun provideApplovin(
        @ApplicationContext
        context: Context
    ): AppLovinSdk =
        AppLovinSdk.getInstance(context)

    @Provides
    @Singleton
    fun provideSdkInitialization(
        @ApplicationContext
        context: Context
    ): AppLovinSdkInitializationConfiguration =
        AppLovinSdkInitializationConfiguration.builder(
            context.getString(R.string.APPLOVIN_SDK_KEY),
            context
        )
            .setMediationProvider(AppLovinMediationProvider.MAX)
            .build()
}

@Module
@InstallIn(SingletonComponent::class)
internal interface CommonModuleBinds {

    @Binds
    @Singleton
    fun provideTimeProvider(dateTimeProvider: DefaultDateTimeProvider): DateTimeProvider
}

const val EVENT_RESTART_APP = 1


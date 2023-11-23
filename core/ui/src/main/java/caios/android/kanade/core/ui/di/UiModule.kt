package caios.android.kanade.core.ui.di

import caios.android.kanade.core.ui.error.DefaultErrorsDispatcher
import caios.android.kanade.core.ui.error.ErrorsDispatcher
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface UiModule {

    @Binds
    @Singleton
    fun provideErrorsDispatcher(errorsDispatcher: DefaultErrorsDispatcher): ErrorsDispatcher
}

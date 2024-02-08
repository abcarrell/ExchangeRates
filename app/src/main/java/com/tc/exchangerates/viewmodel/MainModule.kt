package com.tc.exchangerates.viewmodel

import com.tc.exchangerates.mvi.MVIActor
import com.tc.exchangerates.mvi.mvi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import javax.inject.Named

@Module
@InstallIn(ViewModelComponent::class)
object MainModule {
    @Provides
    @Named("MainMVIActor")
    fun provideMainMVIActor(): MVIActor<MainUiState, MainEvent, UIEffect> = mvi(MainUiState())
}

package com.betsudotai.shibari.core.di

import com.betsudotai.shibari.application.usecase.GetMyQuestsUseCaseImpl
import com.betsudotai.shibari.domain.usecase.GetMyQuestsUseCase
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UseCaseModule {

    @Binds
    @Singleton
    abstract fun bindGetMyQuestsUseCase(
        impl: GetMyQuestsUseCaseImpl
    ): GetMyQuestsUseCase
}

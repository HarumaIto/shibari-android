package com.betsudotai.shibari.core.di

import com.betsudotai.shibari.data.repository.QuestRepositoryImpl
import com.betsudotai.shibari.data.repository.TimelineRepositoryImpl
import com.betsudotai.shibari.domain.repository.QuestRepository
import com.betsudotai.shibari.domain.repository.TimelineRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindQuestRepository(
        impl: QuestRepositoryImpl
    ): QuestRepository

    @Binds
    @Singleton
    abstract fun bindTimelineRepository(
        impl: TimelineRepositoryImpl
    ): TimelineRepository
}
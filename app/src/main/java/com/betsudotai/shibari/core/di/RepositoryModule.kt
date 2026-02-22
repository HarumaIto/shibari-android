package com.betsudotai.shibari.core.di

import com.betsudotai.shibari.data.repository.AuthRepositoryImpl
import com.betsudotai.shibari.data.repository.GroupRepositoryImpl
import com.betsudotai.shibari.data.repository.QuestRepositoryImpl
import com.betsudotai.shibari.data.repository.TimelineRepositoryImpl
import com.betsudotai.shibari.data.repository.UserRepositoryImpl
import com.betsudotai.shibari.domain.repository.AuthRepository
import com.betsudotai.shibari.domain.repository.QuestRepository
import com.betsudotai.shibari.domain.repository.TimelineRepository
import com.betsudotai.shibari.domain.repository.UserRepository
import com.betsudotai.shibari.domain.repository.GroupRepository
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

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        impl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindGroupRepository(
        impl: GroupRepositoryImpl
    ): GroupRepository
}
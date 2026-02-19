package com.betsudotai.shibari.core.di

import com.betsudotai.shibari.data.datasource.remote.QuestRemoteDataSource
import com.betsudotai.shibari.data.datasource.remote.QuestRemoteDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {

    @Binds
    @Singleton
    abstract fun bindQuestRemoteDataSource(
        impl: QuestRemoteDataSourceImpl
    ): QuestRemoteDataSource
}
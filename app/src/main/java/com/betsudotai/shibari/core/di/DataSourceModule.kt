package com.betsudotai.shibari.core.di

import com.betsudotai.shibari.data.datasource.remote.QuestRemoteDataSource
import com.betsudotai.shibari.data.datasource.remote.QuestRemoteDataSourceImpl
import com.betsudotai.shibari.data.datasource.remote.TimelineRemoteDataSource
import com.betsudotai.shibari.data.datasource.remote.TimelineRemoteDataSourceImpl
import com.betsudotai.shibari.data.datasource.remote.UserRemoteDataSource
import com.betsudotai.shibari.data.datasource.remote.UserRemoteDataSourceImpl
import com.betsudotai.shibari.data.datasource.remote.GroupRemoteDataSource
import com.betsudotai.shibari.data.datasource.remote.GroupRemoteDataSourceImpl
import com.betsudotai.shibari.data.datasource.remote.ReportRemoteDataSource
import com.betsudotai.shibari.data.datasource.remote.ReportRemoteDataSourceImpl
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

    @Binds
    @Singleton
    abstract fun bindTimelineRemoteDataSource(
        impl: TimelineRemoteDataSourceImpl
    ): TimelineRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindUserRemoteDataSource(
        impl: UserRemoteDataSourceImpl
    ): UserRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindGroupRemoteDataSource(
        impl: GroupRemoteDataSourceImpl
    ): GroupRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindReportRemoteDataSource(
        impl: ReportRemoteDataSourceImpl
    ): ReportRemoteDataSource

}
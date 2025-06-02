package com.hv.bukutm.di

import com.hv.bukutm.data.ReportsRepositoryImpl
import com.hv.bukutm.data.api.ReportsApi
import com.hv.bukutm.domain.repository.ReportsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ReportsModule {

    @Provides
    @Singleton
    fun provideReportsRepository(api: ReportsApi): ReportsRepository {
        return ReportsRepositoryImpl(api)
    }
}
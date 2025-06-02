package com.hv.bukutm.di

import com.hv.bukutm.data.remote.NotificationApi
import com.hv.bukutm.data.repository.NotificationRepositoryImpl
import com.hv.bukutm.domain.repository.NotificationRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NotificationModule {

    @Provides
    @Singleton
    fun provideNotificationRepository(notificationApi: NotificationApi): NotificationRepository {
        return NotificationRepositoryImpl(notificationApi)
    }
}
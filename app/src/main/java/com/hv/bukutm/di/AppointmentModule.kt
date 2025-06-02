package com.hv.bukutm.di

import com.hv.bukutm.data.remote.AppointmentApi
import com.hv.bukutm.domain.repository.AppointmentRepository
import com.hv.bukutm.data.repository.AppointmentRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppointmentModule {

    @Provides
    @Singleton
    fun provideAppointmentApi(retrofit: Retrofit): AppointmentApi {
        return retrofit.create(AppointmentApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAppointmentRepository(api: AppointmentApi): AppointmentRepository {
        return AppointmentRepositoryImpl(api)
    }
}
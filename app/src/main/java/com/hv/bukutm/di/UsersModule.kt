package com.hv.bukutm.di

import com.hv.bukutm.data.UserRepositoryImpl
import com.hv.bukutm.data.api.UsersApi
import com.hv.bukutm.domain.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UsersModule {

    @Provides
    @Singleton
    fun provideUserRepository(api: UsersApi): UserRepository {
        return UserRepositoryImpl(api)
    }
}
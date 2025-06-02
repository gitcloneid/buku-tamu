package com.hv.bukutm.di

import com.hv.bukutm.data.remote.websocket.OkHttpWebSocketService
import com.hv.bukutm.data.remote.websocket.WebSocketService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WebSocketModule {

    @Binds
    @Singleton
    abstract fun bindWebSocketService(
        okHttpWebSocketService: OkHttpWebSocketService
    ): WebSocketService
}
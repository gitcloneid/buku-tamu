package com.hv.bukutm.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.hv.bukutm.data.api.ReportsApi
import com.hv.bukutm.data.api.UsersApi
import com.hv.bukutm.data.remote.AuthApi
import com.hv.bukutm.data.remote.NotificationApi
import com.hv.bukutm.data.remote.WhatsAppApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    //http://192.168.1.80:3001/
    //http://192.168.1.80:5000/
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val gson = GsonBuilder().create()
        return Retrofit.Builder()
            .baseUrl("https://api-sub.clowlaw.my.id/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    @Named("WhatsAppRetrofit")
    fun provideWhatsAppRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val gson = GsonBuilder().create()
        return Retrofit.Builder()
            .baseUrl("https://wa-service.clowlaw.my.id/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideWhatsAppApi(@Named("WhatsAppRetrofit") retrofit: Retrofit): WhatsAppApi {
        return retrofit.create(WhatsAppApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }

    @Provides
    @Singleton
    fun provideNotificationApi(retrofit: Retrofit): NotificationApi {
        return retrofit.create(NotificationApi::class.java)
    }

    @Provides
    @Singleton
    fun provideReportsApi(retrofit: Retrofit): ReportsApi {
        return retrofit.create(ReportsApi::class.java)
    }

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder().create()
    }

    @Provides
    @Singleton
    fun provideUsersApi(retrofit: Retrofit): UsersApi {
        return retrofit.create(UsersApi::class.java)
    }
}
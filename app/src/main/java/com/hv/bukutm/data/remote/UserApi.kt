package com.hv.bukutm.data.api

import com.hv.bukutm.domain.model.User
import com.hv.bukutm.domain.model.UserResponse
import com.hv.bukutm.domain.model.CreateUserRequest
import com.hv.bukutm.domain.model.UpdateUserRequest
import retrofit2.http.*

interface UsersApi {

    @GET("api/users")
    suspend fun getUsers(
        @Header("Authorization") token: String,
        @Query("role") role: String? = null
    ): UserResponse

    @GET("api/users/{id}")
    suspend fun getUserById(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): User

    @POST("api/users")
    suspend fun createUser(
        @Header("Authorization") token: String,
        @Body request: CreateUserRequest
    ): User

    @PUT("api/users/{id}")
    suspend fun updateUser(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: UpdateUserRequest
    ): User

    @DELETE("api/users/{id}")
    suspend fun deleteUser(
        @Header("Authorization") token: String,
        @Path("id") id: String
    )
}
package com.hv.bukutm.domain.repository

import com.hv.bukutm.domain.model.User
import com.hv.bukutm.domain.model.UserResponse
import com.hv.bukutm.domain.model.CreateUserRequest
import com.hv.bukutm.domain.model.UpdateUserRequest
import kotlin.Result

interface UserRepository {
    suspend fun getUsers(token: String, role: String? = null): Result<UserResponse>
    suspend fun getUserById(token: String, id: Int): Result<User>
    suspend fun createUser(token: String, request: CreateUserRequest): Result<User>
    suspend fun updateUser(token: String, id: Int, request: UpdateUserRequest): Result<User>
    suspend fun deleteUser(token: String, id: Int): Result<Unit>
}
package com.hv.bukutm.domain.repository

import com.hv.bukutm.domain.model.User

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun refreshToken(refreshToken: String): Result<Pair<String, String>>
}
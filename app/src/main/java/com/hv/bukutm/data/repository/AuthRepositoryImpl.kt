package com.hv.bukutm.data.repository

import com.hv.bukutm.data.remote.AuthApi
import com.hv.bukutm.data.remote.LoginResponse
import com.hv.bukutm.domain.model.User
import com.hv.bukutm.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi
) : AuthRepository {
    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val response = authApi.login(
                com.hv.bukutm.data.remote.LoginRequest(email, password)
            )
            Result.success(
                User(
                    idPengguna = response.user.idPengguna,
                    nama = response.user.nama,
                    email = response.user.email,
                    role = response.user.role,
                    token = response.token,
                    refreshToken = response.refreshToken // Ensure refreshToken is passed
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun refreshToken(refreshToken: String): Result<Pair<String, String>> {
        return try {
            val response = authApi.refreshToken(
                com.hv.bukutm.data.remote.RefreshTokenRequest(refreshToken)
            )
            Result.success(Pair(response.accessToken, response.refreshToken))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}